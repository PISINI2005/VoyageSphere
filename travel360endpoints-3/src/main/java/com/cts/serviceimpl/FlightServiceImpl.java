package com.cts.serviceimpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.FlightDTO;
import com.cts.dto.FlightResponseDTO;
import com.cts.dto.FlightSeatDTO;
import com.cts.dto.PriceDateDTO;
import com.cts.entity.Flight;
import com.cts.entity.FlightSeat;
import com.cts.entity.Partner;
import com.cts.enums.AuditEntity;
import com.cts.enums.FlightStatus;
import com.cts.enums.LogType;
import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;
import com.cts.exception.FlightNotFoundException;
import com.cts.exception.InvalidBookingException;
import com.cts.exception.InvalidPartnerException;
import com.cts.exception.PartnerNotFoundException;
import com.cts.mapper.FlightMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.FlightRepository;
import com.cts.repository.PartnerRepository;
import com.cts.service.AuditLogService;
import com.cts.service.FlightService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class FlightServiceImpl implements FlightService {

    private final FlightRepository repo;
    private final PartnerRepository partnerRepo;
    private final AuditLogService auditLogService;
    private final AuthenticatedUserProvider authUser;
    private final FlightMapper flightMapper;
    private final BookingRepository bookingRepo;
    private final BookingHelper bookingHelper;

    @Override
    @Transactional
    public Flight addFlight(FlightDTO dto) {

        log.info("Adding new flight with partnerId: {}", dto.getPartnerId());

        Partner partner = partnerRepo.findById(dto.getPartnerId())
                .orElseThrow(() -> {
                    log.error("Partner not found with id {}", dto.getPartnerId());
                    return new PartnerNotFoundException(
                            "Partner not found with id " + dto.getPartnerId());
                });

        if (partner.getType() != PartnerType.FLIGHT) {
            log.error("Invalid partner type for partnerId: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not a FLIGHT partner");
        }

        if (partner.getStatus() != PartnerStatus.ACTIVE) {
            log.error("Inactive partner: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not active");
        }

        Flight flight = flightMapper.toEntity(dto, partner);
        flight.setStatus(FlightStatus.SCHEDULED);

        Flight savedFlight = repo.save(flight);
        auditLogService.logAction(
                AuditActions.CREATE_FLIGHT,
                AuditEntity.FLIGHT,
                savedFlight.getFlightId(),
                authUser.currentOrNull(),
                LogType.INFO);
        	

        log.info("Flight created successfully with ID: {}", savedFlight.getFlightId());

        return savedFlight;
    }

    @Override
    @Transactional
    public Flight updateFlight(Long id, FlightDTO dto) {

        log.info("Updating flight with ID: {}", id);

        Flight flight = repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Flight not found with id {}", id);
                    return new FlightNotFoundException("Flight not found");
                });

        Partner partner = partnerRepo.findById(dto.getPartnerId())
                .orElseThrow(() -> {
                    log.error("Partner not found with id {}", dto.getPartnerId());
                    return new PartnerNotFoundException(
                            "Partner not found with id " + dto.getPartnerId());
                });

        if (partner.getType() != PartnerType.FLIGHT) {
            log.error("Invalid partner type for partnerId: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not a FLIGHT partner");
        }

        if (partner.getStatus() != PartnerStatus.ACTIVE) {
            log.error("Inactive partner: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not active");
        }

        log.debug("Updating flight details for ID: {}", id);

        flightMapper.updateEntity(flight, dto, partner);

        Flight updatedFlight = repo.save(flight);
        auditLogService.logAction(
                AuditActions.UPDATE_FLIGHT,
                AuditEntity.FLIGHT,
                updatedFlight.getFlightId(),
                authUser.currentOrNull(),
                LogType.INFO);

        log.info("Flight updated successfully with ID: {}", id);

        return updatedFlight;
    }

    @Override
    @Transactional
    public Flight updateFlightStatus(Long id, FlightStatus status) {

        log.info("Updating status for flight with ID: {}", id);

        Flight flight = repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Flight not found with id {}", id);
                    return new FlightNotFoundException("Flight not found");
                });

        flight.setStatus(status);

        Flight updatedFlight = repo.save(flight);

        log.info("Flight status updated successfully with ID: {}", id);

        return updatedFlight;
    }

    @Override
    public List<FlightResponseDTO> searchFlights(String source, String destination, int page, int size) {

        log.info("Searching flights from '{}' to '{}' (page={}, size={})",
                source, destination, page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<Flight> flightPage =
                repo.findBySourceAndDestination(source, destination, pageable);

        log.info("Search returned {} flights", flightPage.getContent().size());

        return flightPage.getContent().stream().map(flightMapper::toResponse).toList();
    }

    @Override
    public Page<FlightResponseDTO> getAllFlights(Pageable pageable) {

        log.info("Fetching all flights (page={}, size={})",
                pageable.getPageNumber(),
                pageable.getPageSize());

        Page<Flight> flightPage = repo.findAll(pageable);

        log.info("Total flights fetched: {}",
                flightPage.getNumberOfElements());

        return flightPage.map(flightMapper::toResponse);
    }

    @Override
    public List<FlightResponseDTO> filterFlights(String source, String destination,
                                      Double min, Double max,
                                      int page, int size) {

        log.info("Filtering flights from '{}' to '{}' with minPrice={}, maxPrice={}",
                source, destination, min, max);

        Pageable pageable = PageRequest.of(page, size);

        Page<Flight> flightPage;

        if (min != null && max != null) {
            log.debug("Applying seat-price filter between {} and {}", min, max);

            flightPage = repo.findByRouteAndSeatPriceBetween(
                    source, destination, min, max, pageable);
        } else {
            log.debug("No price filter applied");

            flightPage = repo.findBySourceAndDestination(source, destination, pageable);
        }

        log.info("Filter returned {} flights", flightPage.getContent().size());

        return flightPage.getContent().stream().map(flightMapper::toResponse).toList();
    }

   

    @Override
    @Transactional
    public void deleteFlight(Long id) {

        log.info("Deleting (deactivating) flight with ID: {}", id);

        // Soft-delete is just a status transition to CANCELLED; reuse the canonical path.
        updateFlightStatus(id, FlightStatus.CANCELLED);
        auditLogService.logAction(AuditActions.DELETE_FLIGHT, AuditEntity.FLIGHT,
                id, authUser.currentOrNull(), LogType.WARN);

        log.info("Flight {} deactivated successfully", id);
    }

    @Override
    public Page<FlightResponseDTO> searchFlightsWithAvailability(
            String source, String destination, Double min, Double max, java.time.LocalDate date, int page, int size) {

        log.info("Searching flights with availability from '{}' to '{}' on date {} with minPrice={}, maxPrice={}",
                source, destination, date, min, max);

        Pageable pageable = PageRequest.of(page, size);

        // Fetch flights for the route (ignoring base price filter to apply dynamic filter in Java)
        Page<Flight> flightPage = repo.findBySourceAndDestination(source, destination, pageable);

        java.time.LocalDate queryDate = (date != null) ? date : java.time.LocalDate.now();

        // Enrich and filter by dynamic price
        List<FlightResponseDTO> filteredFlights = flightPage.getContent().stream()
                .map(flight -> enrichWithLiveSeats(flight, queryDate))
                .filter(dto -> {
                    if (dto.getSeats() == null) return false;
                    return dto.getSeats().stream().anyMatch(seat ->
                        (min == null || seat.getPrice() >= min) &&
                        (max == null || seat.getPrice() <= max)
                    );
                })
                .toList();

        return new org.springframework.data.domain.PageImpl<>(filteredFlights, pageable, filteredFlights.size());
    }

    // Reusable private helper method
    private FlightResponseDTO enrichWithLiveSeats(Flight flight, java.time.LocalDate queryDate) {
        FlightResponseDTO responseDTO = flightMapper.toResponse(flight);

        if (responseDTO.getSeats() != null) {
            for (FlightSeatDTO seatDTO : responseDTO.getSeats()) {
                // Apply Dynamic Urgency Pricing
                double dynamicPrice = bookingHelper.calculateUrgencyPrice(seatDTO.getPrice(), queryDate);
                seatDTO.setPrice(dynamicPrice);

                // Execute your custom booking query
                int bookedCount = bookingRepo.getBookedSeats(
                    flight.getFlightId(),
                    seatDTO.getSeatType(),
                    queryDate
                );

                // Deduct booked seats from max capacity
                int remainingSeats = Math.max(0, seatDTO.getTotalSeats() - bookedCount);
                seatDTO.setAvailableSeats(remainingSeats);
            }
        }
        return responseDTO;
    }

    @Override
    public List<PriceDateDTO> getPriceCalendar(Long id, String seatType, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching price calendar for flight ID: {}, seatType: {} from {} to {}", id, seatType, startDate, endDate);

        Flight flight = repo.findById(id)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found"));

        FlightSeat seatClass = flight.getSeats().stream()
                .filter(s -> s.getSeatType().name().equalsIgnoreCase(seatType))
                .findFirst()
                .orElseThrow(() -> new InvalidBookingException("Seat type " + seatType + " is not offered on this flight"));

        List<PriceDateDTO> calendar = new java.util.ArrayList<>();
        LocalDate current = startDate;

        while (current.isBefore(endDate) || current.isEqual(endDate)) {
            double price = bookingHelper.calculateUrgencyPrice(seatClass.getPrice(), current);
            calendar.add(new PriceDateDTO(current, price));
            current = current.plusDays(1);
        }

        return calendar;
    }

    @Override
    public FlightResponseDTO getFlightById(Long id, LocalDate date) {
        log.info("Fetching flight with ID: {} (date context: {})", id, date);

        // 1. Fetch the flight entity or fail
        Flight flight = repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Flight not found with id {}", id);
                    return new FlightNotFoundException("Flight not found");
                });

        // 2. Conditional check: if a date is present, compute dynamic live seat balance
        if (date != null) {
            log.debug("Date context found. Calculating live available seats for date: {}", date);
            return enrichWithLiveSeats(flight, date);
        }

        // 3. Fallback: If no date is provided, return standard structural details
        log.debug("No date context found. Returning standard mapping without live calculation.");
        return flightMapper.toResponse(flight);
    }
}