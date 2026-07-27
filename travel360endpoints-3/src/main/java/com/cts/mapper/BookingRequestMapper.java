package com.cts.mapper;

import com.cts.dto.BookingRequestResponseDTO;
import com.cts.entity.BookingRequest;
import org.springframework.stereotype.Component;

@Component
public class BookingRequestMapper {

    public BookingRequestResponseDTO toResponse(BookingRequest request) {
        if (request == null) return null;

        return BookingRequestResponseDTO.builder()
                .bookingRequestId(request.getBookingRequestId())
                .customerId(request.getCustomer() != null ? request.getCustomer().getUserId() : null)
                .agentId(request.getAgent() != null ? request.getAgent().getUserId() : null)
                .budget(request.getBudget())
                .requestDetails(request.getRequestDetails())
                .agentRemarks(request.getAgentRemarks())
                .modificationDetails(request.getModificationDetails())
                .customerStatus(request.getCustomerStatus())
                .type(request.getType())
                .status(request.getStatus())
                .linkedBookingIds(request.getLinkedBookingIds())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
