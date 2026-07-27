package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.BookingTransportDTO;
import com.cts.dto.BookingTransportResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Invoice;
import com.cts.entity.Transport;
import com.cts.entity.TransportSeat;
import com.cts.entity.User;
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
import com.cts.exception.TransportNotFoundException;
import com.cts.exception.UserNotFoundException;
import com.cts.mapper.BookingMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.repository.TransportRepository;
import com.cts.repository.UserRepository;
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

    private final UserRepository userRepo;
    private final TransportRepository transportRepo;
    private final BookingRepository bookingRepo;
    private final InvoiceRepository invoiceRepo;
    private final PassengerResolver passengerResolver;
    private final BookingMapper bookingMapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final AuthenticatedUserProvider authUser;
    private final BookingHelper bookingHelper;

    @Override
    @Transactional
    public BookingTransportResponseDTO createTransportBooking(BookingTransportDTO dto) {

        log.info("Creating transport booking for userId: {} and transportId: {}",
                dto.getUserId(), dto.getTransportId());

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

        // Locked load: holds a write-lock on this transport's row until the transaction commits,
        // so the availability check + booking insert below can't race with a concurrent booking.
        Transport transport = transportRepo.findByIdForUpdate(dto.getTransportId()).orElseThrow(() -> {
            log.error("Transport not found with id {}", dto.getTransportId());
            return new TransportNotFoundException("Transport not found");
        });

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
        TransportSeat seatClass = transport.getSeats().stream()
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

        Booking booking = Booking.builder().user(user).transport(transport).bookingType(BookingType.TRANSPORT)
                .bookingName(dto.getBookingName()).gender(dto.getGender()).units(dto.getUnits())
                .transportClass(transportClass)
                .amount(bookingHelper.calculateUrgencyPrice(seatClass.getPrice(), dto.getTravelDate()) * dto.getUnits()).status(BookingStatus.PENDING)
                .bookingDate(dto.getTravelDate())
                .createdAt(LocalDateTime.now())
                .build();

        booking.setPassengers(passengerResolver.resolve(dto.getPassengerProfileIds(), booking, user));
        booking = bookingRepo.save(booking);
        auditLogService.logAction(AuditActions.CREATE_BOOKING, AuditEntity.BOOKING, booking.getBookingId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Transport booking created successfully with bookingId: {}", booking.getBookingId());

        Invoice invoice = Invoice.builder().booking(booking).invoiceDate(LocalDateTime.now())
                .amount(booking.getAmount()).status(PaymentStatus.PENDING).build();

        invoiceRepo.save(invoice);
        notificationService.sendNotification(user,
                "Transport booked from " + transport.getSource() + " to " + transport.getDestination()+" Payment yet to be made.",
                NotificationCategory.BOOKING);

        return bookingMapper.toTransportResponse(booking, transport, dto);
    }
}
