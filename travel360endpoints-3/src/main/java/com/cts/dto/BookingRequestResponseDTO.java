package com.cts.dto;

import com.cts.enums.BookingRequestCustomerStatus;
import com.cts.enums.BookingRequestStatus;
import com.cts.enums.BookingRequestType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestResponseDTO {
    private Long bookingRequestId;
    private Long customerId;
    private Long agentId;
    private Double budget;
    private String requestDetails;
    private String agentRemarks;
    private String modificationDetails;
    private BookingRequestCustomerStatus customerStatus;
    private BookingRequestType type;
    private BookingRequestStatus status;
    private List<Long> linkedBookingIds;
    private LocalDateTime createdAt;
}
