package com.cts.repository;

import com.cts.dto.KpiStatsDTO;
import com.cts.dto.MonthlyKpiStatsDTO;
import com.cts.entity.Payment;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByInvoiceInvoiceId(Long invoiceId);

    // ---------------- KPI money (the Payment ledger is the source of truth for revenue) ----------------
    // Net revenue = SUCCESS payments minus REFUNDED payments; refunds reported separately.
    // Windowed on paymentDate (when cash actually moved). Joined to booking for the per-type split.

    @Query("SELECT new com.cts.dto.KpiStatsDTO("
            + "SUM(CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END), "
            + "SUM(CASE WHEN b.bookingType = com.cts.enums.BookingType.FLIGHT THEN (CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END) ELSE 0 END), "
            + "SUM(CASE WHEN b.bookingType = com.cts.enums.BookingType.HOTEL THEN (CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END) ELSE 0 END), "
            + "SUM(CASE WHEN b.bookingType = com.cts.enums.BookingType.TRANSPORT THEN (CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END) ELSE 0 END), "
            + "SUM(CASE WHEN b.bookingType = com.cts.enums.BookingType.PACKAGE THEN (CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END) ELSE 0 END), "
            + "SUM(CASE WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN p.amount ELSE 0 END)) "
            + "FROM Payment p JOIN p.invoice i JOIN i.booking b "
            + "WHERE p.paymentDate >= :startDate AND p.paymentDate < :endDate")
    KpiStatsDTO getMoneyStats(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT new com.cts.dto.MonthlyKpiStatsDTO(MONTH(p.paymentDate), "
            + "SUM(CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END), "
            + "SUM(CASE WHEN b.bookingType = com.cts.enums.BookingType.FLIGHT THEN (CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END) ELSE 0 END), "
            + "SUM(CASE WHEN b.bookingType = com.cts.enums.BookingType.HOTEL THEN (CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END) ELSE 0 END), "
            + "SUM(CASE WHEN b.bookingType = com.cts.enums.BookingType.TRANSPORT THEN (CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END) ELSE 0 END), "
            + "SUM(CASE WHEN b.bookingType = com.cts.enums.BookingType.PACKAGE THEN (CASE WHEN p.status = com.cts.enums.PaymentStatus.SUCCESS THEN p.amount WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN -p.amount ELSE 0 END) ELSE 0 END), "
            + "SUM(CASE WHEN p.status = com.cts.enums.PaymentStatus.REFUNDED THEN p.amount ELSE 0 END)) "
            + "FROM Payment p JOIN p.invoice i JOIN i.booking b "
            + "WHERE YEAR(p.paymentDate) = :year GROUP BY MONTH(p.paymentDate) ORDER BY MONTH(p.paymentDate)")
    List<MonthlyKpiStatsDTO> getMonthlyMoneyStats(@Param("year") int year);
}
