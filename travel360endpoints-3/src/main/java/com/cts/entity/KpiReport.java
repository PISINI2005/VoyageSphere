package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kpi_report",
        uniqueConstraints = @UniqueConstraint(name = "uk_kpi_report_period",
                columnNames = {"start_date", "end_date"}))
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reportId;

    private LocalDateTime generatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reportLabel; // e.g., "Monthly - 2026-06" or "Custom Range"

    private Double totalRevenue;
    private Long totalBookings;
    private Long totalCancellations;
    private Double cancellationRate;
    private Double averageBookingValue;

    private Double flightRevenue;
    private Double hotelRevenue;
    private Double transportRevenue;
    private Double packageRevenue;
    private Double refundedAmount;   // actual money refunded in the period (was named cancelledRevenue)
}
