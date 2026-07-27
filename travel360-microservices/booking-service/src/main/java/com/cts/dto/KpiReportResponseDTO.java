package com.cts.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiReportResponseDTO {
    private Long reportId;
    private LocalDateTime generatedAt;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String reportLabel;
    private Double totalRevenue;
    private Long totalBookings;
    private Long totalCancellations;
    private Double cancellationRate;
    private Double averageBookingValue;
    private Double flightRevenue;
    private Double hotelRevenue;
    private Double transportRevenue;
    private Double packageRevenue;
    private Double refundedAmount;
}
