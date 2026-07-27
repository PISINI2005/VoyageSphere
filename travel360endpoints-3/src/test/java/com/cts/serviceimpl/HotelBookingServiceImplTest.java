package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enums.*;
import com.cts.exception.*;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelBookingServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private HotelRepository hotelRepo;
    @Mock private BookingRepository bookingRepo;
    @Mock private InvoiceRepository invoiceRepo;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogService auditLogService;
    @Mock private AuthenticatedUserProvider authUser;
    private PassengerMapper passengerMapper;
    private BookingMapper bookingMapper;
    private HotelBookingServiceImpl service;

    private User user;
    private Hotel hotel;
    private BookingHotelDTO dto;

    @BeforeEach
    void setup() {
        passengerMapper = new PassengerMapper();
        bookingMapper = new BookingMapper(passengerMapper);
        service = new HotelBookingServiceImpl(userRepo, hotelRepo, bookingRepo, invoiceRepo,
                bookingMapper, notificationService, auditLogService, authUser);

        user = new User();
        user.setUserId(1L);
        user.setEmail("test@mail.com");

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

        dto = new BookingHotelDTO();
        dto.setUserId(1L);
        dto.setHotelId(20L);
        dto.setCheckInDate(LocalDate.now().plusDays(5));
        dto.setCheckOutDate(LocalDate.now().plusDays(7)); // 2 nights
        dto.setUnits(1);
        dto.setBookingName("John");
        dto.setGender(Gender.MALE);
        dto.setRoomType(HotelRoomType.STANDARD);
    }

    @Test
    void createHotelBooking_success() {

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(hotelRepo.findByIdForUpdate(20L)).thenReturn(Optional.of(hotel));
        when(bookingRepo.getBookedRooms(20L, HotelRoomType.STANDARD,
                dto.getCheckInDate(), dto.getCheckOutDate())).thenReturn(0);
        when(bookingRepo.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1L);
            return b;
        });

        BookingHotelResponseDTO response = service.createHotelBooking(dto);

        assertNotNull(response);
        assertEquals(BookingType.HOTEL, response.getBookingType());
        assertEquals(6000.0, response.getAmount()); // 3000 * 1 room * 2 nights
        verify(invoiceRepo).save(any());
        verify(notificationService).sendNotification(any(), anyString(), any());
    }

    @Test
    void createHotelBooking_userNotFound() {
        dto.setUserId(99L);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.createHotelBooking(dto));
    }

    @Test
    void createHotelBooking_hotelNotFound() {
        dto.setHotelId(99L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(hotelRepo.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(HotelNotFoundException.class, () -> service.createHotelBooking(dto));
    }

    @Test
    void createHotelBooking_hotelNotAvailable() {
        hotel.setStatus(HotelStatus.INACTIVE);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(hotelRepo.findByIdForUpdate(20L)).thenReturn(Optional.of(hotel));
        assertThrows(InvalidBookingException.class, () -> service.createHotelBooking(dto));
    }

    @Test
    void createHotelBooking_invalidDateRange() {
        dto.setCheckOutDate(dto.getCheckInDate()); // same day → 0 nights
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(hotelRepo.findByIdForUpdate(20L)).thenReturn(Optional.of(hotel));
        assertThrows(InvalidBookingException.class, () -> service.createHotelBooking(dto));
    }

    @Test
    void createHotelBooking_roomTypeNotOffered() {
        dto.setRoomType(HotelRoomType.SUITE);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(hotelRepo.findByIdForUpdate(20L)).thenReturn(Optional.of(hotel));
        assertThrows(InvalidBookingException.class, () -> service.createHotelBooking(dto));
    }

    @Test
    void createHotelBooking_insufficientRooms() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(hotelRepo.findByIdForUpdate(20L)).thenReturn(Optional.of(hotel));
        when(bookingRepo.getBookedRooms(20L, HotelRoomType.STANDARD,
                dto.getCheckInDate(), dto.getCheckOutDate())).thenReturn(50);
        assertThrows(InsufficientAvailabilityException.class, () -> service.createHotelBooking(dto));
    }
}
