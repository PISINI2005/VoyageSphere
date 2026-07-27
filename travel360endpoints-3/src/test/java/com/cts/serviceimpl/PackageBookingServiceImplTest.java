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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PackageBookingServiceImplTest {

    @Mock private UserRepository userRepo;
    @Mock private TravelPackageRepository packageRepo;
    @Mock private BookingRepository bookingRepo;
    @Mock private InvoiceRepository invoiceRepo;
    @Mock private NotificationService notificationService;
    @Mock private AuditLogService auditLogService;
    @Mock private AuthenticatedUserProvider authUser;
    private PassengerMapper passengerMapper;
    private BookingMapper bookingMapper;
    private PackageBookingServiceImpl service;

    private User user;
    private TravelPackage tpackage;
    private BookingPackageDTO dto;

    @BeforeEach
    void setup() {
        passengerMapper = new PassengerMapper();
        bookingMapper = new BookingMapper(passengerMapper);
        service = new PackageBookingServiceImpl(userRepo, packageRepo, bookingRepo, invoiceRepo,
                bookingMapper, notificationService, auditLogService, authUser);

        user = new User();
        user.setUserId(1L);
        user.setEmail("test@mail.com");

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

        dto = new BookingPackageDTO();
        dto.setUserId(1L);
        dto.setPackageId(30L);
        dto.setTravelDate(LocalDate.now().plusDays(10));
        dto.setUnits(1);
        dto.setBookingName("John");
        dto.setGender(Gender.MALE);
    }

    @Test
    void createPackageBooking_success() {

        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(packageRepo.findByIdForUpdate(30L)).thenReturn(Optional.of(tpackage));
        when(bookingRepo.getBookedSlots(30L, dto.getTravelDate())).thenReturn(0);
        when(bookingRepo.save(any())).thenAnswer(inv -> {
            Booking b = inv.getArgument(0);
            b.setBookingId(1L);
            return b;
        });

        BookingPackageResponseDTO response = service.createPackageBooking(dto);

        assertNotNull(response);
        assertEquals(BookingType.PACKAGE, response.getBookingType());
        assertEquals(15000.0, response.getAmount());
        assertEquals(dto.getTravelDate(), response.getBookingDate());
        verify(invoiceRepo).save(any());
        verify(notificationService).sendNotification(any(), anyString(), any());
    }

    @Test
    void createPackageBooking_userNotFound() {
        dto.setUserId(99L);
        when(userRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.createPackageBooking(dto));
    }

    @Test
    void createPackageBooking_packageNotFound() {
        dto.setPackageId(99L);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(packageRepo.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        assertThrows(PackageNotFoundException.class, () -> service.createPackageBooking(dto));
    }

    @Test
    void createPackageBooking_packageNotAvailable() {
        tpackage.setStatus(PackageStatus.INACTIVE);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(packageRepo.findByIdForUpdate(30L)).thenReturn(Optional.of(tpackage));
        assertThrows(InvalidBookingException.class, () -> service.createPackageBooking(dto));
    }

    @Test
    void createPackageBooking_insufficientSlots() {
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(packageRepo.findByIdForUpdate(30L)).thenReturn(Optional.of(tpackage));
        when(bookingRepo.getBookedSlots(30L, dto.getTravelDate())).thenReturn(50);
        assertThrows(InsufficientAvailabilityException.class, () -> service.createPackageBooking(dto));
    }
}
