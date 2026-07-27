package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enums.*;
import com.cts.enums.Gender;
import com.cts.exception.*;
import com.cts.mapper.BookingMapper;
import com.cts.mapper.PassengerMapper;
import com.cts.repository.*;
import com.cts.entity.PassengerProfile;
import com.cts.service.AuditLogService;
import com.cts.service.FlightBookingService;
import com.cts.service.HotelBookingService;
import com.cts.service.NotificationService;
import com.cts.service.PackageBookingService;
import com.cts.service.TransportBookingService;

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
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepo;
    @Mock private InvoiceRepository invoiceRepo;
    @Mock private PaymentRepository paymentRepo;
    @Mock private PassengerRepository passengerRepo;
    @Mock private AuthenticatedUserProvider authUser;
    @Mock private AuditLogService auditLogService;
    @Mock private NotificationService notificationService;
    @Mock private FlightBookingService flightBookingService;
    @Mock private HotelBookingService hotelBookingService;
    @Mock private TransportBookingService transportBookingService;
    @Mock private PackageBookingService packageBookingService;
    private PassengerMapper passengerMapper;
    private BookingMapper bookingMapper;
    private BookingHelper bookingHelper;
    private BookingServiceImpl service;

    private User user;
    private Flight flight;
    private Hotel hotel;
    private TravelPackage tpackage;
    private Transport transport;

    @BeforeEach
    void setup() {
        passengerMapper = new PassengerMapper();
        bookingMapper = new BookingMapper(passengerMapper);
        bookingHelper = new BookingHelper();
        service = new BookingServiceImpl(bookingRepo, invoiceRepo, paymentRepo, passengerRepo,
                authUser, auditLogService, bookingMapper, notificationService,
                flightBookingService, hotelBookingService, transportBookingService, packageBookingService, bookingHelper);

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

        hotel = Hotel.builder()
                .hotelId(20L)
                .hotelName("Taj")
                .city("Chennai")
                .status(HotelStatus.AVAILABLE)
                .build();
        hotel.setRooms(new ArrayList<>(List.of(
                HotelRoom.builder()
                        .roomType(HotelRoomType.STANDARD)
                        .price(3000.0)
                        .totalRooms(50)
                        .hotel(hotel)
                        .build())));

        tpackage = TravelPackage.builder()
                .packageId(30L)
                .packageName("Goa")
                .source("Chennai")
                .destination("Goa")
                .status(PackageStatus.AVAILABLE)
                .totalSlots(50)
                .price(15000.0)
                .durationDays(5)
                .build();

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
    }

    // ---------------- FLIGHT BOOKING (delegation tests) ----------------

    @Test
    void createFlightBooking_delegatesToSubService() {

        BookingFlightDTO dto = new BookingFlightDTO();
        dto.setUserId(1L);
        dto.setFlightId(10L);
        dto.setTravelDate(LocalDate.now().plusDays(10));
        dto.setUnits(1);
        dto.setSeatType(SeatType.ECONOMY);

        BookingFlightResponseDTO expected = BookingFlightResponseDTO.builder().bookingId(1000L).build();
        when(flightBookingService.createFlightBooking(dto)).thenReturn(expected);

        BookingFlightResponseDTO response = service.createFlightBooking(dto);

        assertNotNull(response);
        assertEquals(1000L, response.getBookingId());
        verify(flightBookingService).createFlightBooking(dto);
    }

    @Test
    void createFlightBooking_propagatesUserNotFoundException() {

        BookingFlightDTO dto = new BookingFlightDTO();
        dto.setUserId(99L);
        when(flightBookingService.createFlightBooking(any())).thenThrow(UserNotFoundException.class);

        assertThrows(UserNotFoundException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_propagatesFlightNotFoundException() {

        BookingFlightDTO dto = new BookingFlightDTO();
        dto.setFlightId(99L);
        when(flightBookingService.createFlightBooking(any())).thenThrow(FlightNotFoundException.class);

        assertThrows(FlightNotFoundException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_propagatesInsufficientAvailabilityException() {

        BookingFlightDTO dto = new BookingFlightDTO();
        when(flightBookingService.createFlightBooking(any())).thenThrow(InsufficientAvailabilityException.class);

        assertThrows(InsufficientAvailabilityException.class, () -> service.createFlightBooking(dto));
    }

    @Test
    void createFlightBooking_propagatesInvalidBookingException() {

        BookingFlightDTO dto = new BookingFlightDTO();
        when(flightBookingService.createFlightBooking(any())).thenThrow(InvalidBookingException.class);

        assertThrows(InvalidBookingException.class, () -> service.createFlightBooking(dto));
    }

    // ---------------- HOTEL BOOKING (delegation tests) ----------------

    @Test
    void createHotelBooking_delegatesToSubService() {

        BookingHotelDTO dto = new BookingHotelDTO();
        dto.setUserId(1L);
        dto.setHotelId(20L);

        BookingHotelResponseDTO expected = BookingHotelResponseDTO.builder().bookingId(2000L).build();
        when(hotelBookingService.createHotelBooking(dto)).thenReturn(expected);

        BookingHotelResponseDTO response = service.createHotelBooking(dto);

        assertNotNull(response);
        assertEquals(2000L, response.getBookingId());
        verify(hotelBookingService).createHotelBooking(dto);
    }

    @Test
    void createHotelBooking_propagatesHotelNotFoundException() {

        BookingHotelDTO dto = new BookingHotelDTO();
        when(hotelBookingService.createHotelBooking(any())).thenThrow(HotelNotFoundException.class);

        assertThrows(HotelNotFoundException.class, () -> service.createHotelBooking(dto));
    }

    @Test
    void createHotelBooking_propagatesInvalidBookingException() {

        BookingHotelDTO dto = new BookingHotelDTO();
        when(hotelBookingService.createHotelBooking(any())).thenThrow(InvalidBookingException.class);

        assertThrows(InvalidBookingException.class, () -> service.createHotelBooking(dto));
    }

    // ---------------- PACKAGE BOOKING (delegation tests) ----------------

    @Test
    void createPackageBooking_delegatesToSubService() {

        BookingPackageDTO dto = new BookingPackageDTO();
        dto.setUserId(1L);
        dto.setPackageId(30L);
        dto.setTravelDate(LocalDate.now().plusDays(10));

        BookingPackageResponseDTO expected = BookingPackageResponseDTO.builder()
                .bookingId(3000L).bookingDate(dto.getTravelDate()).build();
        when(packageBookingService.createPackageBooking(dto)).thenReturn(expected);

        BookingPackageResponseDTO response = service.createPackageBooking(dto);

        assertNotNull(response);
        assertEquals(3000L, response.getBookingId());
        assertEquals(dto.getTravelDate(), response.getBookingDate());
        verify(packageBookingService).createPackageBooking(dto);
    }

    @Test
    void createPackageBooking_propagatesPackageNotFoundException() {

        BookingPackageDTO dto = new BookingPackageDTO();
        when(packageBookingService.createPackageBooking(any())).thenThrow(PackageNotFoundException.class);

        assertThrows(PackageNotFoundException.class, () -> service.createPackageBooking(dto));
    }

    @Test
    void createPackageBooking_propagatesInsufficientAvailabilityException() {

        BookingPackageDTO dto = new BookingPackageDTO();
        when(packageBookingService.createPackageBooking(any())).thenThrow(InsufficientAvailabilityException.class);

        assertThrows(InsufficientAvailabilityException.class, () -> service.createPackageBooking(dto));
    }

    // ---------------- TRANSPORT BOOKING (delegation tests) ----------------

    @Test
    void createTransportBooking_delegatesToSubService() {

        BookingTransportDTO dto = new BookingTransportDTO();
        dto.setUserId(1L);
        dto.setTransportId(40L);
        dto.setTravelDate(LocalDate.now().plusDays(10));

        BookingTransportResponseDTO expected = BookingTransportResponseDTO.builder().bookingId(4000L).build();
        when(transportBookingService.createTransportBooking(dto)).thenReturn(expected);

        BookingTransportResponseDTO response = service.createTransportBooking(dto);

        assertNotNull(response);
        assertEquals(4000L, response.getBookingId());
        verify(transportBookingService).createTransportBooking(dto);
    }

    @Test
    void createTransportBooking_propagatesTransportNotFoundException() {

        BookingTransportDTO dto = new BookingTransportDTO();
        when(transportBookingService.createTransportBooking(any())).thenThrow(TransportNotFoundException.class);

        assertThrows(TransportNotFoundException.class, () -> service.createTransportBooking(dto));
    }

    @Test
    void createTransportBooking_propagatesInvalidBookingException() {

        BookingTransportDTO dto = new BookingTransportDTO();
        when(transportBookingService.createTransportBooking(any())).thenThrow(InvalidBookingException.class);

        assertThrows(InvalidBookingException.class, () -> service.createTransportBooking(dto));
    }

    // ---------------- LISTS ----------------

 // ---------------- GET BOOKINGS UNIFIED METHOD ----------------

//    @Test
//    void getBookings_withExplicitUserId_success() {
//        Booking b = Booking.builder().bookingId(1L).user(user).amount(100.0)
//                .bookingType(BookingType.FLIGHT).status(BookingStatus.PENDING).units(1).build();
//
//        when(bookingRepo.findByUserUserId(1L)).thenReturn(List.of(b));
//
//        // Act
//        List<BookingResponseDTO> list = service.getBookings(1L);
//
//        // Assert
//        assertEquals(1, list.size());
//        verify(authUser).assertCanActAs(1L);
//        verify(bookingRepo).findByUserUserId(1L);
//    }
//
//    @Test
//    void getBookings_withNullUserId_defaultsToCurrentUserSuccess() {
//        Booking b = Booking.builder().bookingId(2L).user(user).amount(150.0)
//                .bookingType(BookingType.HOTEL).status(BookingStatus.CONFIRMED).units(1).build();
//
//        // Simulate AuthenticatedUserProvider context parsing
//        when(authUser.current()).thenReturn(user); 
//        when(bookingRepo.findByUserUserId(1L)).thenReturn(List.of(b));
//
//        // Act: Passing null triggers the authenticated Context fallback
//        List<BookingResponseDTO> list = service.getBookings(null);
//
//        // Assert
//        assertEquals(1, list.size());
//        verify(authUser).assertCanActAs(1L);
//        verify(bookingRepo).findByUserUserId(1L);
//    }

//    @Test
//    void getAllBookings_success() {
//
//        Booking b = Booking.builder().bookingId(1L).user(user).amount(100.0)
//                .bookingType(BookingType.FLIGHT).status(BookingStatus.PENDING).units(1).build();
//
//        when(bookingRepo.findAll()).thenReturn(List.of(b));
//
//        List<BookingResponseDTO> list = service.getAllBookings();
//
//        assertEquals(1, list.size());
//    }

//    @Test
//    void getAllBookings_mapsAllRelations() {
//
//        PassengerProfile profileP = PassengerProfile.builder().passengerProfileId(1L).passengerName("P").build();
//        Passenger p = Passenger.builder().passengerId(9L)
//                .status(PassengerStatus.ACTIVE).profile(profileP).build();
//
//        Booking b = Booking.builder().bookingId(1L).user(user).amount(100.0).units(1)
//                .bookingType(BookingType.FLIGHT).status(BookingStatus.PENDING)
//                .flight(flight).hotel(hotel).transport(transport).travelPackage(tpackage)
//                .itinerary(Itinerary.builder().itineraryId(7L).build())
//                .passengers(List.of(p)).build();
//
//        when(bookingRepo.findAll()).thenReturn(List.of(b));
//
//        List<BookingResponseDTO> list = service.getAllBookings();
//
//        assertEquals(1, list.size());
//        BookingResponseDTO dto = list.get(0);
//        assertEquals(flight.getFlightId(), dto.getFlightId());
//        assertEquals(hotel.getHotelId(), dto.getHotelId());
//        assertEquals(transport.getTransportId(), dto.getTransportId());
//        assertEquals(tpackage.getPackageId(), dto.getPackageId());
//        assertEquals(7L, dto.getItineraryId());
//        assertNotNull(dto.getPassengers());
//        assertEquals(1, dto.getPassengers().size());
//    }

    // ---------------- DELETE BOOKING ----------------

    @Test
    void deleteBooking_pendingSuccess() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(1000.0).units(1)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.PENDING)
                .bookingDate(LocalDate.now().plusDays(10)).build();

        Invoice inv = Invoice.builder().invoiceId(1L).booking(b).status(PaymentStatus.PENDING).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);
        dto.setUserId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(invoiceRepo.findByBookingBookingId(1L)).thenReturn(List.of(inv));

        BookingCancelResponseDTO response = service.deleteBooking(dto);

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertEquals(0.0, response.getRefundAmount());
        verify(invoiceRepo).save(any());
    }

    @Test
    void deleteBooking_confirmedFullRefund() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(1000.0).units(1)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now().plusDays(15)).build();

        // The paid invoice the refund is recorded against (no separate refund invoice is created).
        Invoice paid = Invoice.builder().invoiceId(1L).booking(b).amount(1000.0)
                .status(PaymentStatus.SUCCESS).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);
        dto.setUserId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(invoiceRepo.findByBookingBookingId(1L)).thenReturn(List.of(paid));

        BookingCancelResponseDTO response = service.deleteBooking(dto);

        assertEquals(BookingStatus.CANCELLED, response.getStatus());
        assertEquals(1000.0, response.getRefundAmount());
        assertEquals("FULL", response.getRefundStatus());
        // The original invoice is flipped to REFUNDED; the refund is captured as a Payment.
        assertEquals(PaymentStatus.REFUNDED, paid.getStatus());
        verify(paymentRepo).save(any());
    }

    @Test
    void deleteBooking_confirmedPartialRefund() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(1000.0).units(1)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now().plusDays(5)).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);
        dto.setUserId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        BookingCancelResponseDTO response = service.deleteBooking(dto);

        assertEquals("PARTIAL", response.getRefundStatus());
        assertEquals(800.0, response.getRefundAmount());
    }

    @Test
    void deleteBooking_confirmedRefund60() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(1000.0).units(1)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now().plusDays(3)).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);
        dto.setUserId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        BookingCancelResponseDTO response = service.deleteBooking(dto);

        assertEquals("PARTIAL", response.getRefundStatus());
        assertEquals(600.0, response.getRefundAmount());
    }

    @Test
    void deleteBooking_confirmedNoRefund() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(1000.0).units(1)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now()).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);
        dto.setUserId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        BookingCancelResponseDTO response = service.deleteBooking(dto);

        assertEquals("NONE", response.getRefundStatus());
        assertEquals(0.0, response.getRefundAmount());
    }

    @Test
    void deleteBooking_confirmedOneDayBefore_noRefund() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(1000.0).units(1)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now().plusDays(1)).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);
        dto.setUserId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        BookingCancelResponseDTO response = service.deleteBooking(dto);

        assertEquals("NONE", response.getRefundStatus());
        assertEquals(0.0, response.getRefundAmount());
        verify(paymentRepo, never()).save(any());
    }

    @Test
    void deleteBooking_pastDate_throws() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(1000.0).units(1)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now().minusDays(1)).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);
        dto.setUserId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(InvalidBookingException.class, () -> service.deleteBooking(dto));
    }

    @Test
    void deleteBooking_confirmedNullBookingDate_throws() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(1000.0).units(1)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(null).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);
        dto.setUserId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(InvalidBookingException.class, () -> service.deleteBooking(dto));
    }

    @Test
    void deleteBooking_bookingNotFound() {

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(99L);

        when(bookingRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.deleteBooking(dto));
    }

    @Test
    void deleteBooking_alreadyCancelled() {

        Booking b = Booking.builder().bookingId(1L).user(user).status(BookingStatus.CANCELLED).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(InvalidBookingException.class, () -> service.deleteBooking(dto));
    }

    @Test
    void deleteBooking_invalidState() {

        Booking b = Booking.builder().bookingId(1L).user(user).status(BookingStatus.FAILED).build();

        BookingCancelDTO dto = new BookingCancelDTO();
        dto.setBookingId(1L);
        dto.setUserId(1L);

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(InvalidBookingException.class, () -> service.deleteBooking(dto));
    }

    // ---------------- CANCEL PASSENGER ----------------

    @Test
    void cancelPassenger_multiPassengerSuccess() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(2000.0).units(2)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.PENDING)
                .bookingDate(LocalDate.now().plusDays(10)).build();

        PassengerProfile profile = PassengerProfile.builder().passengerProfileId(1L).passengerName("John").build();
        Passenger p = Passenger.builder().passengerId(5L)
                .status(PassengerStatus.ACTIVE).booking(b).profile(profile).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(passengerRepo.findById(5L)).thenReturn(Optional.of(p));
        when(passengerRepo.countByBookingBookingIdAndStatus(1L, PassengerStatus.ACTIVE)).thenReturn(2L);
        when(invoiceRepo.findByBookingBookingId(1L)).thenReturn(new ArrayList<>());

        PassengerCancelResponseDTO response = service.cancelPassenger(1L, 5L);

        assertNotNull(response);
        assertEquals(5L, response.getPassengerId());
        assertEquals(1, response.getRemainingUnits());
    }

    @Test
    void cancelPassenger_lastPassengerCancelsBooking() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(1000.0).units(1)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.PENDING)
                .bookingDate(LocalDate.now().plusDays(10)).build();

        PassengerProfile profile = PassengerProfile.builder().passengerProfileId(1L).passengerName("John").build();
        Passenger p = Passenger.builder().passengerId(5L)
                .status(PassengerStatus.ACTIVE).booking(b).profile(profile).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(passengerRepo.findById(5L)).thenReturn(Optional.of(p));
        when(passengerRepo.countByBookingBookingIdAndStatus(1L, PassengerStatus.ACTIVE)).thenReturn(1L);
        when(invoiceRepo.findByBookingBookingId(1L)).thenReturn(new ArrayList<>());

        PassengerCancelResponseDTO response = service.cancelPassenger(1L, 5L);

        assertEquals(BookingStatus.CANCELLED, response.getBookingStatus());
        assertEquals(0, response.getRemainingUnits());
    }

    @Test
    void cancelPassenger_confirmedFullRefund() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(2000.0).units(2)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now().plusDays(15)).build();

        PassengerProfile profile = PassengerProfile.builder().passengerProfileId(1L).passengerName("John").build();
        Passenger p = Passenger.builder().passengerId(5L)
                .status(PassengerStatus.ACTIVE).booking(b).profile(profile).build();

        // Booking is paid, so the refund is recorded against this SUCCESS invoice.
        Invoice paid = Invoice.builder().invoiceId(1L).booking(b).amount(2000.0)
                .status(PaymentStatus.SUCCESS).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(passengerRepo.findById(5L)).thenReturn(Optional.of(p));
        when(passengerRepo.countByBookingBookingIdAndStatus(1L, PassengerStatus.ACTIVE)).thenReturn(2L);
        when(invoiceRepo.findByBookingBookingId(1L)).thenReturn(List.of(paid));

        PassengerCancelResponseDTO response = service.cancelPassenger(1L, 5L);

        assertEquals("FULL", response.getRefundStatus());
        assertEquals(1000.0, response.getRefundAmount());
        assertEquals(1, response.getRemainingUnits());
        // Booking lives on with one fewer passenger, so the invoice is PARTIALLY_REFUNDED.
        assertEquals(PaymentStatus.PARTIALLY_REFUNDED, paid.getStatus());
        verify(paymentRepo).save(any());
    }

    @Test
    void cancelPassenger_confirmedPartialRefund() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(2000.0).units(2)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now().plusDays(5)).build();

        PassengerProfile profile = PassengerProfile.builder().passengerProfileId(1L).passengerName("John").build();
        Passenger p = Passenger.builder().passengerId(5L)
                .status(PassengerStatus.ACTIVE).booking(b).profile(profile).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(passengerRepo.findById(5L)).thenReturn(Optional.of(p));
        when(passengerRepo.countByBookingBookingIdAndStatus(1L, PassengerStatus.ACTIVE)).thenReturn(2L);
        when(invoiceRepo.findByBookingBookingId(1L)).thenReturn(new ArrayList<>());

        PassengerCancelResponseDTO response = service.cancelPassenger(1L, 5L);

        assertEquals("PARTIAL", response.getRefundStatus());
        assertEquals(800.0, response.getRefundAmount());
    }

    @Test
    void cancelPassenger_confirmedNoRefund() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(2000.0).units(2)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now()).build();

        PassengerProfile profile = PassengerProfile.builder().passengerProfileId(1L).passengerName("John").build();
        Passenger p = Passenger.builder().passengerId(5L)
                .status(PassengerStatus.ACTIVE).booking(b).profile(profile).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(passengerRepo.findById(5L)).thenReturn(Optional.of(p));
        when(passengerRepo.countByBookingBookingIdAndStatus(1L, PassengerStatus.ACTIVE)).thenReturn(2L);
        when(invoiceRepo.findByBookingBookingId(1L)).thenReturn(new ArrayList<>());

        PassengerCancelResponseDTO response = service.cancelPassenger(1L, 5L);

        assertEquals("NONE", response.getRefundStatus());
        assertEquals(0.0, response.getRefundAmount());
        assertEquals(1, response.getRemainingUnits());
        verify(paymentRepo, never()).save(any());
    }

    @Test
    void cancelPassenger_bookingNotFound() {

        when(bookingRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.cancelPassenger(99L, 5L));
    }

    @Test
    void cancelPassenger_bookingAlreadyCancelled() {

        Booking b = Booking.builder().bookingId(1L).user(user).status(BookingStatus.CANCELLED).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(InvalidBookingException.class,
                () -> service.cancelPassenger(1L, 5L));
    }

    @Test
    void cancelPassenger_pastDate_throws() {

        Booking b = Booking.builder().bookingId(1L).user(user).amount(2000.0).units(2)
                .bookingType(BookingType.FLIGHT).status(BookingStatus.CONFIRMED)
                .bookingDate(LocalDate.now().minusDays(1)).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(InvalidBookingException.class,
                () -> service.cancelPassenger(1L, 5L));
    }

    @Test
    void cancelPassenger_wrongBookingType() {

        Booking b = Booking.builder().bookingId(1L).user(user).status(BookingStatus.PENDING)
                .bookingType(BookingType.HOTEL).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(InvalidBookingException.class,
                () -> service.cancelPassenger(1L, 5L));
    }

    @Test
    void cancelPassenger_passengerNotFound() {

        Booking b = Booking.builder().bookingId(1L).user(user).status(BookingStatus.PENDING)
                .bookingType(BookingType.FLIGHT).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(passengerRepo.findById(5L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.cancelPassenger(1L, 5L));
    }

    @Test
    void cancelPassenger_passengerNotInBooking() {

        Booking b = Booking.builder().bookingId(1L).user(user).status(BookingStatus.PENDING)
                .bookingType(BookingType.FLIGHT).build();

        Booking other = Booking.builder().bookingId(2L).build();
        Passenger p = Passenger.builder().passengerId(5L).status(PassengerStatus.ACTIVE).booking(other).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(passengerRepo.findById(5L)).thenReturn(Optional.of(p));

        assertThrows(InvalidBookingException.class,
                () -> service.cancelPassenger(1L, 5L));
    }

    @Test
    void cancelPassenger_passengerAlreadyCancelled() {

        Booking b = Booking.builder().bookingId(1L).user(user).status(BookingStatus.PENDING)
                .bookingType(BookingType.FLIGHT).build();

        Passenger p = Passenger.builder().passengerId(5L).status(PassengerStatus.CANCELLED).booking(b).build();

        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(passengerRepo.findById(5L)).thenReturn(Optional.of(p));

        assertThrows(InvalidBookingException.class,
                () -> service.cancelPassenger(1L, 5L));
    }
}
