package com.cts.enums;

public enum PaymentStatus {

    PENDING,

    SUCCESS,

    FAILED,

    CANCELLED,

    REFUNDED,       // the whole invoice was refunded (booking fully cancelled)

    PARTIALLY_REFUNDED   // part of the invoice was refunded (e.g. one passenger removed)
}
