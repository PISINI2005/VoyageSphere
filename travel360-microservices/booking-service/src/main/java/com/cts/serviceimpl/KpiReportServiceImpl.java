package com.cts.serviceimpl;

import com.cts.dto.KpiReportResponseDTO;
import com.cts.dto.KpiStatsDTO;
import com.cts.dto.MonthlyKpiCountsDTO;
import com.cts.dto.MonthlyKpiStatsDTO;
import com.cts.entity.KpiReport;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.KpiReportMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.KpiReportRepository;
import com.cts.repository.PaymentRepository;
import com.cts.service.KpiReportService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class KpiReportServiceImpl implements KpiReportService {

    private final BookingRepository bookingRepo;
    private final PaymentRepository paymentRepo;
    private final KpiReportRepository kpiRepo;
    private final KpiReportMapper kpiReportMapper;

    @Override
    @Transactional
    public KpiReportResponseDTO generateMonthlyReport(int month, int year) {
        log.info("Generating monthly KPI report for {}-{}", year, String.format("%02d", month));
        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);   // half-open [start, end): clean month boundary, no sub-second key
        String label = "Monthly - " + year + "-" + String.format("%02d", month);
        KpiReportResponseDTO result = save(computeReport(start, end, label));
        log.info("Monthly KPI report generated for {}-{}", year, String.format("%02d", month));
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public KpiReportResponseDTO generateCustomReport(LocalDateTime start, LocalDateTime end) {
        log.info("Generating (transient) custom KPI report from {} to {}", start, end);
        // Custom reports are computed on demand and returned only — never persisted, so they
        // carry no reportId and don't appear in getAllReports / getReportById.
        return kpiReportMapper.toResponse(computeReport(start, end, "Custom Range"));
    }

    @Override
    @Transactional
    public List<KpiReportResponseDTO> generateYearlyReport(int year) {
        log.info("Generating yearly breakdown for {}: money from payments, counts from bookings", year);

        // Two single queries for the whole year: money (from the Payment ledger) and
        // counts (from Booking). They're merged per month below.
        List<MonthlyKpiStatsDTO> monthlyMoney = paymentRepo.getMonthlyMoneyStats(year);
        List<MonthlyKpiCountsDTO> monthlyCounts = bookingRepo.getMonthlyCounts(year);

        List<KpiReportResponseDTO> yearlyData = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            int currentMonth = month;
            LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
            LocalDateTime end = start.plusMonths(1);   // half-open [start, end)
            String label = "Monthly - " + year + "-" + String.format("%02d", month);

            MonthlyKpiStatsDTO money = monthlyMoney.stream()
                    .filter(s -> s.getMonth() == currentMonth)
                    .findFirst()
                    .orElse(new MonthlyKpiStatsDTO(currentMonth, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            MonthlyKpiCountsDTO counts = monthlyCounts.stream()
                    .filter(c -> c.getMonth() == currentMonth)
                    .findFirst()
                    .orElse(new MonthlyKpiCountsDTO(currentMonth, 0L, 0L));

            yearlyData.add(save(buildReport(start, end, label,
                                    money.getTotalRevenue(),
                                    counts.getTotalBookings(),
                                    counts.getTotalCancellations(),
                                    money.getFlightRevenue(),
                                    money.getHotelRevenue(),
                                    money.getTransportRevenue(),
                                    money.getPackageRevenue(),
                                    money.getRefundedAmount())));
        }
        log.info("Yearly KPI report generated for {} — {} months processed", year, yearlyData.size());
        return yearlyData;
    }

    @Override
    public List<KpiReportResponseDTO> getAllReports() {
        log.info("Fetching all KPI reports");
        List<KpiReportResponseDTO> reports = kpiRepo.findAll().stream()
                .map(kpiReportMapper::toResponse)
                .toList();
        log.debug("Returned {} KPI reports", reports.size());
        return reports;
    }

    @Override
    public KpiReportResponseDTO getReportById(Long id) {
        log.info("Fetching KPI report with id={}", id);
        KpiReport report = kpiRepo.findById(id).orElseThrow(() -> {
            log.error("KPI report not found with id={}", id);
            return new ResourceNotFoundException("Report not found");
        });
        log.info("KPI report found with id={}", id);
        return kpiReportMapper.toResponse(report);
    }

    /** Runs the period queries (money from the Payment ledger, counts from Booking) into a transient report. */
    private KpiReport computeReport(LocalDateTime start, LocalDateTime end, String label) {
        log.info("Calculating KPI for period {} to {} with label {}", start, end, label);

        KpiStatsDTO money = paymentRepo.getMoneyStats(start, end);
        if (money == null) {
            money = new KpiStatsDTO(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        long totalBookings = bookingRepo.countBookingsInPeriod(start, end);
        long cancellations = bookingRepo.countCancellationsInPeriod(start, end);

        return buildReport(start, end, label,
                money.getTotalRevenue(), totalBookings, cancellations,
                money.getFlightRevenue(), money.getHotelRevenue(),
                money.getTransportRevenue(), money.getPackageRevenue(), money.getRefundedAmount());
    }

    /** Builds a transient (unsaved) KpiReport from explicit metric values, coalescing nulls and deriving ratios. */
    private KpiReport buildReport(LocalDateTime start, LocalDateTime end, String label,
                                  Double totalRev, long totalB, long cancelled,
                                  Double fRev, Double hRev, Double tRev, Double pRev, Double refunds) {
        // JPQL SUM() returns null when there are no matching rows; coalesce to 0.0.
        totalRev = nz(totalRev);
        fRev = nz(fRev);
        hRev = nz(hRev);
        tRev = nz(tRev);
        pRev = nz(pRev);
        refunds = nz(refunds);

        double avgValue = totalB > 0 ? Math.round((totalRev / totalB) * 100.0) / 100.0 : 0.0;
        double cancelRate = totalB > 0 ? ((double) cancelled / totalB) * 100 : 0.0;

        return KpiReport.builder()
                .generatedAt(LocalDateTime.now())
                .startDate(start)
                .endDate(end)
                .reportLabel(label)
                .totalRevenue(totalRev)
                .totalBookings(totalB)
                .totalCancellations(cancelled)
                .cancellationRate(cancelRate)
                .averageBookingValue(avgValue)
                .flightRevenue(fRev)
                .hotelRevenue(hRev)
                .transportRevenue(tRev)
                .packageRevenue(pRev)
                .refundedAmount(refunds)
                .build();
    }

    /** Upserts a report for its period: updates the existing row for [start, end) if one exists, else inserts. */
    private KpiReportResponseDTO save(KpiReport report) {
        log.debug("Saving KPI report: label={} totalBookings={} totalRevenue={}",
                report.getReportLabel(), report.getTotalBookings(), report.getTotalRevenue());
        kpiRepo.findFirstByStartDateAndEndDate(report.getStartDate(), report.getEndDate())
                .ifPresent(existing -> report.setReportId(existing.getReportId()));
        return kpiReportMapper.toResponse(kpiRepo.save(report));
    }

    private static double nz(Double value) {
        return value == null ? 0.0 : value;
    }
}
