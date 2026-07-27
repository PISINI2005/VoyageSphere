package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.HotelDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.dto.HotelStatusUpdateDTO;
import com.cts.entity.Hotel;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.mapper.HotelMapper;
import com.cts.service.AuditLogService;
import com.cts.service.HotelService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/hotels")
@AllArgsConstructor
@Validated
@Tag(name = "Hotel Controller", description = "Manage hotel inventory and search hotels by location, rating, or price range")
@Slf4j
public class HotelController {

    private final HotelService hotelService;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;
    private final HotelMapper hotelMapper;

    @Operation(summary = "Add a new hotel to the inventory")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<Hotel> addHotel(@RequestBody @Valid HotelDTO dto) {

        log.info("Received request to add hotel: {}", dto);
        auditLogService.logAction(AuditActions.CREATE_HOTEL, AuditEntity.HOTEL, null, authUser.currentOrNull(), LogType.INFO);

        Hotel hotel = hotelService.addHotel(dto);

        log.info("Hotel created successfully with ID: {}", hotel.getHotelId());

        return new ResponseEntity<>(hotel, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing hotel by ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<Hotel> updateHotel(@PathVariable Long id,
                                             @RequestBody @Valid HotelDTO dto) {

        log.info("Received request to update hotel with ID: {}", id);
        auditLogService.logAction(AuditActions.UPDATE_HOTEL, AuditEntity.HOTEL, id, authUser.currentOrNull(), LogType.INFO);

        Hotel updatedHotel = hotelService.updateHotel(id, dto);

        log.info("Hotel updated successfully with ID: {}", id);

        return new ResponseEntity<>(updatedHotel, HttpStatus.OK);
    }

    @Operation(summary = "Update only a hotel's status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<Hotel> updateHotelStatus(@PathVariable Long id,
                                                   @RequestBody @Valid HotelStatusUpdateDTO dto) {

        log.info("Received request to update status for hotel with ID: {}", id);
        auditLogService.logAction(AuditActions.UPDATE_HOTEL, AuditEntity.HOTEL, id, authUser.currentOrNull(), LogType.INFO);

        Hotel updatedHotel = hotelService.updateHotelStatus(id, dto.getStatus());

        log.info("Hotel status updated successfully with ID: {}", id);

        return new ResponseEntity<>(updatedHotel, HttpStatus.OK);
    }

    @Operation(summary = "Search and filter hotels by city, rating, and price range (all params optional)")
    @GetMapping("/search")
    public ResponseEntity<?> searchHotels(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) Integer ratings,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size) {

        log.info("Searching hotels: city={}, ratings={}, minPrice={}, maxPrice={}, page={}, size={}",
                city, ratings, minPrice, maxPrice, page, size);

        Object result = hotelService.getFilteredHotels(city, ratings, minPrice, maxPrice, page, size);

        if (result instanceof List<?>) {
            log.info("Search returned {} hotels", ((List<?>) result).size());
        }

        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Soft-delete a hotel by setting its status to INACTIVE")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteHotel(@PathVariable Long id) {

        log.info("Received request to delete hotel with ID: {}", id);
        auditLogService.logAction(AuditActions.DELETE_HOTEL, AuditEntity.HOTEL, id, authUser.currentOrNull(), LogType.WARN);

        hotelService.deleteHotel(id);

        log.info("Hotel {} deleted (deactivated) successfully", id);

        return new ResponseEntity<>("Hotel deactivated successfully", HttpStatus.OK);
    }
    

@GetMapping("/{id}")
public ResponseEntity<HotelResponseDTO> getHotel(@PathVariable Long id) {

    log.info("Fetching hotel with ID: {}", id);

    Hotel hotel = hotelService.getHotelById(id);

    HotelResponseDTO response = hotelMapper.toResponse(hotel);

    log.info("Hotel fetched successfully with ID: {}", id);

    return ResponseEntity.ok(response);
}

}
