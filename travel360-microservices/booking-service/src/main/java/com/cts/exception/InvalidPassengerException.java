package com.cts.exception;

/**
 * Thrown when a passenger profile's identity-document details are internally
 * inconsistent (e.g. a foreign national without a passport, or an identification
 * number that does not match its type's format). Carries the offending field name
 * so {@code GlobalExceptionHandler} can surface it in the same {@code validationErrors}
 * shape as bean-validation failures, keeping the error contract consistent for clients.
 */
public class InvalidPassengerException extends RuntimeException {

    private final String field;

    public InvalidPassengerException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
