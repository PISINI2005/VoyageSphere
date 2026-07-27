package com.cts.exception;

/**
 * Thrown when a complaint's target reference is invalid — e.g. a target type without
 * an id (or vice versa). Carries the offending field name so {@code GlobalExceptionHandler}
 * can surface it in the same {@code validationErrors} shape as bean-validation failures.
 * (A missing/unauthorized target is reported via ResourceNotFoundException / AccessDeniedException.)
 */
public class InvalidComplaintException extends RuntimeException {

    private final String field;

    public InvalidComplaintException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
