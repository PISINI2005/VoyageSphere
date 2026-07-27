package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUser;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.BookingHotelDTO;
import com.cts.dto.BookingHotelResponseDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.dto.HotelRoomDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import com.cts.enums.AuditEntity;
import com.cts.enums.BookingStatus;
import com.cts.enums.BookingType;
import com.cts.enums.HotelRoomType;
import com.cts.enums.HotelStatus;
import com.cts.enums.LogType;
import com.cts.enums.NotificationCategory;
import com.cts.enums.PaymentStatus;
import com.cts.exception.InsufficientAvailabilityException;
import com.cts.exception.InvalidBookingException;
import com.cts.mapper.BookingMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.service.AuditLogService;
import com.cts.service.HotelBookingService;
import com.cts.service.NotificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
@Slf4j
public class HotelBookingServiceImpl implements HotelBookingService {

	private final BookingRepository bookingRepo;
	private final InvoiceRepository invoiceRepo;
	private final NotificationService notificationService;
	private final AuditLogService auditLogService;
	private final BookingMapper bookingMapper;
	private final AuthenticatedUserProvider authUser;
	private final BookingHelper bookingHelper;

	@Override
	@Transactional
	public BookingHotelResponseDTO createHotelBooking(BookingHotelDTO dto) {

		log.info("Creating hotel booking for userId: {} and hotelId: {}", dto.getUserId(), dto.getHotelId());

		// Auto-inject userId from JWT if not provided
		if (dto.getUserId() == null) {
			AuthenticatedUser currentUser = authUser.current();
			dto.setUserId(currentUser.getUserId());
			log.debug("Auto-injected userId {} from JWT for user {}", currentUser.getUserId(), currentUser.getEmail());
		}

		// Security: Users can only create bookings for themselves (TRAVEL_AGENT can book for anyone)
		authUser.assertCanActAs(dto.getUserId());

		UserResponseDTO user = bookingHelper.fetchUser(dto.getUserId());

		HotelResponseDTO hotel = bookingHelper.fetchHotel(dto.getHotelId());
		
		if(hotel.getHotelName().contains("Unknown")) {
			log.error("Hotel {} is not available for booking, status: {}", hotel.getHotelId(), hotel.getStatus());
			throw new InvalidBookingException("Hotel is not available for booking - Service is down");
		}

		if (hotel.getStatus() != HotelStatus.AVAILABLE) {
			log.error("Hotel {} is not available for booking, status: {}", hotel.getHotelId(), hotel.getStatus());
			throw new InvalidBookingException("Hotel is not available for booking");
		}

		long days = ChronoUnit.DAYS.between(dto.getCheckInDate(), dto.getCheckOutDate());
		if (days <= 0) {
			log.error("Invalid date range for hotel {}: checkIn {}, checkOut {}",
					hotel.getHotelId(), dto.getCheckInDate(), dto.getCheckOutDate());
			throw new InvalidBookingException("Check-out date must be after check-in date");
		}

		HotelRoomType roomType = dto.getRoomType();
		HotelRoomDTO roomClass = hotel.getRooms().stream()
				.filter(r -> r.getRoomType() == roomType)
				.findFirst()
				.orElseThrow(() -> {
					log.error("Room type {} is not offered by hotel {}", roomType, hotel.getHotelId());
					return new InvalidBookingException("Room type " + roomType + " is not offered by this hotel");
				});

		int bookedRooms = bookingRepo.getBookedRooms(
				hotel.getHotelId(), roomType, dto.getCheckInDate(), dto.getCheckOutDate());
		int availableRooms = roomClass.getTotalRooms() - bookedRooms;
		log.debug("Hotel {} {} availability for {} to {}: {} rooms available, {} requested",
				hotel.getHotelId(), roomType, dto.getCheckInDate(), dto.getCheckOutDate(), availableRooms, dto.getUnits());

		if (availableRooms < dto.getUnits()) {
			log.error("Insufficient {} rooms for hotel {}: {} available, {} requested",
					roomType, hotel.getHotelId(), availableRooms, dto.getUnits());
			throw new InsufficientAvailabilityException("Not enough " + roomType + " rooms available");
		}

		Booking booking = Booking.builder().userId(user.getUserId()).hotelId(hotel.getHotelId()).bookingType(BookingType.HOTEL)
				.bookingName(dto.getBookingName()).gender(dto.getGender()).units(dto.getUnits()).days((int) days)
				.roomType(roomType)
				.checkInDate(dto.getCheckInDate()).checkOutDate(dto.getCheckOutDate())
				.amount(roomClass.getPrice() * dto.getUnits() * days).status(BookingStatus.PENDING)
				.bookingDate(dto.getCheckInDate()).createdAt(LocalDateTime.now()).build();

		bookingRepo.save(booking);
		auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, booking.getBookingId(), bookingHelper.currentUserId(), LogType.INFO);

		log.info("Hotel booking created successfully with bookingId: {}", booking.getBookingId());

		Invoice invoice = Invoice.builder().booking(booking).invoiceDate(LocalDateTime.now())
				.amount(booking.getAmount()).status(PaymentStatus.PENDING).build();

		invoiceRepo.save(invoice);
		notificationService.sendNotification(booking.getUserId(), "Hotel booked successfully. Payment yet to be made. Booking ID: " + booking.getBookingId(),
				NotificationCategory.BOOKING);

		return bookingMapper.toHotelResponse(booking, user, hotel, dto);
	}
}
