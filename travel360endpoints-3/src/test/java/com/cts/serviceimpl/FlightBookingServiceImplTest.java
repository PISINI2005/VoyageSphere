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
class FlightBookingServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private FlightRepository flightRepo;
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
    private FlightBookingServiceImpl service;

    private User user;
    private Flight flight;
    private PassengerProfile profile;
    private BookingFlightDTO dto;

    @BeforeEach
    void setup() {
        user = new User();
        user.setUserId(1L);
        user.setEmail("test@mail.com");

        flight = Flight.builder()
                .flightId(10L)
                .flightNumber("AA-100")
                .source("Chennai")
                .destination("Delhi")
                .status(FlightStatus.SCHEDULED)
                .departureTime(LocalTime.of(8, 0))
                .arrivalTime(LocalTime.of(10, 0))
                .build();
        flight.setSeats(new ArrayList<>(List.of(
                FlightSeat.builder()
                        .seatType(SeatType.ECONOMY)
                        .price(5000.0)
                        .totalSeats(100)
                        .flight(flight)
                        .build())));

        passengerMapper = new PassengerMapper();
        passengerResolver = new PassengerResolver(profileRepo);
        bookingMapper = new BookingMapper(passengerMapper);
        bookingHelper = new BookingHelper();
        service = new FlightBookingServiceImpl(userRepo, flightRepo, bookingRepo, invoiceRepo,
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

        dto = new BookingFlightDTO();
        dto.setUserId(1L);
        dto.setFlightId(10L);
        dto.setTravelDate(LocalDate.now().plusDays(10));
        dto.setUnits(1);
        dto.setSeatType(SeatType.ECONOMY);
        dto.setBookingName("John");
        dto.setGender(Gender.MALE);
        dto.setPassengerProfileIds(List.of(5L));
    }

    @Test
    void createFlightBooking_success() {

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(bookingRepo.getBookedSeats(10L, SeatType.ECONOMY, dto.getTravelDate())).thenReturn(0);
        when(profileRepo.findByPassengerProfileIdAndUserUserId(5L, 1L)).thenReturn(Optional.of(profile));
        when(bookingRepo.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1L);
            return b;
        });

        BookingFlightResponseDTO response = service.createFlightBooking(dto);

        assertNotNull(response);
        assertEquals(BookingType.FLIGHT, response.getBookingType());
        assertEquals(5000.0, response.getAmount());
        verify(invoiceRepo).save(any());
        verify(notificationService).sendNotification(any(), anyString(), any());
    }

    @Test
    void createFlightBooking_profileNotFound_throwsResourceNotFound() {
        dto.setPassengerProfileIds(List.of(999L));

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(bookingRepo.getBookedSeats(10L, SeatType.ECONOMY, dto.getTravelDate())).thenReturn(0);
        when(profileRepo.findByPassengerProfileIdAndUserUserId(999L, 1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_userNotFound() {
        dto.setUserId(99L);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_flightNotFound() {
        dto.setFlightId(99L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepo.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(FlightNotFoundException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_flightNotScheduled() {
        flight.setStatus(FlightStatus.CANCELLED);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        assertThrows(InvalidBookingException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_seatTypeNotOffered() {
        dto.setSeatType(SeatType.BUSINESS);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        assertThrows(InvalidBookingException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_insufficientSeats() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(bookingRepo.getBookedSeats(10L, SeatType.ECONOMY, dto.getTravelDate())).thenReturn(100);
        assertThrows(InsufficientAvailabilityException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_travelDateTooSoon() {
        dto.setTravelDate(LocalDate.now());
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(bookingRepo.getBookedSeats(anyLong(), any(), any())).thenReturn(0);
        assertThrows(InvalidBookingException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_passengerCountMismatch() {
        dto.setUnits(2); // 2 seats but only 1 passenger in list
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(flightRepo.findByIdForUpdate(10L)).thenReturn(Optional.of(flight));
        when(bookingRepo.getBookedSeats(10L, SeatType.ECONOMY, dto.getTravelDate())).thenReturn(0);
        assertThrows(InvalidBookingException.class, () -> service.createFlightBooking(dto));
    }
}
