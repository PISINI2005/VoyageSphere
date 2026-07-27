//package com.cts.serviceimpl;
//
//import com.cts.dto.*;
//import com.cts.entity.KpiReport;
//import com.cts.exception.ResourceNotFoundException;
//import com.cts.mapper.KpiReportMapper;
//import com.cts.repository.BookingRepository;
//import com.cts.repository.KpiReportRepository;
//import com.cts.repository.PaymentRepository;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Spy;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class KpiReportServiceImplTest {
//
//    @Mock private BookingRepository bookingRepo;
//    @Mock private PaymentRepository paymentRepo;
//    @Mock private KpiReportRepository kpiRepo;
//    @Spy private KpiReportMapper kpiReportMapper = new KpiReportMapper();
//
//    @InjectMocks
//    private KpiReportServiceImpl service;
//
//    private KpiReport report;
//    private KpiStatsDTO money;
//
//    @BeforeEach
//    void setup() {
//
//        report = new KpiReport();
//        report.setReportId(1L);
//        report.setTotalRevenue(1000.0);
//        report.setTotalBookings(10L);
//
//        // money side: net revenue + per-type + refunds (counts come from the booking queries)
//        money = new KpiStatsDTO(1000.0, 100.0, 200.0, 300.0, 400.0, 50.0);
//    }
//
//    // ✅ MONTHLY REPORT SUCCESS
//    @Test
//    void generateMonthly_success() {
//
//        when(paymentRepo.getMoneyStats(any(), any())).thenReturn(money);
//        when(bookingRepo.countBookingsInPeriod(any(), any())).thenReturn(10L);
//        when(bookingRepo.countCancellationsInPeriod(any(), any())).thenReturn(2L);
//        when(kpiRepo.findFirstByStartDateAndEndDate(any(), any())).thenReturn(Optional.empty());
//        when(kpiRepo.save(any())).thenReturn(report);
//
//        assertNotNull(service.generateMonthlyReport(5, 2025));
//    }
//
//    // ✅ CUSTOM REPORT SUCCESS
//    @Test
//    void generateCustom_success() {
//
//        // Custom reports are computed only — no findFirst / save stubs, and nothing is persisted.
//        when(paymentRepo.getMoneyStats(any(), any())).thenReturn(money);
//        when(bookingRepo.countBookingsInPeriod(any(), any())).thenReturn(10L);
//        when(bookingRepo.countCancellationsInPeriod(any(), any())).thenReturn(2L);
//
//        LocalDateTime start = LocalDateTime.now().minusDays(5);
//        LocalDateTime end = LocalDateTime.now();
//
//        assertNotNull(service.generateCustomReport(start, end));
//        verify(kpiRepo, never()).save(any());
//    }
//
//    // ✅ NULL MONEY COVERAGE (aggregate over an empty period returns null)
//    @Test
//    void generateMonthly_nullMoneyCoverage() {
//
//        when(paymentRepo.getMoneyStats(any(), any())).thenReturn(null);
//        when(bookingRepo.countBookingsInPeriod(any(), any())).thenReturn(0L);
//        when(bookingRepo.countCancellationsInPeriod(any(), any())).thenReturn(0L);
//        when(kpiRepo.findFirstByStartDateAndEndDate(any(), any())).thenReturn(Optional.empty());
//        when(kpiRepo.save(any())).thenReturn(report);
//
//        assertNotNull(service.generateMonthlyReport(1, 2025));
//    }
//
//    // ✅ YEARLY REPORT SUCCESS
//    @Test
//    void generateYearly_success() {
//
//        List<MonthlyKpiStatsDTO> moneyByMonth = List.of(
//                new MonthlyKpiStatsDTO(1, 100.0, 10.0, 10.0, 10.0, 10.0, 5.0)
//        );
//        List<MonthlyKpiCountsDTO> countsByMonth = List.of(
//                new MonthlyKpiCountsDTO(1, 2L, 1L)
//        );
//
//        when(paymentRepo.getMonthlyMoneyStats(anyInt())).thenReturn(moneyByMonth);
//        when(bookingRepo.getMonthlyCounts(anyInt())).thenReturn(countsByMonth);
//        when(kpiRepo.findFirstByStartDateAndEndDate(any(), any())).thenReturn(Optional.empty());
//        when(kpiRepo.save(any())).thenReturn(report);
//
//        List<KpiReportResponseDTO> result = service.generateYearlyReport(2025);
//
//        assertEquals(12, result.size()); // ✅ loop coverage
//    }
//
//    // ✅ YEARLY DEFAULT BRANCH (no data for any month)
//    @Test
//    void generateYearly_defaultBranchCoverage() {
//
//        when(paymentRepo.getMonthlyMoneyStats(anyInt())).thenReturn(Collections.emptyList());
//        when(bookingRepo.getMonthlyCounts(anyInt())).thenReturn(Collections.emptyList());
//        when(kpiRepo.findFirstByStartDateAndEndDate(any(), any())).thenReturn(Optional.empty());
//        when(kpiRepo.save(any())).thenReturn(report);
//
//        service.generateYearlyReport(2025);
//
//        verify(kpiRepo, atLeast(12)).save(any());
//    }
//
//    // ✅ GET ALL
//    @Test
//    void getAllReports_success() {
//
//        when(kpiRepo.findAll()).thenReturn(List.of(report));
//
//        assertFalse(service.getAllReports().isEmpty());
//    }
//
//    // ✅ GET BY ID SUCCESS
//    @Test
//    void getById_success() {
//
//        when(kpiRepo.findById(1L)).thenReturn(Optional.of(report));
//
//        assertNotNull(service.getReportById(1L));
//    }
//
//    // ✅ GET BY ID NOT FOUND
//    @Test
//    void getById_notFound() {
//
//        when(kpiRepo.findById(1L)).thenReturn(Optional.empty());
//
//        assertThrows(ResourceNotFoundException.class,
//                () -> service.getReportById(1L));
//    }
//
//    // ✅ EXISTING REPORT UPDATE (covers findFirstByStartDate)
//    @Test
//    void saveReport_existingEntityCoverage() {
//
//        when(paymentRepo.getMoneyStats(any(), any())).thenReturn(money);
//        when(bookingRepo.countBookingsInPeriod(any(), any())).thenReturn(10L);
//        when(bookingRepo.countCancellationsInPeriod(any(), any())).thenReturn(2L);
//        when(kpiRepo.findFirstByStartDateAndEndDate(any(), any())).thenReturn(Optional.of(report));
//        when(kpiRepo.save(any())).thenReturn(report);
//
//        service.generateMonthlyReport(2, 2025);
//
//        // The freshly computed report carries the existing row's id, then is upserted.
//        verify(kpiRepo).save(any());
//    }
//
//    // ✅ ZERO BOOKINGS COVERAGE (division branch)
//    @Test
//    void zeroBookings_branchCoverage() {
//
//        when(paymentRepo.getMoneyStats(any(), any())).thenReturn(money);
//        when(bookingRepo.countBookingsInPeriod(any(), any())).thenReturn(0L);
//        when(bookingRepo.countCancellationsInPeriod(any(), any())).thenReturn(0L);
//        when(kpiRepo.findFirstByStartDateAndEndDate(any(), any())).thenReturn(Optional.empty());
//        when(kpiRepo.save(any())).thenReturn(report);
//
//        service.generateMonthlyReport(3, 2025);
//    }
//
//    // ✅ NULL VALUES COVERAGE (nz method on the money fields)
//    @Test
//    void nullValueCoverage_nzMethod() {
//
//        KpiStatsDTO nullMoney = new KpiStatsDTO(null, null, null, null, null, null);
//
//        when(paymentRepo.getMoneyStats(any(), any())).thenReturn(nullMoney);
//        when(bookingRepo.countBookingsInPeriod(any(), any())).thenReturn(10L);
//        when(bookingRepo.countCancellationsInPeriod(any(), any())).thenReturn(1L);
//        when(kpiRepo.findFirstByStartDateAndEndDate(any(), any())).thenReturn(Optional.empty());
//        when(kpiRepo.save(any())).thenReturn(report);
//
//        service.generateMonthlyReport(4, 2025);
//    }
//}
