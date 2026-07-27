package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.exception.*;
import com.cts.mapper.BookingMapper;
import com.cts.mapper.ItineraryMapper;
import com.cts.mapper.PassengerMapper;
import com.cts.repository.*;
import com.cts.service.AuditLogService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItineraryServiceImplTest {

    @Mock private ItineraryRepository itineraryRepo;
    @Mock private BookingRepository bookingRepo;
    @Mock private UserRepository userRepo;
    @Mock private AuthenticatedUserProvider authUser;
    @Mock private AuditLogService auditLogService;
    private ItineraryMapper itineraryMapper;
    private ItineraryServiceImpl service;

    private User user;
    private Itinerary itinerary;

    @BeforeEach
    void setup() {
        itineraryMapper = new ItineraryMapper(new BookingMapper(new PassengerMapper()));
        service = new ItineraryServiceImpl(itineraryRepo, bookingRepo, userRepo, authUser, auditLogService, itineraryMapper);

        user = new User();
        user.setUserId(1L);
        user.setEmail("test@mail.com");

        itinerary = new Itinerary();
        itinerary.setItineraryId(1L);
        itinerary.setUser(user);
        itinerary.setBookings(new ArrayList<>());
    }

    //  CREATE
    @Test
    void createSuccess() {
        CreateItineraryDTO dto = new CreateItineraryDTO();
        dto.setUserId(1L);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(itineraryRepo.save(any())).thenReturn(itinerary);

        assertNotNull(service.createItinerary(dto));
    }

    @Test
    void createInvalidDate() {
        CreateItineraryDTO dto = new CreateItineraryDTO();
        dto.setUserId(1L);
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now());

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(InvalidBookingException.class,
                () -> service.createItinerary(dto));
    }

    @Test
    void create_userNotFound() {
        when(userRepo.findById(1L)).thenReturn(Optional.empty());

        CreateItineraryDTO dto = new CreateItineraryDTO();
        dto.setUserId(1L);

        assertThrows(UserNotFoundException.class,
                () -> service.createItinerary(dto));
    }

    //  ADD BOOKING SUCCESS
    @Test
    void addBooking_success() {
        Booking booking = new Booking();
        booking.setUser(user);

        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(booking));

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        assertNotNull(service.addBookingToItinerary(dto));
    }

    @Test
    void addBooking_bookingNotFound_coverLog() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(bookingRepo.findById(10L)).thenReturn(Optional.empty());

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        assertThrows(ResourceNotFoundException.class,
                () -> service.addBookingToItinerary(dto));
    }

    @Test
    void addBooking_invalidOwner() {
        User other = new User();
        other.setUserId(2L);

        Booking booking = new Booking();
        booking.setUser(other);

        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(booking));

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        assertThrows(InvalidBookingException.class,
                () -> service.addBookingToItinerary(dto));
    }

    @Test
    void addBooking_alreadyExists() {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setItinerary(new Itinerary());

        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(booking));

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        assertThrows(InvalidBookingException.class,
                () -> service.addBookingToItinerary(dto));
    }

    //  REMOVE BOOKING
    @Test
    void removeBooking_success() {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setItinerary(itinerary);

        itinerary.setBookings(new ArrayList<>(List.of(booking)));

        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(booking));

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        assertNotNull(service.removeBookingFromItinerary(dto));
    }

    @Test
    void removeBooking_invalid() {
        Booking booking = new Booking();

        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(booking));

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        assertThrows(InvalidBookingException.class,
                () -> service.removeBookingFromItinerary(dto));
    }

    //  UPDATE
    @Test
    void update_success() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(itineraryRepo.save(any())).thenReturn(itinerary);
        doNothing().when(authUser).assertCanActAs(anyLong());

        CreateItineraryDTO dto = new CreateItineraryDTO();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));

        assertNotNull(service.updateItinerary(1L, dto));
    }

    //  DELETE
    @Test
    void delete_success() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(bookingRepo.findByItineraryItineraryId(1L)).thenReturn(List.of(new Booking()));
        doNothing().when(authUser).assertCanActAs(anyLong());

        service.deleteItinerary(1L);

        verify(itineraryRepo).delete(itinerary);
    }

    //  GET UNIFIED: Explicit User Lookup
    @Test
    void getItineraries_withExplicitUserId() {
        when(itineraryRepo.findByUserUserId(1L)).thenReturn(List.of(itinerary));
        doNothing().when(authUser).assertCanActAs(1L);

        assertFalse(service.getItineraries(1L).isEmpty());
        verify(itineraryRepo).findByUserUserId(1L);
    }

    //  GET UNIFIED: Implicit Token Context Fallback
    @Test
    void getItineraries_withNullUserId_defaultsToCurrentContext() {
        when(authUser.current()).thenReturn(user);
        when(itineraryRepo.findByUserUserId(1L)).thenReturn(List.of(itinerary));
        doNothing().when(authUser).assertCanActAs(1L);

        assertFalse(service.getItineraries(null).isEmpty());
        verify(itineraryRepo).findByUserUserId(1L);
    }

    //  SORTING BRANCH COVERAGE
    @Test
    void mapper_sorting_allBranches() {
        Booking b1 = new Booking();
        b1.setUser(user);
        b1.setBookingDate(null);

        Booking b2 = new Booking();
        b2.setUser(user);
        b2.setBookingDate(LocalDate.now());

        itinerary.setBookings(new ArrayList<>(List.of(b1, b2)));

        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        doNothing().when(authUser).assertCanActAs(anyLong());

        service.getItineraryById(1L);
    }

    @Test
    void mapper_sorting_secondBranch() {
        Booking b1 = new Booking();
        b1.setUser(user);
        b1.setBookingDate(LocalDate.now());

        Booking b2 = new Booking();
        b2.setUser(user);
        b2.setBookingDate(null);

        itinerary.setBookings(new ArrayList<>(List.of(b1, b2)));

        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        doNothing().when(authUser).assertCanActAs(anyLong());

        service.getItineraryById(1L);
    }
    
    @Test
    void addBookingItineraryNotFound() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.empty());

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        assertThrows(ResourceNotFoundException.class,
                () -> service.addBookingToItinerary(dto));
    }
    
    @Test
    void removeBookingItineraryNotFound() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.empty());

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        assertThrows(ResourceNotFoundException.class,
                () -> service.removeBookingFromItinerary(dto));
    }

    @Test
    void removeBookingBookingNotFound() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(bookingRepo.findById(10L)).thenReturn(Optional.empty());

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        assertThrows(ResourceNotFoundException.class,
                () -> service.removeBookingFromItinerary(dto));
    }
    
    @Test
    void getByIdNotFound() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.getItineraryById(1L));
    }
    
    @Test
    void update_notFound() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.updateItinerary(1L, new CreateItineraryDTO()));
    }
    
    @Test
    void delete_notFound() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteItinerary(1L));
    }
    
    @Test
    void update_invalidDate_logCoverage() {
        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));

        CreateItineraryDTO dto = new CreateItineraryDTO();
        dto.setStartDate(LocalDate.of(2025, 1, 1));
        dto.setEndDate(LocalDate.of(2025, 1, 1));

        assertThrows(InvalidBookingException.class,
                () -> service.updateItinerary(1L, dto));
    }
    
    @Test
    void mapper_compareTo_branch() {
        Booking b1 = new Booking();
        b1.setUser(user);
        b1.setBookingDate(LocalDate.now().minusDays(1));

        Booking b2 = new Booking();
        b2.setUser(user);
        b2.setBookingDate(LocalDate.now());

        itinerary.setBookings(new ArrayList<>(List.of(b2, b1)));

        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        doNothing().when(authUser).assertCanActAs(anyLong());

        service.getItineraryById(1L);
    }
    
    @Test
    void addBooking_bookingListIsNull_finalCoverage() {
        itinerary.setBookings(null);

        Booking booking = new Booking();
        booking.setUser(user);

        when(itineraryRepo.findById(1L)).thenReturn(Optional.of(itinerary));
        when(bookingRepo.findById(10L)).thenReturn(Optional.of(booking));

        AddBookingDTO dto = new AddBookingDTO();
        dto.setItineraryId(1L);
        dto.setBookingId(10L);

        service.addBookingToItinerary(dto);

        assertNotNull(itinerary.getBookings());
        assertFalse(itinerary.getBookings().isEmpty());
    }
}