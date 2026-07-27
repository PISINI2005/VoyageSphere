package com.cts.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import com.cts.dto.ErrorResponseDTO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponseDTO> build(HttpStatus status, String message,
                                                   HttpServletRequest request) {
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .status(status.value())    
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        ex.getFieldErrors().forEach(error -> fields.put(error.getField(), error.getDefaultMessage()));
        log.warn("Validation failed: {}", fields);
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .validationErrors(fields)
                .build();
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidPassengerException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidPassenger(
            InvalidPassengerException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put(ex.getField(), ex.getMessage());
        log.warn("Passenger validation failed: {}", fields);
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .validationErrors(fields)
                .build();
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidComplaintException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidComplaint(
            InvalidComplaintException ex, HttpServletRequest request) {
        Map<String, String> fields = new LinkedHashMap<>();
        fields.put(ex.getField(), ex.getMessage());
        log.warn("Complaint validation failed: {}", fields);
        ErrorResponseDTO body = ErrorResponseDTO.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                .message("Validation failed")
                .path(request.getRequestURI())
                .validationErrors(fields)
                .build();
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleUser(UserNotFoundException ex, HttpServletRequest request) {
        log.warn("User not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(FlightNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleFlight(FlightNotFoundException ex, HttpServletRequest request) {
        log.warn("Flight not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(HotelNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleHotel(HotelNotFoundException ex, HttpServletRequest request) {
        log.warn("Hotel not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PackageNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handlePackage(PackageNotFoundException ex, HttpServletRequest request) {
        log.warn("Package not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(TransportNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleTransport(TransportNotFoundException ex, HttpServletRequest request) {
        log.warn("Transport not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvoiceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvoice(InvoiceNotFoundException ex, HttpServletRequest request) {
        log.warn("Invoice not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handlePayment(PaymentNotFoundException ex, HttpServletRequest request) {
        log.warn("Payment not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalid(InvalidBookingException ex, HttpServletRequest request) {
        log.warn("Invalid booking: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(InsufficientAvailabilityException.class)
    public ResponseEntity<ErrorResponseDTO> handleAvailability(InsufficientAvailabilityException ex, HttpServletRequest request) {
        log.warn("Insufficient availability: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(PartnerNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handlePartner(PartnerNotFoundException ex, HttpServletRequest request) {
        log.warn("Partner not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(InvalidPartnerException.class)
    public ResponseEntity<ErrorResponseDTO> handleInvalidPartner(InvalidPartnerException ex, HttpServletRequest request) {
        log.warn("Invalid partner: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponseDTO> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Throwable cause = ex.getMostSpecificCause();
        if (cause instanceof InvalidFormatException ife) {
            String fieldName = (ife.getPath() != null && !ife.getPath().isEmpty())
                ? ife.getPath().get(0).getFieldName()
                : "unknown";
            String value = ife.getValue() != null ? ife.getValue().toString() : "null";
            Class<?> targetType = ife.getTargetType();
            String typeName = targetType != null ? targetType.getSimpleName() : "unknown type";

            String acceptedValues = "";
            if (targetType != null && targetType.isEnum()) {
                acceptedValues = " Accepted values: " + java.util.Arrays.toString(targetType.getEnumConstants());
            }

            log.warn("Invalid format for field {}: expected {}, got {}", fieldName, typeName, value);
            return build(HttpStatus.BAD_REQUEST,
                String.format("Invalid value '%s' for field '%s'. Expected type: %s.%s",
                    value, fieldName, typeName, acceptedValues),
                request);
        }

        log.warn("Malformed request body: {}", cause.getMessage());
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request: check for syntax errors such as trailing commas", request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        log.warn("Access denied: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN,
                "Access denied: you do not have permission to perform this action", request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponseDTO> handleDuplicate(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "Email already exists", request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponseDTO> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        log.warn("Type mismatch for parameter {}: expected {}, got {}", ex.getName(), ex.getRequiredType(), ex.getValue());
        String message = String.format("Parameter '%s' with value '%s' is invalid. Expected type: %s",
                ex.getName(), ex.getValue(), ex.getRequiredType().getSimpleName());
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }
}
