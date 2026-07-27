package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Count side of one month's KPI, sourced from Booking (windowed on createdAt). Booking
 * events are counted here; the money side lives in {@link MonthlyKpiStatsDTO}. The two are
 * kept apart because joining bookings to payments would overcount the booking rows.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyKpiCountsDTO {
    private Integer month;
    private Long totalBookings;
    private Long totalCancellations;
}
