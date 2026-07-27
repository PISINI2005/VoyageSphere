package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enums.*;
import com.cts.exception.*;
import com.cts.entity.PassengerProfile;
import com.cts.mapper.BookingMapper;
import com.cts.mapper.PassengerMapper;
import com.cts.repository.*;
import com.cts.service.AuditLogService;
import com.cts.service.NotificationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransportBookingServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private TransportRepository transportRepo;
    @Mock private BookingRepository bookingRepo;
    @Mock private InvoiceRepository invoiceRepo;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogService auditLogService;
    @Mock private AuthenticatedUserProvider authUser;
    @Mock private PassengerProfileRepository profileRepo;
    private PassengerResolver passengerResolver;
    private PassengerMapper passengerMapper;
    private BookingMapper bookingMapper;
    private BookingHelper bookingHelper;
    private TransportBookingServiceImpl service;

    private User user;
    private Transport transport;
    private PassengerProfile profile;
    private BookingTransportDTO dto;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@mail.com");

        transport = Transport.builder()
                .transportId(40L)
                .source("Chennai")
                .destination("Bangalore")
                .transportStatus(TransportStatus.AVAILABLE)
                .departureTime(LocalTime.of(6, 0))
                .arrivalTime(LocalTime.of(12, 0))
                .build();
        transport.setSeats(new ArrayList<>(List.of(
                TransportSeat.builder()
                        .transportClass(TransportClass.SEATER)
                        .price(800.0)
                        .totalSeats(40)
                        .transport(transport)
                        .build())));

        passengerMapper = new PassengerMapper();
        passengerResolver = new PassengerResolver(profileRepo);
        bookingMapper = new BookingMapper(passengerMapper);
        bookingHelper = new BookingHelper();
        service = new TransportBookingServiceImpl(userRepo, transportRepo, bookingRepo, invoiceRepo,
                passengerResolver, bookingMapper, notificationService, auditLogService, authUser, bookingHelper);

        profile = PassengerProfile.builder()
                .passengerProfileId(5L)
                .passengerName("John")
                .dateOfBirth(LocalDate.of(1995, 1, 1))
                .gender(Gender.MALE)
                .contactNo("9876543210")
                .emailAddress("john@mail.com")
                .nationality(Nationality.INDIAN)
                .identificationType(IdentificationType.AADHAAR)
                .identificationNumber("123456789012")
                .status(PassengerProfileStatus.ACTIVE)
                .user(user)
                .build();

        dto = new BookingTransportDTO();
        dto.setUserId(1L);
        dto.setTransportId(40L);
        dto.setTravelDate(LocalDate.now().plusDays(10));
        dto.setUnits(1);
        dto.setTransportClass(TransportClass.SEATER);
        dto.setBookingName("John");
        dto.setGender(Gender.MALE);
        dto.setPassengerProfileIds(List.of(5L));
    }

    @Test
    void createTransportBooking_success() {

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(transportRepo.findByIdForUpdate(40L)).thenReturn(Optional.of(transport));
        when(bookingRepo.getBookedTransportSeats(40L, TransportClass.SEATER, dto.getTravelDate())).thenReturn(0);
        when(profileRepo.findByPassengerProfileIdAndUserUserId(5L, 1L)).thenReturn(Optional.of(profile));
        when(bookingRepo.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1L);
            return b;
        });

        BookingTransportResponseDTO response = service.createTransportBooking(dto);

        assertNotNull(response);
        assertEquals(BookingType.TRANSPORT, response.getBookingType());
        assertEquals(800.0, response.getAmount());
        verify(invoiceRepo).save(any());
        verify(notificationService).sendNotification(any(), anyString(), any());
    }

    @Test
    void createTransportBooking_userNotFound() {
        dto.setUserId(99L);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.createTransportBooking(dto));
    }

    @Test
    void createTransportBooking_transportNotFound() {
        dto.setTransportId(99L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(transportRepo.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(TransportNotFoundException.class, () -> service.createTransportBooking(dto));
    }

    @Test
    void createTransportBooking_transportNotAvailable() {
        transport.setTransportStatus(TransportStatus.OUT_OF_SERVICE);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(transportRepo.findByIdForUpdate(40L)).thenReturn(Optional.of(transport));
        assertThrows(InvalidBookingException.class, () -> service.createTransportBooking(dto));
    }

    @Test
    void createTransportBooking_travelDateTooSoon() {
        dto.setTravelDate(LocalDate.now()); // same day — not allowed
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(transportRepo.findByIdForUpdate(40L)).thenReturn(Optional.of(transport));
        assertThrows(InvalidBookingException.class, () -> service.createTransportBooking(dto));
    }

    @Test
    void createTransportBooking_classNotOffered() {
        dto.setTransportClass(TransportClass.SLEEPER);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(transportRepo.findByIdForUpdate(40L)).thenReturn(Optional.of(transport));
        assertThrows(InvalidBookingException.class, () -> service.createTransportBooking(dto));
    }

    @Test
    void createTransportBooking_insufficientSeats() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(transportRepo.findByIdForUpdate(40L)).thenReturn(Optional.of(transport));
        when(bookingRepo.getBookedTransportSeats(40L, TransportClass.SEATER, dto.getTravelDate())).thenReturn(40);
        assertThrows(InsufficientAvailabilityException.class, () -> service.createTransportBooking(dto));
    }

    @Test
    void createTransportBooking_passengerCountMismatch() {
        dto.setUnits(2); // 2 seats but only 1 passenger in list
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(transportRepo.findByIdForUpdate(40L)).thenReturn(Optional.of(transport));
        when(bookingRepo.getBookedTransportSeats(40L, TransportClass.SEATER, dto.getTravelDate())).thenReturn(0);
        assertThrows(InvalidBookingException.class, () -> service.createTransportBooking(dto));
    }
}
