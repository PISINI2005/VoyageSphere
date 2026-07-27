package com.cts.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.cts.dto.PaymentDTO;
import com.cts.dto.PaymentResponseDTO;
import com.cts.entity.Invoice;
import com.cts.entity.Payment;
import com.cts.enums.PaymentStatus;

/**
 * Maps between {@link Payment} entities and DTOs. Stateless.
 */
@Component
public class PaymentMapper {

    /**
     * Builds a successful {@link Payment} for the given invoice from a payment request.
     */
    public Payment toEntity(PaymentDTO dto, Invoice invoice) {
        return Payment.builder()
                .paymentDate(LocalDateTime.now())
                .amount(dto.getAmount())
                .paymentMethod(dto.getPaymentMethod())
                .status(PaymentStatus.SUCCESS)
                .invoice(invoice)
                .build();
    }

    public PaymentResponseDTO toResponse(Payment payment) {
        return PaymentResponseDTO.builder()
        		.invoiceId(payment.getInvoice().getInvoiceId())
        		.date(payment.getPaymentDate())
                .paymentId(payment.getPaymentId())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .status(payment.getStatus())
                .build();
    }
}
