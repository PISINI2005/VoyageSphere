package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUser;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.BookingFlightDTO;
import com.cts.dto.BookingFlightResponseDTO;
import com.cts.dto.FlightResponseDTO;
import com.cts.dto.FlightSeatDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import com.cts.enums.AuditEntity;
import com.cts.enums.BookingStatus;
import com.cts.enums.BookingType;
import com.cts.enums.FlightStatus;
import com.cts.enums.LogType;
import com.cts.enums.NotificationCategory;
import com.cts.enums.PaymentStatus;
import com.cts.enums.SeatType;
import com.cts.exception.InsufficientAvailabilityException;
import com.cts.exception.InvalidBookingException;
import com.cts.mapper.BookingMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.service.AuditLogService;
import com.cts.service.FlightBookingService;
import com.cts.service.NotificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class FlightBookingServiceImpl implements FlightBookingService {

	private final BookingRepository bookingRepo;
	private final InvoiceRepository invoiceRepo;
	private final NotificationService notificationService;
	private final AuditLogService auditLogService;
	private final BookingMapper bookingMapper;
	private final PassengerResolver passengerResolver;
	private final AuthenticatedUserProvider authUser;
	private final BookingHelper bookingHelper;

	@Override
	@Transactional
	public BookingFlightResponseDTO createFlightBooking(BookingFlightDTO dto) {

		log.info("Creating flight booking for userId: {} and flightId: {}", dto.getUserId(), dto.getFlightId());

		// Auto-inject userId from JWT if not provided
		if (dto.getUserId() == null) {
			AuthenticatedUser currentUser = authUser.current();
			dto.setUserId(currentUser.getUserId());
			log.debug("Auto-injected userId {} from JWT for user {}", currentUser.getUserId(), currentUser.getEmail());
		}

		// Security: Users can only create bookings for themselves (TRAVEL_AGENT can book for anyone)
		authUser.assertCanActAs(dto.getUserId());

		UserResponseDTO user = bookingHelper.fetchUser(dto.getUserId());

		FlightResponseDTO flight = bookingHelper.fetchFlight(dto.getFlightId());

		if (flight.getStatus() != FlightStatus.SCHEDULED) {
			log.error("Flight {} is not available for booking, status: {}", flight.getFlightId(), flight.getStatus());
			throw new InvalidBookingException("Flight is not available for booking");
		}

		SeatType seatType = dto.getSeatType();
		FlightSeatDTO seatClass = flight.getSeats().stream()
				.filter(s -> s.getSeatType() == seatType)
				.findFirst()
				.orElseThrow(() -> {
					log.error("Seat type {} is not offered on flight {}", seatType, flight.getFlightId());
					return new InvalidBookingException("Seat type " + seatType + " is not offered on this flight");
				});

		int bookedSeats = bookingRepo.getBookedSeats(flight.getFlightId(), seatType, dto.getTravelDate());
		int availableSeats = seatClass.getTotalSeats() - bookedSeats;
		log.debug("Flight {} {} availability on {}: {} seats available, {} requested",
				flight.getFlightId(), seatType, dto.getTravelDate(), availableSeats, dto.getUnits());
		if (availableSeats < dto.getUnits()) {
			log.error("Insufficient {} seats for flight {} on {}: {} available, {} requested",
					seatType, flight.getFlightId(), dto.getTravelDate(), availableSeats, dto.getUnits());
			throw new InsufficientAvailabilityException("Not enough " + seatType + " seats available");
		}

		LocalDate today = LocalDate.now();
		LocalDate travelDate = dto.getTravelDate();

		if (!travelDate.isAfter(today.plusDays(1))) {
			log.error("Booking not allowed for flight {} on date {}", flight.getFlightId(), travelDate);
			throw new InvalidBookingException("Booking is not allowed 1 day before or on the same day of the flight");
		}

		bookingHelper.validatePassengerCount(dto.getPassengerProfileIds(), dto.getUnits());

		Booking booking = Booking.builder().userId(user.getUserId()).flightId(flight.getFlightId()).bookingType(BookingType.FLIGHT)
				.bookingName(dto.getBookingName()).gender(dto.getGender()).amount(seatClass.getPrice() * dto.getUnits())
				.seatType(seatType)
				.units(dto.getUnits()).days(1).createdAt(LocalDateTime.now()).status(BookingStatus.PENDING)
				.bookingDate(dto.getTravelDate()).build();

		booking.setPassengers(passengerResolver.resolve(dto.getPassengerProfileIds(), booking, user.getUserId()));
		bookingRepo.save(booking);
		auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, booking.getBookingId(), bookingHelper.currentUserId(), LogType.INFO);

		log.info("Flight booking created successfully with bookingId: {}", booking.getBookingId());

		Invoice invoice = Invoice.builder().booking(booking).invoiceDate(LocalDateTime.now())
				.amount(booking.getAmount()).status(PaymentStatus.PENDING).build();

		invoiceRepo.save(invoice);
		notificationService.sendNotification(booking.getUserId(), "Flight booked successfully. Payment yet to be made. Booking ID: " + booking.getBookingId(),
				NotificationCategory.BOOKING);

		return bookingMapper.toFlightResponse(booking, user, flight, dto);
	}
}
