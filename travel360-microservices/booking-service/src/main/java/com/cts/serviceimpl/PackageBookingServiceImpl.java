package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUser;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.BookingPackageDTO;
import com.cts.dto.BookingPackageResponseDTO;
import com.cts.dto.TravelPackageResponseDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import com.cts.enums.AuditEntity;
import com.cts.enums.BookingStatus;
import com.cts.enums.BookingType;
import com.cts.enums.LogType;
import com.cts.enums.NotificationCategory;
import com.cts.enums.PackageStatus;
import com.cts.enums.PaymentStatus;
import com.cts.exception.InsufficientAvailabilityException;
import com.cts.exception.InvalidBookingException;
import com.cts.mapper.BookingMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.service.AuditLogService;
import com.cts.service.NotificationService;
import com.cts.service.PackageBookingService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@AllArgsConstructor
@Slf4j
public class PackageBookingServiceImpl implements PackageBookingService {

	private final BookingRepository bookingRepo;
	private final InvoiceRepository invoiceRepo;
	private final NotificationService notificationService;
	private final AuditLogService auditLogService;
	private final BookingMapper bookingMapper;
	private final AuthenticatedUserProvider authUser;
	private final BookingHelper bookingHelper;

	@Override
	@Transactional
	public BookingPackageResponseDTO createPackageBooking(BookingPackageDTO dto) {

		log.info("Creating package booking for userId: {} and packageId: {}", dto.getUserId(), dto.getPackageId());

		// Auto-inject userId from JWT if not provided
		if (dto.getUserId() == null) {
			AuthenticatedUser currentUser = authUser.current();
			dto.setUserId(currentUser.getUserId());
			log.debug("Auto-injected userId {} from JWT for user {}", currentUser.getUserId(), currentUser.getEmail());
		}

		// Security: Users can only create bookings for themselves (TRAVEL_AGENT can book for anyone)
		authUser.assertCanActAs(dto.getUserId());

		UserResponseDTO user = bookingHelper.fetchUser(dto.getUserId());

		TravelPackageResponseDTO tpackage = bookingHelper.fetchPackage(dto.getPackageId());

		if (tpackage.getStatus() != PackageStatus.AVAILABLE) {
			log.error("Package {} is not available for booking, status: {}",
					tpackage.getPackageId(), tpackage.getStatus());
			throw new InvalidBookingException("Package is not available for booking");
		}

		int totalSlots = tpackage.getTotalSlots();
		int bookedSlots = bookingRepo.getBookedSlots(tpackage.getPackageId(), dto.getTravelDate());
		int availableSlots = totalSlots - bookedSlots;
		log.debug("Package {} availability on {}: {} slots available, {} requested",
				tpackage.getPackageId(), dto.getTravelDate(), availableSlots, dto.getUnits());
		if (availableSlots < dto.getUnits()) {
			log.error("Insufficient slots for package {} on {}: {} available, {} requested",
					tpackage.getPackageId(), dto.getTravelDate(), availableSlots, dto.getUnits());
			throw new InsufficientAvailabilityException(
					"Not enough slots available on " + dto.getTravelDate());
		}

		Booking booking = Booking.builder().userId(user.getUserId()).packageId(tpackage.getPackageId()).bookingType(BookingType.PACKAGE)
				.bookingName(dto.getBookingName()).gender(dto.getGender()).units(dto.getUnits())
				.amount(tpackage.getPrice() * dto.getUnits()).status(BookingStatus.PENDING)
				.bookingDate(dto.getTravelDate())
				.createdAt(LocalDateTime.now()).build();

		booking = bookingRepo.save(booking);
		auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, booking.getBookingId(), bookingHelper.currentUserId(), LogType.INFO);

		log.info("Package booking created successfully with bookingId: {}", booking.getBookingId());

		Invoice invoice = Invoice.builder().booking(booking).invoiceDate(LocalDateTime.now())
				.amount(booking.getAmount()).status(PaymentStatus.PENDING).build();

		invoiceRepo.save(invoice);
		notificationService.sendNotification(booking.getUserId(), "Package booked successfully. Payment Pending. Booking ID: " + booking.getBookingId(),
				NotificationCategory.BOOKING);

		return bookingMapper.toPackageResponse(booking, user, tpackage, dto);
	}
}
