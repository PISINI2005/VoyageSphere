package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUser;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.BookingTransportDTO;
import com.cts.dto.BookingTransportResponseDTO;
import com.cts.dto.TransportResponseDTO;
import com.cts.dto.TransportSeatDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import com.cts.enums.AuditEntity;
import com.cts.enums.BookingStatus;
import com.cts.enums.BookingType;
import com.cts.enums.LogType;
import com.cts.enums.NotificationCategory;
import com.cts.enums.PaymentStatus;
import com.cts.enums.TransportClass;
import com.cts.enums.TransportStatus;
import com.cts.exception.InsufficientAvailabilityException;
import com.cts.exception.InvalidBookingException;
import com.cts.mapper.BookingMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.service.AuditLogService;
import com.cts.service.NotificationService;
import com.cts.service.TransportBookingService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class TransportBookingServiceImpl implements TransportBookingService {

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
	public BookingTransportResponseDTO createTransportBooking(BookingTransportDTO dto) {

		log.info("Creating transport booking for userId: {} and transportId: {}",
				dto.getUserId(), dto.getTransportId());

		// Auto-inject userId from JWT if not provided
		if (dto.getUserId() == null) {
			AuthenticatedUser currentUser = authUser.current();
			dto.setUserId(currentUser.getUserId());
			log.debug("Auto-injected userId {} from JWT for user {}", currentUser.getUserId(), currentUser.getEmail());
		}

		// Security: Users can only create bookings for themselves (TRAVEL_AGENT can book for anyone)
		authUser.assertCanActAs(dto.getUserId());

		UserResponseDTO user = bookingHelper.fetchUser(dto.getUserId());

		TransportResponseDTO transport = bookingHelper.fetchTransport(dto.getTransportId());

		if (transport.getTransportStatus() != TransportStatus.AVAILABLE) {
			log.error("Transport {} is not available for booking, status: {}",
					transport.getTransportId(), transport.getTransportStatus());
			throw new InvalidBookingException("Transport is not available for booking");
		}

		LocalDate today = LocalDate.now();
		LocalDate travelDate = dto.getTravelDate();

		if (!travelDate.isAfter(today.plusDays(1))) {
			log.error("Booking not allowed for transport {} on date {}", transport.getTransportId(), travelDate);
			throw new InvalidBookingException("Booking is not allowed 1 day before or on the same day of travel");
		}

		TransportClass transportClass = dto.getTransportClass();
		TransportSeatDTO seatClass = transport.getSeats().stream()
				.filter(s -> s.getTransportClass() == transportClass)
				.findFirst()
				.orElseThrow(() -> {
					log.error("Class {} is not offered on transport {}", transportClass, transport.getTransportId());
					return new InvalidBookingException("Class " + transportClass + " is not offered on this transport");
				});

		int bookedSeats = bookingRepo.getBookedTransportSeats(transport.getTransportId(), transportClass, dto.getTravelDate());
		int availableSeats = seatClass.getTotalSeats() - bookedSeats;
		log.debug("Transport {} {} availability on {}: {} seats available, {} requested",
				transport.getTransportId(), transportClass, dto.getTravelDate(), availableSeats, dto.getUnits());
		if (availableSeats < dto.getUnits()) {
			log.error("Insufficient {} seats for transport {} on {}: {} available, {} requested",
					transportClass, transport.getTransportId(), dto.getTravelDate(), availableSeats, dto.getUnits());
			throw new InsufficientAvailabilityException("Not enough " + transportClass + " seats available");
		}

		bookingHelper.validatePassengerCount(dto.getPassengerProfileIds(), dto.getUnits());

		Booking booking = Booking.builder().userId(user.getUserId()).transportId(transport.getTransportId()).bookingType(BookingType.TRANSPORT)
				.bookingName(dto.getBookingName()).gender(dto.getGender()).units(dto.getUnits())
				.transportClass(transportClass)
				.amount(seatClass.getPrice() * dto.getUnits()).status(BookingStatus.PENDING)
				.bookingDate(dto.getTravelDate())
				.createdAt(LocalDateTime.now())
				.build();

		booking.setPassengers(passengerResolver.resolve(dto.getPassengerProfileIds(), booking, user.getUserId()));
		booking = bookingRepo.save(booking);
		auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, booking.getBookingId(), bookingHelper.currentUserId(), LogType.INFO);

		log.info("Transport booking created successfully with bookingId: {}", booking.getBookingId());

		Invoice invoice = Invoice.builder().booking(booking).invoiceDate(LocalDateTime.now())
				.amount(booking.getAmount()).status(PaymentStatus.PENDING).build();

		invoiceRepo.save(invoice);

		notificationService.sendNotification(booking.getUserId(),
				"Transport booked from " + transport.getSource() + " to " + transport.getDestination() + " Payment yet to be made.",
				NotificationCategory.BOOKING);

		return bookingMapper.toTransportResponse(booking, user, transport, dto);
	}
}
