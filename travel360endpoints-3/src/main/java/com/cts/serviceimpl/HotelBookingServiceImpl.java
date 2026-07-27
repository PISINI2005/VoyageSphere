package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.BookingHotelDTO;
import com.cts.dto.BookingHotelResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Hotel;
import com.cts.entity.HotelRoom;
import com.cts.entity.Invoice;
import com.cts.entity.User;
import com.cts.enums.AuditEntity;
import com.cts.enums.BookingStatus;
import com.cts.enums.BookingType;
import com.cts.enums.HotelRoomType;
import com.cts.enums.HotelStatus;
import com.cts.enums.LogType;
import com.cts.enums.NotificationCategory;
import com.cts.enums.PaymentStatus;
import com.cts.exception.HotelNotFoundException;
import com.cts.exception.InsufficientAvailabilityException;
import com.cts.exception.InvalidBookingException;
import com.cts.exception.UserNotFoundException;
import com.cts.mapper.BookingMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.HotelRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.repository.UserRepository;
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

    private final UserRepository userRepo;
    private final HotelRepository hotelRepo;
    private final BookingRepository bookingRepo;
    private final InvoiceRepository invoiceRepo;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final AuthenticatedUserProvider authUser;

    @Override
    @Transactional
    public BookingHotelResponseDTO createHotelBooking(BookingHotelDTO dto) {

        log.info("Creating hotel booking for userId: {} and hotelId: {}", dto.getUserId(), dto.getHotelId());

        // Auto-inject userId from JWT if not provided
        if (dto.getUserId() == null) {
            User currentUser = authUser.current();
            dto.setUserId(currentUser.getUserId());
            log.debug("Auto-injected userId {} from JWT for user {}", currentUser.getUserId(), currentUser.getEmail());
        }

        // Security: Users can only create bookings for themselves (TRAVEL_AGENT can book for anyone)
        authUser.assertCanActAs(dto.getUserId());

        User user = userRepo.findById(dto.getUserId()).orElseThrow(() -> {
            log.error("User not found with id {}", dto.getUserId());
            return new UserNotFoundException("User not found");
        });

        // Locked load: holds a write-lock on this hotel's row until the transaction commits,
        // so the availability check + booking insert below can't race with a concurrent booking.
        Hotel hotel = hotelRepo.findByIdForUpdate(dto.getHotelId()).orElseThrow(() -> {
            log.error("Hotel not found with id {}", dto.getHotelId());
            return new HotelNotFoundException("Hotel not found");
        });

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
        HotelRoom roomClass = hotel.getRooms().stream()
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

        Booking booking = Booking.builder().user(user).hotel(hotel).bookingType(BookingType.HOTEL)
                .bookingName(dto.getBookingName()).gender(dto.getGender()).units(dto.getUnits()).days((int) days)
                .roomType(roomType)
                .checkInDate(dto.getCheckInDate()).checkOutDate(dto.getCheckOutDate())
                .amount(roomClass.getPrice() * dto.getUnits() * days).status(BookingStatus.PENDING)
                .bookingDate(dto.getCheckInDate()).createdAt(LocalDateTime.now()).build();

        bookingRepo.save(booking);
        auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, booking.getBookingId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Hotel booking created successfully with bookingId: {}", booking.getBookingId());

        Invoice invoice = Invoice.builder().booking(booking).invoiceDate(LocalDateTime.now())
                .amount(booking.getAmount()).status(PaymentStatus.PENDING).build();

        invoiceRepo.save(invoice);
        notificationService.sendNotification(user, "Hotel booked successfully. Payment yet to be made. Booking ID: " + booking.getBookingId(),
                NotificationCategory.BOOKING);

        return bookingMapper.toHotelResponse(booking, hotel, dto);
    }
}
