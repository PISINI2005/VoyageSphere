package com.cts.serviceimpl;

import com.cts.dto.HotelDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.dto.HotelRoomDTO;
import com.cts.entity.Hotel;
import com.cts.entity.HotelRoom;
import com.cts.entity.Partner;
import com.cts.enums.HotelRoomType;
import com.cts.enums.HotelStatus;
import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;
import com.cts.exception.HotelNotFoundException;
import com.cts.exception.InvalidPartnerException;
import com.cts.exception.PartnerNotFoundException;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.mapper.HotelMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.HotelRepository;
import com.cts.repository.PartnerRepository;
import com.cts.service.AuditLogService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelrepo;

    @Mock
    private PartnerRepository partnerRepo;

    @Mock
    private AuthenticatedUserProvider authUser;

    @Mock
    private AuditLogService auditLogService;

    @Mock
    private BookingRepository bookingRepo;

    @Spy
    private HotelMapper hotelMapper = new HotelMapper();

    @InjectMocks
    private HotelServiceImpl hotelService;

    private HotelDTO dto;
    private Partner partner;

    @BeforeEach
    void setUp() {

        dto = new HotelDTO();
        dto.setHotelName("Test Hotel");
        dto.setCity("Chennai");
        dto.setRatings(4);
        dto.setContactNo("9876543210");
        dto.setEmailId("test@mail.com");
        dto.setStatus(HotelStatus.AVAILABLE);
        dto.setPartnerId(1L);
        dto.setRooms(List.of(
                HotelRoomDTO.builder()
                        .roomType(HotelRoomType.STANDARD)
                        .price(2000.0)
                        .totalRooms(10)
                        .build()));

        partner = new Partner();
        partner.setPartnerId(1L);
        partner.setType(PartnerType.HOTEL);
        partner.setStatus(PartnerStatus.ACTIVE);
    }

    @Test
    void addHotel_success() {

        when(partnerRepo.findById(1L)).thenReturn(Optional.of(partner));
        when(hotelrepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Hotel result = hotelService.addHotel(dto);

        assertNotNull(result);
        assertEquals("Test Hotel", result.getHotelName());
    }

    @Test
    void addHotel_partnerNotFound() {

        when(partnerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PartnerNotFoundException.class,
                () -> hotelService.addHotel(dto));
    }

    @Test
    void addHotel_invalidPartnerType() {

        partner.setType(PartnerType.FLIGHT);

        when(partnerRepo.findById(1L)).thenReturn(Optional.of(partner));

        assertThrows(InvalidPartnerException.class,
                () -> hotelService.addHotel(dto));
    }

    @Test
    void addHotel_inactivePartner() {

        partner.setStatus(PartnerStatus.INACTIVE);

        when(partnerRepo.findById(1L)).thenReturn(Optional.of(partner));

        assertThrows(InvalidPartnerException.class,
                () -> hotelService.addHotel(dto));
    }

    @Test
    void updateHotel_success() {

        Hotel hotel = new Hotel();
        hotel.setHotelId(1L);

        when(hotelrepo.findById(1L)).thenReturn(Optional.of(hotel));
        when(partnerRepo.findById(1L)).thenReturn(Optional.of(partner));
        when(hotelrepo.save(any())).thenReturn(hotel);

        Hotel result = hotelService.updateHotel(1L, dto);

        assertEquals(1L, result.getHotelId());
    }

    @Test
    void updateHotel_notFound() {

        when(hotelrepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class,
                () -> hotelService.updateHotel(1L, dto));
    }

    @Test
    void updateHotel_partnerNotFound() {

        when(hotelrepo.findById(1L)).thenReturn(Optional.of(new Hotel()));
        when(partnerRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(PartnerNotFoundException.class,
                () -> hotelService.updateHotel(1L, dto));
    }

    @Test
    void updateHotel_invalidPartnerType() {

        Hotel hotel = new Hotel();
        hotel.setHotelId(1L);

        partner.setType(PartnerType.FLIGHT);

        when(hotelrepo.findById(1L)).thenReturn(Optional.of(hotel));
        when(partnerRepo.findById(1L)).thenReturn(Optional.of(partner));

        assertThrows(InvalidPartnerException.class,
                () -> hotelService.updateHotel(1L, dto));
    }

    @Test
    void updateHotel_inactivePartner() {

        Hotel hotel = new Hotel();
        hotel.setHotelId(1L);

        partner.setStatus(PartnerStatus.INACTIVE);

        when(hotelrepo.findById(1L)).thenReturn(Optional.of(hotel));
        when(partnerRepo.findById(1L)).thenReturn(Optional.of(partner));

        assertThrows(InvalidPartnerException.class,
                () -> hotelService.updateHotel(1L, dto));
    }

    @Test
    void updateHotelStatus_success() {

        Hotel hotel = new Hotel();
        hotel.setHotelId(1L);

        when(hotelrepo.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelrepo.save(any())).thenReturn(hotel);

        Hotel result = hotelService.updateHotelStatus(1L, HotelStatus.INACTIVE);

        assertEquals(1L, result.getHotelId());
        assertEquals(HotelStatus.INACTIVE, result.getStatus());
    }

    @Test
    void updateHotelStatus_notFound() {

        when(hotelrepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class,
                () -> hotelService.updateHotelStatus(1L, HotelStatus.INACTIVE));
    }

    @Test
    void deleteHotel_success() {

        Hotel hotel = new Hotel();
        hotel.setHotelId(1L);

        when(hotelrepo.findById(1L)).thenReturn(Optional.of(hotel));
        when(hotelrepo.save(any())).thenReturn(hotel);

        hotelService.deleteHotel(1L);

        assertEquals(HotelStatus.INACTIVE, hotel.getStatus());

        verify(auditLogService).logAction(
                eq("DELETE_HOTEL"),
                eq(com.cts.enums.AuditEntity.HOTEL),
                eq(1L),
                any(),
                eq(com.cts.enums.LogType.WARN)
        );
    }

    @Test
    void getHotelById_withoutDates_success() {

        Hotel hotel = new Hotel();
        hotel.setHotelId(1L);
        hotel.setHotelName("Test Hotel");

        when(hotelrepo.findById(1L)).thenReturn(Optional.of(hotel));

        HotelResponseDTO result = hotelService.getHotelById(1L, null, null);

        assertNotNull(result);
        assertEquals(1L, result.getHotelId());
    }

    @Test
    void getHotelById_withDates_success() {

        Hotel hotel = new Hotel();
        hotel.setHotelId(1L);
        hotel.setRooms(List.of(
                HotelRoom.builder()
                        .roomType(HotelRoomType.STANDARD)
                        .price(1500.0)
                        .totalRooms(10)
                        .hotel(hotel)
                        .build()));

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(2);

        when(hotelrepo.findById(1L)).thenReturn(Optional.of(hotel));
        when(bookingRepo.getBookedRooms(1L, HotelRoomType.STANDARD, checkIn, checkOut)).thenReturn(4);

        HotelResponseDTO result = hotelService.getHotelById(1L, checkIn, checkOut);

        assertNotNull(result);
        assertEquals(6, result.getRooms().get(0).getAvailableRooms());
    }

    @Test
    void getHotelById_notFound() {

        when(hotelrepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(HotelNotFoundException.class,
                () -> hotelService.getHotelById(1L, null, null));
    }

    @Test
    void getFilteredHotels_success() {

        Page<Hotel> page = new PageImpl<>(List.of(new Hotel()));

        when(hotelrepo.filterHotels(any(), any(), any(), any(), any()))
                .thenReturn(page);

        Page<HotelResponseDTO> result = hotelService.getFilteredHotels(
                "Chennai", 4, 1000.0, 5000.0, 0, 5);

        assertFalse(result.isEmpty());
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getFilteredHotels_mappingCoverage() {

        Hotel hotel = new Hotel();
        hotel.setHotelId(1L);
        hotel.setHotelName("Test Hotel");
        hotel.setCity("Chennai");
        hotel.setRatings(4);
        hotel.setContactNo("9876543210");
        hotel.setEmailId("test@mail.com");
        hotel.setStatus(HotelStatus.AVAILABLE);
        hotel.setPartner(partner);
        hotel.setRooms(List.of(
                HotelRoom.builder()
                        .roomType(HotelRoomType.STANDARD)
                        .price(1500.0)
                        .totalRooms(5)
                        .hotel(hotel)
                        .build()));

        Page<Hotel> page = new PageImpl<>(List.of(hotel));

        when(hotelrepo.filterHotels(any(), any(), any(), any(), any()))
                .thenReturn(page);

        Page<HotelResponseDTO> result = hotelService.getFilteredHotels(
                "Chennai", 4, 1000.0, 2000.0, 0, 5);

        HotelResponseDTO mapped = result.getContent().get(0);

        assertEquals("Test Hotel", mapped.getHotelName());
        assertEquals("Chennai", mapped.getCity());
        assertEquals(4, mapped.getRatings());
        assertEquals("9876543210", mapped.getContactNo());
        assertEquals("test@mail.com", mapped.getEmailId());
        assertEquals(HotelStatus.AVAILABLE, mapped.getStatus());
        assertEquals(1, mapped.getRooms().size());
        assertEquals(HotelRoomType.STANDARD, mapped.getRooms().get(0).getRoomType());
        assertEquals(1500.0, mapped.getRooms().get(0).getPrice());
        assertEquals(5, mapped.getRooms().get(0).getTotalRooms());
    }

    @Test
    void getFilteredHotels_emptyResult() {

        Page<Hotel> emptyPage = new PageImpl<>(Collections.emptyList());

        when(hotelrepo.filterHotels(any(), any(), any(), any(), any()))
                .thenReturn(emptyPage);

        Page<HotelResponseDTO> result = hotelService.getFilteredHotels(
                null, null, null, null, 0, 5);

        assertTrue(result.isEmpty());
    }

    @Test
    void findByLocation_success() {

        Page<Hotel> page = new PageImpl<>(List.of(new Hotel()));

        when(hotelrepo.findByCity(eq("Chennai"), any()))
                .thenReturn(page);

        Page<HotelResponseDTO> result = hotelService.findByLocation("Chennai", 0, 5);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void findByLocation_empty() {

        when(hotelrepo.findByCity(eq("Chennai"), any()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        Page<HotelResponseDTO> result = hotelService.findByLocation("Chennai", 0, 5);

        assertTrue(result.isEmpty());
    }

    @Test
    void getFilteredHotelsWithAvailability_success() {

        Hotel hotel = new Hotel();
        hotel.setHotelId(1L);
        hotel.setRooms(List.of(
                HotelRoom.builder()
                        .roomType(HotelRoomType.STANDARD)
                        .price(1500.0)
                        .totalRooms(10)
                        .hotel(hotel)
                        .build()));

        LocalDate checkIn = LocalDate.now();
        LocalDate checkOut = checkIn.plusDays(2);
        Page<Hotel> page = new PageImpl<>(List.of(hotel));

        when(hotelrepo.filterHotels(any(), any(), any(), any(), any())).thenReturn(page);
        when(bookingRepo.getBookedRooms(1L, HotelRoomType.STANDARD, checkIn, checkOut)).thenReturn(3);

        Page<HotelResponseDTO> result = hotelService.getFilteredHotelsWithAvailability(
                "Chennai", 4, 1000.0, 5000.0, checkIn, checkOut, 0, 5);

        assertFalse(result.isEmpty());
        assertEquals(7, result.getContent().get(0).getRooms().get(0).getAvailableRooms());
    }
}