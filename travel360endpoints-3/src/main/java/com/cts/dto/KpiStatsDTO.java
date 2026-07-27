package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Money side of a KPI period, sourced from the Payment ledger (joined to booking for
 * the per-type split). Booking counts are gathered separately and combined in the service.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KpiStatsDTO {
    private Double totalRevenue;        // net realized: SUM(SUCCESS) - SUM(REFUNDED)
    private Double flightRevenue;       // net per type
    private Double hotelRevenue;
    private Double transportRevenue;
    private Double packageRevenue;
    private Double refundedAmount;      // actual refunds paid out: SUM(REFUNDED)
}
