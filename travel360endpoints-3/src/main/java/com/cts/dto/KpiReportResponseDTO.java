package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiReportResponseDTO {
    
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