package com.cts.serviceimpl;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.HotelDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.dto.HotelRoomDTO;
import com.cts.entity.Hotel;
import com.cts.entity.Partner;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;
import com.cts.enums.HotelStatus;
import com.cts.exception.HotelNotFoundException;
import com.cts.exception.InvalidPartnerException;
import com.cts.exception.PartnerNotFoundException;
import com.cts.mapper.HotelMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.HotelRepository;
import com.cts.repository.PartnerRepository;
import com.cts.service.AuditLogService;
import com.cts.service.HotelService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
@Slf4j
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelrepo;
    private final PartnerRepository partnerRepo;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;
    private final HotelMapper hotelMapper;
    private final BookingRepository bookingRepo;

    @Override
    public Hotel addHotel(HotelDTO dto) {

        log.info("Adding new hotel with partnerId: {}", dto.getPartnerId());

        Partner partner = partnerRepo.findById(dto.getPartnerId())
                .orElseThrow(() -> {
                    log.error("Partner not found with id {}", dto.getPartnerId());
                    return new PartnerNotFoundException(
                            "Partner not found with id " + dto.getPartnerId());
                });

        if (partner.getType() != PartnerType.HOTEL) {
            log.error("Invalid partner type for partnerId: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not a HOTEL partner");
        }

        if (partner.getStatus() != PartnerStatus.ACTIVE) {
            log.error("Inactive partner: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not active");
        }

        Hotel hotel = hotelMapper.toEntity(dto, partner);
        hotel.setStatus(HotelStatus.AVAILABLE);

        Hotel savedHotel = hotelrepo.save(hotel);
        auditLogService.logAction(AuditActions.CREATE_HOTEL, AuditEntity.HOTEL, savedHotel.getHotelId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Hotel created successfully with ID: {}", savedHotel.getHotelId());

        return savedHotel;
    }

    @Override
    @Transactional
    public Hotel updateHotel(Long id, HotelDTO dto) {

        log.info("Updating hotel with ID: {}", id);

        Hotel hotel = hotelrepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Hotel not found with id {}", id);
                    return new HotelNotFoundException("Hotel not found");
                });

        Partner partner = partnerRepo.findById(dto.getPartnerId())
                .orElseThrow(() -> {
                    log.error("Partner not found with id {}", dto.getPartnerId());
                    return new PartnerNotFoundException(
                            "Partner not found with id " + dto.getPartnerId());
                });

        if (partner.getType() != PartnerType.HOTEL) {
            log.error("Invalid partner type for partnerId: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not a HOTEL partner");
        }

        if (partner.getStatus() != PartnerStatus.ACTIVE) {
            log.error("Inactive partner: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not active");
        }

        log.debug("Updating fields for hotel ID: {}", id);

        hotelMapper.updateEntity(hotel, dto, partner);

        Hotel updatedHotel = hotelrepo.save(hotel);
        auditLogService.logAction(AuditActions.UPDATE_HOTEL, AuditEntity.HOTEL, updatedHotel.getHotelId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Hotel updated successfully with ID: {}", id);

        return updatedHotel;
    }

    @Override
    @Transactional
    public Hotel updateHotelStatus(Long id, HotelStatus status) {

        log.info("Updating status for hotel with ID: {}", id);

        Hotel hotel = hotelrepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Hotel not found with id {}", id);
                    return new HotelNotFoundException("Hotel not found");
                });

        hotel.setStatus(status);

        Hotel updatedHotel = hotelrepo.save(hotel);

        log.info("Hotel status updated successfully with ID: {}", id);

        return updatedHotel;
    }

   

   

    @Override
    @Transactional
    public void deleteHotel(Long id) {

        log.info("Deleting (deactivating) hotel with ID: {}", id);

        // Soft-delete is just a status transition to INACTIVE; reuse the canonical path.
        updateHotelStatus(id, HotelStatus.INACTIVE);
        auditLogService.logAction(AuditActions.DELETE_HOTEL, AuditEntity.HOTEL,
                id, authUser.currentOrNull(), LogType.WARN);

        log.info("Hotel {} deactivated successfully", id);
    }
    @Override
    public HotelResponseDTO getHotelById(
            Long id,
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        log.info(
                "Fetching hotel with ID: {} (checkIn={}, checkOut={})",
                id,
                checkInDate,
                checkOutDate);

        Hotel hotel = hotelrepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Hotel not found with id {}", id);
                    return new HotelNotFoundException("Hotel not found");
                });

        if (checkInDate != null && checkOutDate != null) {

            log.debug(
                    "Date context found. Calculating live room availability.");

            return enrichWithLiveRooms(
                    hotel,
                    checkInDate,
                    checkOutDate);
        }

        log.debug(
                "No date context found. Returning standard mapping.");

        return hotelMapper.toResponse(hotel);
    }
    
    
    public HotelResponseDTO enrichWithLiveRooms(
            Hotel hotel,
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        HotelResponseDTO responseDTO = hotelMapper.toResponse(hotel);

        if (responseDTO.getRooms() != null) {

            for (HotelRoomDTO roomDTO : responseDTO.getRooms()) {

                int bookedRooms = bookingRepo.getBookedRooms(
                        hotel.getHotelId(),
                        roomDTO.getRoomType(),
                        checkInDate,
                        checkOutDate);

                int remainingRooms = Math.max(
                        0,
                        roomDTO.getTotalRooms() - bookedRooms);

                roomDTO.setAvailableRooms(remainingRooms);
            }
        }

        return responseDTO;
    }

    @Override
    public Page<HotelResponseDTO> getFilteredHotels(
            String location,
            Integer ratings,
            Double minPrice,
            Double maxPrice,
            int page,
            int size) {

        log.info("Filtering hotels with location={}, ratings={}, minPrice={}, maxPrice={}, page={}, size={}",
                location, ratings, minPrice, maxPrice, page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<Hotel> hotelPage = hotelrepo.filterHotels(
                location,
                ratings,
                minPrice,
                maxPrice,
                pageable
        );

        log.info("Filter query returned {} total elements", hotelPage.getTotalElements());

        // FIX: Map directly on the Page object to preserve pagination metadata
        return hotelPage.map(hotelMapper::toResponse);
    }

    @Override
    public Page<HotelResponseDTO> findByLocation(String location, int page, int size) {

        log.info("Fetching hotels by location '{}' (page={}, size={})", location, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Hotel> hotelPage = hotelrepo.findByCity(location, pageable);

        log.info("Found {} total elements in location '{}'", hotelPage.getTotalElements(), location);

        // FIX: Map directly on the Page object to preserve pagination metadata
        return hotelPage.map(hotelMapper::toResponse);
    }

    @Override
    public Page<HotelResponseDTO> getFilteredHotelsWithAvailability(
            String city,
            Integer ratings,
            Double minPrice,
            Double maxPrice,
            LocalDate checkInDate,
            LocalDate checkOutDate,
            int page,
            int size) {

        log.info("Searching hotels with availability: city={}, checkIn={}, checkOut={}",
                city, checkInDate, checkOutDate);

        Pageable pageable = PageRequest.of(page, size);

        Page<Hotel> hotelPage = hotelrepo.filterHotels(
                city,
                ratings,
                minPrice,
                maxPrice,
                pageable);

        // FIX: Map directly on the Page object to keep pagination metadata alive.
        // NOTE: We drop the in-memory .filter(...) step here so the page maintains its full structural size.
        // Any rooms with 0 inventory will simply be displayed as "Sold Out" on the frontend item.
        return hotelPage.map(hotel -> enrichWithLiveRooms(hotel, checkInDate, checkOutDate));
    }
    
    
    
}
