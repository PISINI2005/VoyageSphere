package com.cts.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.cts.dto.InvoiceResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import com.cts.enums.PaymentStatus;

import lombok.extern.slf4j.Slf4j;

/**
 * Maps between {@link Invoice} entities and DTOs. Stateless.
 */
@Component
@Slf4j
public class InvoiceMapper {

    /**
     * Builds a new PENDING invoice for the given booking, billed at the booking amount.
     */
    public Invoice toEntity(Booking booking) {
        return Invoice.builder()
                .invoiceDate(LocalDateTime.now())
                .amount(booking.getAmount())
                .status(PaymentStatus.PENDING)
                .booking(booking)
                .build();
    }

    public InvoiceResponseDTO toResponse(Invoice invoice) {

        log.debug("Mapping invoice to DTO for invoiceId: {}", invoice.getInvoiceId());

        Booking booking = invoice.getBooking();

        return InvoiceResponseDTO.builder()
                .invoiceId(invoice.getInvoiceId())
                .amount(invoice.getAmount())
                .status(invoice.getStatus())
                .bookingId(booking != null ? booking.getBookingId() : null)
                .userId(booking != null ? booking.getUserId() : null)
                .build();
    }
}
