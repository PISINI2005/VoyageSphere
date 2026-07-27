package com.cts.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.BookingRequestCreateDTO;
import com.cts.dto.BookingRequestFeedbackDTO;
import com.cts.dto.BookingRequestRejectDTO;
import com.cts.dto.BookingRequestResponseDTO;
import com.cts.dto.BookingRequestSubmitDTO;
import com.cts.entity.User;
import com.cts.enums.BookingRequestStatus;
import com.cts.service.BookingRequestService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/booking-requests")
@AllArgsConstructor
@Tag(name = "Booking Request Controller", description = "Manage customer booking and cancellation requests")
@Slf4j
public class BookingRequestController {

    private final BookingRequestService service;
    private final AuthenticatedUserProvider authUser;

    @Operation(summary = "Get all booking requests with optional status filter")
    @GetMapping
    @PreAuthorize("hasRole('TRAVEL_AGENT') or hasRole('ADMIN')")
    public ResponseEntity<Page<BookingRequestResponseDTO>> getAllRequests(
            @RequestParam(required = false) BookingRequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getRequests(status, pageable));
    }

    @Operation(summary = "Create a new booking or cancellation request")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingRequestResponseDTO> createRequest(@RequestBody @Valid BookingRequestCreateDTO dto) {
        User customer = authUser.current();
        log.info("Customer {} is creating a booking request", customer.getUserId());
        return new ResponseEntity<>(service.createRequest(dto, customer), HttpStatus.CREATED);
    }

    @Operation(summary = "Get all pending requests (Travel Agents only)")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('TRAVEL_AGENT')")
    public ResponseEntity<Page<BookingRequestResponseDTO>> getPending(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getPendingRequests(pageable));
    }

    @Operation(summary = "Get details of a specific request")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingRequestResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getRequestById(id));
    }

    @Operation(summary = "Claim a pending request")
    @PatchMapping("/{id}/claim")
    @PreAuthorize("hasRole('TRAVEL_AGENT')")
    public ResponseEntity<BookingRequestResponseDTO> claim(@PathVariable Long id) {
        User agent = authUser.current();
        log.info("Agent {} is claiming request {}", agent.getUserId(), id);
        return ResponseEntity.ok(service.claimRequest(id, agent));
    }

    @Operation(summary = "Accept a claimed request and notify customer")
    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasRole('TRAVEL_AGENT')")
    public ResponseEntity<BookingRequestResponseDTO> accept(@PathVariable Long id) {
        User agent = authUser.current();
        log.info("Agent {} is accepting request {}", agent.getUserId(), id);
        return ResponseEntity.ok(service.acceptRequest(id, agent));
    }

    @Operation(summary = "Submit the final booking results for a request")
    @PatchMapping("/{id}/submit")
    @PreAuthorize("hasRole('TRAVEL_AGENT')")
    public ResponseEntity<BookingRequestResponseDTO> submit(@PathVariable Long id, @RequestBody @Valid BookingRequestSubmitDTO dto) {
        User agent = authUser.current();
        log.info("Agent {} is submitting fulfillment for request {}", agent.getUserId(), id);
        return ResponseEntity.ok(service.submitFulfillment(id, dto, agent));
    }

    @Operation(summary = "Provide feedback on a submitted request")
    @PatchMapping("/{id}/feedback")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<BookingRequestResponseDTO> feedback(@PathVariable Long id, @RequestBody @Valid BookingRequestFeedbackDTO dto) {
        User customer = authUser.current();
        log.info("Customer {} is providing feedback for request {}", customer.getUserId(), id);
        return ResponseEntity.ok(service.provideFeedback(id, dto, customer));
    }

    @Operation(summary = "Get my booking requests")
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Page<BookingRequestResponseDTO>> getMyRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User customer = authUser.current();
        Pageable pagebase = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getMyRequests(customer.getUserId(), pagebase));
    }

    @Operation(summary = "Get booking requests assigned to the current agent")
    @GetMapping("/agent/me")
    @PreAuthorize("hasRole('TRAVEL_AGENT')")
    public ResponseEntity<Page<BookingRequestResponseDTO>> getMyAgentRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        User agent = authUser.current();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.getAgentRequests(agent.getUserId(), pageable));
    }

    @Operation(summary = "Reject a claimed request with a reason")
    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('TRAVEL_AGENT')")
    public ResponseEntity<BookingRequestResponseDTO> reject(@PathVariable Long id, @RequestBody @Valid BookingRequestRejectDTO dto) {
        User agent = authUser.current();
        log.info("Agent {} is rejecting request {} with reason: {}", agent.getUserId(), id, dto.getRemarks());
        return ResponseEntity.ok(service.rejectRequest(id, dto, agent));
    }
}
