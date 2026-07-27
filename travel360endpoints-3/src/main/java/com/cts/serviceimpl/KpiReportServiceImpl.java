package com.cts.serviceimpl;

import com.cts.dto.KpiReportResponseDTO;
import com.cts.dto.KpiStatsDTO;
import com.cts.repository.BookingRepository;
import com.cts.repository.PaymentRepository;
import com.cts.service.KpiReportService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiReportServiceImpl implements KpiReportService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;

    // Method A: Keeps the original behavior for point-in-time snapshots
    @Override
    public KpiReportResponseDTO generateReport(Integer year, Integer month) {
        LocalDateTime startDate;
        LocalDateTime endDate;
        String label;

        if (year != null && month != null) {
            startDate = LocalDateTime.of(year, month, 1, 0, 0);
            endDate = YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59, 999999999);
            label = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " - " + year;
        } else if (year != null) {
            startDate = LocalDateTime.of(year, 1, 1, 0, 0);
            endDate = LocalDateTime.of(year, 12, 31, 23, 59, 59, 999999999);
            label = "Annual Summary - " + year;
        } else {
            startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
            endDate = LocalDateTime.of(2099, 12, 31, 23, 59, 59, 999999999);
            label = "All-Time Metrics Overview";
        }

        long totalBookings = bookingRepository.countBookingsInPeriod(startDate, endDate);
        long totalCancellations = bookingRepository.countCancellationsInPeriod(startDate, endDate);
        KpiStatsDTO moneyStats = paymentRepository.getMoneyStats(startDate, endDate);

        if (moneyStats == null) {
            moneyStats = new KpiStatsDTO(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        }

        return mapToDto(null, startDate, endDate, label, totalBookings, totalCancellations, moneyStats);
    }

    // Method B: Your specific request - returns a rich trend of months for "where year = :year"
    @Override
    public List<KpiReportResponseDTO> generateMonthlyTrendReport(int year) {
        List<Object[]> bookingRows = bookingRepository.getMonthlyBookingCountsByYear(year);
        List<Object[]> moneyRows = paymentRepository.getMonthlyMoneyStatsByYear(year);

        Map<Integer, long[]> bookingMap = new HashMap<>();
        for (Object[] row : bookingRows) {
            int m = ((Number) row[0]).intValue();
            long cnt = ((Number) row[1]).longValue();
            long can = row[2] != null ? ((Number) row[2]).longValue() : 0L;
            bookingMap.put(m, new long[]{cnt, can});
        }

        List<KpiReportResponseDTO> reportList = new ArrayList<>();

        for (Object[] row : moneyRows) {
            int m = ((Number) row[0]).intValue();
            
            KpiStatsDTO stats = new KpiStatsDTO(
                row[1] != null ? ((Number) row[1]).doubleValue() : 0.0,
                row[2] != null ? ((Number) row[2]).doubleValue() : 0.0,
                row[3] != null ? ((Number) row[3]).doubleValue() : 0.0,
                row[4] != null ? ((Number) row[4]).doubleValue() : 0.0,
                row[5] != null ? ((Number) row[5]).doubleValue() : 0.0,
                row[6] != null ? ((Number) row[6]).doubleValue() : 0.0
            );

            long[] counts = bookingMap.getOrDefault(m, new long[]{0L, 0L});
            String label = Month.of(m).getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " - " + year;

            reportList.add(mapToDto(
                null,
                LocalDateTime.of(year, m, 1, 0, 0),
                YearMonth.of(year, m).atEndOfMonth().atTime(23, 59, 59),
                label,
                counts[0],
                counts[1],
                stats
            ));
        }
        return reportList;
    }

    private KpiReportResponseDTO mapToDto(Long id, LocalDateTime start, LocalDateTime end, String label, 
                                          long bookings, long cancellations, KpiStatsDTO money) {
        double cancellationRate = bookings > 0 ? ((double) cancellations / bookings) * 100.0 : 0.0;
        double avgValue = bookings > 0 ? (money.getTotalRevenue() / bookings) : 0.0;

        return KpiReportResponseDTO.builder()
                
                .generatedAt(LocalDateTime.now())
                .startDate(start)
                .endDate(end)
                .reportLabel(label)
                .totalBookings(bookings)
                .totalCancellations(cancellations)
                .cancellationRate(cancellationRate)
                .averageBookingValue(avgValue)
                .totalRevenue(money.getTotalRevenue())
                .flightRevenue(money.getFlightRevenue())
                .hotelRevenue(money.getHotelRevenue())
                .transportRevenue(money.getTransportRevenue())
                .packageRevenue(money.getPackageRevenue())
                .refundedAmount(money.getRefundedAmount())
                .build();
    }
}