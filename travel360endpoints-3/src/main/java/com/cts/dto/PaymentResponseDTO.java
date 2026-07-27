package com.cts.dto;

import java.time.LocalDateTime;

import com.cts.enums.PaymentStatus;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PaymentResponseDTO {

    private Long paymentId;
    private double amount;
    private LocalDateTime date;
    private Long invoiceId;
    private String transactionId;
    private PaymentStatus status;
}
