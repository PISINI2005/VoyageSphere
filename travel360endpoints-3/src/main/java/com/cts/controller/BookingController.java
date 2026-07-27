package com.cts.controller;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.*;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.service.AuditLogService;
import com.cts.service.BookingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@AllArgsConstructor
@Tag(name = "Booking Controller", description = "Create flight/hotel/package/transport bookings and cancel bookings or individual passengers")
@Slf4j
public class BookingController {

	private final BookingService service;
	private final AuthenticatedUserProvider authUser;
	private final AuditLogService auditLogService;

	@Operation(summary = "Create a flight booking with passenger list")
	@PostMapping("/flight")
	@PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
	public ResponseEntity<BookingFlightResponseDTO> createFlightBooking(@RequestBody @Valid BookingFlightDTO dto) {

		log.info("Received request to create FLIGHT booking for userId: {}", dto.getUserId());
		auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, null, authUser.currentOrNull(),
				LogType.INFO);

		BookingFlightResponseDTO response = service.createFlightBooking(dto);

		log.info("Flight booking created successfully with bookingId: {}", response.getBookingId());

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Operation(summary = "Create a hotel booking for a date range")
	@PostMapping("/hotel")
	@PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
	public ResponseEntity<BookingHotelResponseDTO> createHotelBooking(@RequestBody @Valid BookingHotelDTO dto) {

		log.info("Received request to create HOTEL booking for userId: {}", dto.getUserId());
		auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, null, authUser.currentOrNull(),
				LogType.INFO);

		BookingHotelResponseDTO response = service.createHotelBooking(dto);

		log.info("Hotel booking created successfully with bookingId: {}", response.getBookingId());

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Operation(summary = "Create a travel package booking")
	@PostMapping("/package")
	@PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
	public ResponseEntity<BookingPackageResponseDTO> createPackageBooking(@RequestBody @Valid BookingPackageDTO dto) {

		log.info("Received request to create PACKAGE booking for userId: {}", dto.getUserId());
		auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, null, authUser.currentOrNull(),
				LogType.INFO);

		BookingPackageResponseDTO response = service.createPackageBooking(dto);

		log.info("Package booking created successfully with bookingId: {}", response.getBookingId());

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Operation(summary = "Create a transport (bus) booking with passenger list")
	@PostMapping("/transport")
	@PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
	public ResponseEntity<BookingTransportResponseDTO> createTransportBooking(
			@RequestBody @Valid BookingTransportDTO dto) {

		log.info("Received request to create TRANSPORT booking for userId: {}", dto.getUserId());
		auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, null, authUser.currentOrNull(),
				LogType.INFO);

		BookingTransportResponseDTO response = service.createTransportBooking(dto);

		log.info("Transport booking created successfully with bookingId: {}", response.getBookingId());

		return new ResponseEntity<>(response, HttpStatus.CREATED);
	}

	@Operation(summary = "Get all bookings (admin / travel-agent only)")

	@GetMapping
	@PreAuthorize("hasAnyRole('TRAVEL_AGENT','ADMIN')")
	public ResponseEntity<Page<BookingResponseDTO>> getAll(@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "5") int size) {

		Pageable pageable = PageRequest.of(page, size);

		Page<BookingResponseDTO> bookings = service.getAllBookings(pageable);

		return new ResponseEntity<>(bookings, HttpStatus.OK);
	}
	
	@GetMapping("/{bookingId}")
	@PreAuthorize("hasAnyRole('TRAVEL_AGENT','ADMIN')")
	public ResponseEntity<BookingResponseDTO> getBookingById(
	        @PathVariable Long bookingId) {

	    BookingResponseDTO booking = service.getBookingById(bookingId);

	    return new ResponseEntity<>(booking,HttpStatus.OK);
	}

	@Operation(summary = "Get bookings for a specific user, or the current user if no ID is provided")
	@GetMapping("/me")
	@PreAuthorize("hasAnyRole('CUSTOMER', 'TRAVEL_AGENT', 'ADMIN')")
	public ResponseEntity<Page<BookingResponseDTO>> getBookings(@RequestParam(required = false) Long userId,
			@RequestParam(defaultValue = "0") @Min(0) int page,
			@RequestParam(defaultValue = "5") @Min(1) @Max(100) int size) {

		log.info("Request received to fetch bookings. userId={}, page={}, size={}", userId, page, size);

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "bookingId"));

		Page<BookingResponseDTO> bookings = service.getBookings(userId, pageable);

		return ResponseEntity.ok(bookings);
	}

	@Operation(summary = "Cancel a booking with tiered refund based on days remaining")
	@PostMapping("/cancel")
	@PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
	public ResponseEntity<BookingCancelResponseDTO> cancelBooking(@RequestBody @Valid BookingCancelDTO dto) {

		log.info("Received request to cancel booking for bookingId: {}", dto.getBookingId());
		auditLogService.logAction(AuditActions.CANCEL_BOOKING, AuditEntity.BOOKING, dto.getBookingId(),
				authUser.currentOrNull(), LogType.WARN);

		BookingCancelResponseDTO response = service.deleteBooking(dto);

		log.info("Booking cancelled successfully for bookingId: {}", dto.getBookingId());
		return new ResponseEntity<>(response, HttpStatus.OK);

	}

	@Operation(summary = "Cancel a single passenger from a flight or transport booking")
	@DeleteMapping("/{bookingId}/passengers/{passengerId}")
	@PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
	public ResponseEntity<PassengerCancelResponseDTO> cancelPassenger(@PathVariable Long bookingId,
			@PathVariable Long passengerId) {

		log.info("Cancelling passengerId: {} from bookingId: {}", passengerId, bookingId);
		auditLogService.logAction(AuditActions.CANCEL_PASSENGER, AuditEntity.PASSENGER, passengerId,
				authUser.currentOrNull(), LogType.WARN);

		PassengerCancelResponseDTO response = service.cancelPassenger(bookingId, passengerId);

		log.info("Passenger cancelled successfully: passengerId={}, bookingId={}", passengerId, bookingId);

		return ResponseEntity.ok(response);
	}
}
