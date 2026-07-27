package com.cts.mapper;

import org.springframework.stereotype.Component;

import com.cts.dto.KpiReportResponseDTO;
import com.cts.entity.KpiReport;

/**
 * Maps {@link KpiReport} entities to their DTO representation. Stateless.
 */
@Component
public class KpiReportMapper {

    public KpiReportResponseDTO toResponse(KpiReport report) {
        return KpiReportResponseDTO.builder()
                .reportId(report.getReportId())
                .generatedAt(report.getGeneratedAt())
                .startDate(report.getStartDate())
                .endDate(report.getEndDate())
                .reportLabel(report.getReportLabel())
                .totalRevenue(report.getTotalRevenue())
                .totalBookings(report.getTotalBookings())
                .totalCancellations(report.getTotalCancellations())
                .cancellationRate(report.getCancellationRate())
                .averageBookingValue(report.getAverageBookingValue())
                .flightRevenue(report.getFlightRevenue())
                .hotelRevenue(report.getHotelRevenue())
                .transportRevenue(report.getTransportRevenue())
                .packageRevenue(report.getPackageRevenue())
                .refundedAmount(report.getRefundedAmount())
                .build();
    }
}
