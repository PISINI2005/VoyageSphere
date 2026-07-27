package com.cts.serviceimpl;

import com.cts.dto.BookingCancelDTO;
import com.cts.dto.BookingCancelResponseDTO;
import com.cts.dto.BookingFlightDTO;
import com.cts.dto.BookingFlightResponseDTO;
import com.cts.dto.BookingHotelDTO;
import com.cts.dto.BookingHotelResponseDTO;
import com.cts.dto.BookingPackageDTO;
import com.cts.dto.BookingPackageResponseDTO;
import com.cts.dto.BookingResponseDTO;
import com.cts.dto.BookingTransportDTO;
import com.cts.dto.BookingTransportResponseDTO;
import com.cts.dto.PassengerCancelResponseDTO;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.entity.*;
import com.cts.enums.AuditEntity;
import com.cts.enums.BookingStatus;
import com.cts.enums.BookingType;
import com.cts.enums.NotificationCategory;
import com.cts.enums.PassengerStatus;
import com.cts.enums.LogType;
import com.cts.enums.PaymentStatus;
import com.cts.exception.InvalidBookingException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.BookingMapper;
import com.cts.repository.*;
import com.cts.service.AuditLogService;
import com.cts.service.BookingService;
import com.cts.service.FlightBookingService;
import com.cts.service.HotelBookingService;
import com.cts.service.NotificationService;
import com.cts.service.PackageBookingService;
import com.cts.service.TransportBookingService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class BookingServiceImpl implements BookingService {

	private final BookingRepository bookingRepo;
	private final InvoiceRepository invoiceRepo;
	private final PaymentRepository paymentRepo;
	private final PassengerRepository passengerRepo;
	private final AuthenticatedUserProvider authUser;
	private final AuditLogService auditLogService;
	private final BookingMapper bookingMapper;
	private final NotificationService notificationService;
	private final BookingHelper bookingHelper;
	private final FlightBookingService flightBookingService;
	private final HotelBookingService hotelBookingService;
	private final TransportBookingService transportBookingService;
	private final PackageBookingService packageBookingService;

	@Override
	@Transactional
	public BookingFlightResponseDTO createFlightBooking(BookingFlightDTO dto) {
		return flightBookingService.createFlightBooking(dto);
	}

	@Override
	@Transactional
	public BookingHotelResponseDTO createHotelBooking(BookingHotelDTO dto) {
		return hotelBookingService.createHotelBooking(dto);
	}

	@Override
	@Transactional
	public BookingPackageResponseDTO createPackageBooking(BookingPackageDTO dto) {
		return packageBookingService.createPackageBooking(dto);
	}

	@Override
	@Transactional
	public BookingTransportResponseDTO createTransportBooking(BookingTransportDTO dto) {
		return transportBookingService.createTransportBooking(dto);
	}

	@Override
	public List<BookingResponseDTO> getBookingsByUser(Long userId) {
		authUser.assertCanActAs(userId);
		log.info("Fetching bookings for userId: {}", userId);
		List<Booking> list = bookingRepo.findByUserId(userId);
		log.info("Found {} bookings for userId: {}", list.size(), userId);
		return list.stream().map(bookingMapper::toResponse).collect(Collectors.toList());
	}

	@Override
	public List<BookingResponseDTO> getMyBookings() {
		return getBookingsByUser(authUser.current().getUserId());
	}

	@Override
	public List<BookingResponseDTO> getAllBookings() {
		log.info("Fetching all bookings");
		List<Booking> list = bookingRepo.findAll();
		log.info("Total bookings fetched: {}", list.size());
		return list.stream().map(bookingMapper::toResponse).collect(Collectors.toList());
	}

	@Override
	@Transactional
	public BookingCancelResponseDTO deleteBooking(BookingCancelDTO dto) {

		log.info("Cancelling booking with bookingId: {} for userId: {}", dto.getBookingId(), dto.getUserId());

		Booking booking = bookingRepo.findById(dto.getBookingId())
				.orElseThrow(() -> {
					log.error("Booking not found with id {}", dto.getBookingId());
					return new ResourceNotFoundException("Booking not found");
				});

		authUser.assertCanActAs(booking.getUserId());

		if (booking.getStatus() == BookingStatus.CANCELLED) {
			log.error("Booking {} is already cancelled", dto.getBookingId());
			throw new InvalidBookingException("Booking is already cancelled");
		}

		if (booking.getBookingDate() != null && booking.getBookingDate().isBefore(LocalDate.now())) {
			log.error("Booking {} travel date {} has passed; cancellation not allowed",
					booking.getBookingId(), booking.getBookingDate());
			throw new InvalidBookingException("Cancellation not allowed: travel date has already passed");
		}

		LocalDateTime now = LocalDateTime.now();
		double refundAmount = 0.0;
		String refundStatus = "NONE";

		if (booking.getStatus() == BookingStatus.PENDING) {
			log.debug("Booking {} is PENDING, cancelling with no refund", booking.getBookingId());
			booking.setStatus(BookingStatus.CANCELLED);
			bookingRepo.save(booking);
			auditLogService.logAction(AuditActions.CANCEL_BOOKING, AuditEntity.BOOKING, booking.getBookingId(), bookingHelper.currentUserId(), LogType.INFO);

			// No payment was made: void the still-unpaid invoice(s) so they are not
			// left dangling as an outstanding (PENDING) bill on a cancelled booking.
			invoiceRepo.findByBookingBookingId(booking.getBookingId()).stream()
					.filter(inv -> inv.getStatus() == PaymentStatus.PENDING)
					.forEach(inv -> {
						inv.setStatus(PaymentStatus.CANCELLED);
						invoiceRepo.save(inv);
					});

			notificationService.sendNotification(booking.getUserId(),
					"Booking cancelled (no payment made). Booking ID: " + booking.getBookingId(),
					NotificationCategory.BOOKING);

			log.info("Booking {} cancelled successfully (no payment made)", booking.getBookingId());

			return BookingCancelResponseDTO.builder().bookingId(booking.getBookingId())
					.userId(booking.getUserId()).status(booking.getStatus())
					.originalAmount(booking.getAmount()).refundAmount(0.0).deductionAmount(booking.getAmount())
					.bookingDate(booking.getBookingDate()).cancelledAt(now).refundStatus("NONE")
					.message("Booking cancelled successfully (no payment made)").build();
		}

		if (booking.getStatus() == BookingStatus.CONFIRMED) {
			refundAmount = bookingHelper.calculateRefundAmount(booking.getAmount(), booking.getBookingDate());
			log.debug("Booking {} is CONFIRMED, calculated refund amount: {}", booking.getBookingId(), refundAmount);

			refundStatus = bookingHelper.refundStatus(refundAmount, booking.getAmount());

			booking.setStatus(BookingStatus.CANCELLED);
			bookingRepo.save(booking);
			auditLogService.logAction(AuditActions.CANCEL_BOOKING, AuditEntity.BOOKING, booking.getBookingId(), bookingHelper.currentUserId(), LogType.INFO);

			if (refundAmount > 0) {
				// Whole booking cancelled -> the paid invoice is fully refunded.
				recordRefund(booking, refundAmount, PaymentStatus.REFUNDED, now);
			}

			notificationService.sendNotification(booking.getUserId(),
					"Booking cancelled. Refund amount: " + refundAmount + " | Booking ID: " + booking.getBookingId(),
					NotificationCategory.BOOKING);

			log.info("Booking {} cancelled successfully with refund amount: {} (status: {})",
					booking.getBookingId(), refundAmount, refundStatus);

			return BookingCancelResponseDTO.builder().bookingId(booking.getBookingId())
					.userId(booking.getUserId()).status(booking.getStatus())
					.originalAmount(booking.getAmount()).refundAmount(refundAmount)
					.deductionAmount(booking.getAmount() - refundAmount).bookingDate(booking.getBookingDate())
					.cancelledAt(now).refundStatus(refundStatus).message("Booking cancelled successfully").build();
		}

		log.error("Invalid booking state for booking {}: {}", booking.getBookingId(), booking.getStatus());
		throw new InvalidBookingException("Invalid booking state");
	}

	@Override
	@Transactional
	public PassengerCancelResponseDTO cancelPassenger(Long bookingId, Long passengerId, Long userId) {

		log.info("Cancelling passengerId: {} from bookingId: {} for userId: {}", passengerId, bookingId, userId);

		Booking booking = bookingRepo.findById(bookingId)
				.orElseThrow(() -> {
					log.error("Booking not found with id {}", bookingId);
					return new ResourceNotFoundException("Booking not found");
				});

		authUser.assertCanActAs(booking.getUserId());

		if (booking.getStatus() == BookingStatus.CANCELLED) {
			log.error("Booking {} is already cancelled", bookingId);
			throw new InvalidBookingException("Booking is already cancelled");
		}

		if (booking.getBookingDate() != null && booking.getBookingDate().isBefore(LocalDate.now())) {
			log.error("Booking {} travel date {} has passed; cancellation not allowed",
					bookingId, booking.getBookingDate());
			throw new InvalidBookingException("Cancellation not allowed: travel date has already passed");
		}

		if (booking.getBookingType() != BookingType.FLIGHT && booking.getBookingType() != BookingType.TRANSPORT) {
			log.error("Passenger cancellation not allowed for booking {} of type {}",
					bookingId, booking.getBookingType());
			throw new InvalidBookingException(
					"Passenger cancellation is only allowed for flight and transport bookings");
		}

		Passenger passenger = passengerRepo.findById(passengerId)
				.orElseThrow(() -> {
					log.error("Passenger not found with id {}", passengerId);
					return new ResourceNotFoundException("Passenger not found");
				});

		if (passenger.getBooking() == null || !passenger.getBooking().getBookingId().equals(bookingId)) {
			log.error("Passenger {} does not belong to booking {}", passengerId, bookingId);
			throw new InvalidBookingException("Passenger does not belong to the given booking");
		}

		if (passenger.getStatus() == PassengerStatus.CANCELLED) {
			log.error("Passenger {} is already cancelled", passengerId);
			throw new InvalidBookingException("Passenger is already cancelled");
		}

		LocalDateTime now = LocalDateTime.now();
		long activePassengers = passengerRepo.countByBookingBookingIdAndStatus(bookingId, PassengerStatus.ACTIVE);

		if (activePassengers <= 1) {
			log.debug("Last active passenger in booking {}, cancelling entire booking", bookingId);
			BookingCancelDTO cancelDto = new BookingCancelDTO();
			cancelDto.setBookingId(bookingId);
			cancelDto.setUserId(userId);
			BookingCancelResponseDTO full = deleteBooking(cancelDto);

			passenger.setStatus(PassengerStatus.CANCELLED);
			passengerRepo.save(passenger);
			auditLogService.logAction(AuditActions.CANCEL_PASSENGER, AuditEntity.PASSENGER, passengerId, bookingHelper.currentUserId(), LogType.INFO);

			log.info("Last passenger {} removed; booking {} cancelled entirely", passengerId, bookingId);

			return PassengerCancelResponseDTO.builder().bookingId(bookingId).passengerId(passengerId)
					.passengerName(passenger.getProfile().getPassengerName()).bookingStatus(BookingStatus.CANCELLED)
					.remainingUnits(0).refundAmount(full.getRefundAmount()).deductionAmount(full.getDeductionAmount())
					.refundStatus(full.getRefundStatus()).cancelledAt(now)
					.message("Last passenger removed; entire booking cancelled").build();
		}

		double perSeat = booking.getAmount() / booking.getUnits();
		double refundAmount = 0.0;
		String refundStatus = "NONE";

		if (booking.getStatus() == BookingStatus.CONFIRMED) {
			refundAmount = bookingHelper.calculateRefundAmount(perSeat, booking.getBookingDate());
			refundStatus = bookingHelper.refundStatus(refundAmount, perSeat);
		}

		booking.setUnits(booking.getUnits() - 1);
		booking.setAmount(booking.getAmount() - perSeat);
		bookingRepo.save(booking);

		// Keep the original (still unpaid) invoice in sync with the reduced booking
		// amount. A paid SUCCESS invoice is left intact as an audit record of money
		// already collected; that giveback is captured by the REFUNDED invoice below.
		invoiceRepo.findByBookingBookingId(bookingId).stream()
				.filter(inv -> inv.getStatus() == PaymentStatus.PENDING)
				.findFirst()
				.ifPresent(inv -> {
					inv.setAmount(booking.getAmount());
					invoiceRepo.save(inv);
				});

		passenger.setStatus(PassengerStatus.CANCELLED);
		passengerRepo.save(passenger);
		auditLogService.logAction(AuditActions.CANCEL_PASSENGER, AuditEntity.PASSENGER, passengerId, bookingHelper.currentUserId(), LogType.INFO);

		if (refundAmount > 0) {
			// One passenger removed but the booking lives on -> the invoice is partially refunded.
			recordRefund(booking, refundAmount, PaymentStatus.PARTIALLY_REFUNDED, now);
		}

		notificationService.sendNotification(booking.getUserId(), "Passenger " + passenger.getProfile().getPassengerName()
				+ " removed from booking " + bookingId + ". Refund: " + refundAmount, NotificationCategory.BOOKING);

		log.info("Passenger {} removed from booking {} successfully. Refund: {} (status: {})",
				passengerId, bookingId, refundAmount, refundStatus);

		return PassengerCancelResponseDTO.builder().bookingId(bookingId).passengerId(passengerId)
				.passengerName(passenger.getProfile().getPassengerName()).bookingStatus(booking.getStatus())
				.remainingUnits(booking.getUnits()).refundAmount(refundAmount).deductionAmount(perSeat - refundAmount)
				.refundStatus(refundStatus).cancelledAt(now).message("Passenger removed from booking").build();
	}

	/**
	 * Records a refund against the booking's existing paid invoice rather than creating a
	 * separate "refund invoice". This keeps one invoice per booking (so revenue can be read
	 * from invoices without double-counting) and makes the original invoice self-describing.
	 * The actual refunded amount is captured on a {@link Payment} ({@code REFUNDED}), and the
	 * invoice's status is set to {@code invoiceStatus} (REFUNDED when the whole booking is
	 * cancelled, PARTIALLY_REFUNDED when only a passenger is removed).
	 */
	private void recordRefund(Booking booking, double refundAmount, PaymentStatus invoiceStatus, LocalDateTime now) {
		Invoice paidInvoice = invoiceRepo.findByBookingBookingId(booking.getBookingId()).stream()
				.filter(inv -> inv.getStatus() == PaymentStatus.SUCCESS)
				.findFirst()
				.orElse(null);

		if (paidInvoice == null) {
			// A CONFIRMED booking is expected to have a paid invoice; if not, don't block the
			// refund — still record the payment so the cancellation can complete.
			log.warn("No paid (SUCCESS) invoice found for booking {} while refunding {}; recording payment only",
					booking.getBookingId(), refundAmount);
		} else {
			paidInvoice.setStatus(invoiceStatus);
			invoiceRepo.save(paidInvoice);
		}

		Payment refundPayment = Payment.builder().invoice(paidInvoice).amount(refundAmount)
				.status(PaymentStatus.REFUNDED).paymentDate(now).paymentMethod("UPI").build();
		paymentRepo.save(refundPayment);
	}
}
