package com.cts.serviceimpl;

import com.cts.dto.FlightDTO;
import com.cts.dto.FlightResponseDTO;
import com.cts.entity.Flight;
import com.cts.entity.Partner;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;
import com.cts.enums.FlightStatus;
import com.cts.exception.FlightNotFoundException;
import com.cts.exception.InvalidPartnerException;
import com.cts.exception.PartnerNotFoundException;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.mapper.FlightMapper;
import com.cts.repository.FlightRepository;
import com.cts.repository.PartnerRepository;
import com.cts.service.AuditLogService;
import com.cts.service.FlightService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class FlightServiceImpl implements FlightService {

    private final FlightRepository repo;
    private final PartnerRepository partnerRepo;
    private final AuditLogService auditLogService;
    private final AuthenticatedUserProvider authUser;
    private final FlightMapper flightMapper;

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
    public List<FlightResponseDTO> getAllFlights(int page, int size) {

        log.info("Fetching all flights (page={}, size={})", page, size);

        Pageable pageable = PageRequest.of(page, size);

        Page<Flight> flightPage = repo.findAll(pageable);

        log.info("Total flights fetched: {}", flightPage.getContent().size());

        return flightPage.getContent().stream().map(flightMapper::toResponse).toList();
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
    public FlightResponseDTO getFlightById(Long id) {

        log.info("Fetching flight with ID: {}", id);

        Flight flight = repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Flight not found with id {}", id);
                    return new FlightNotFoundException("Flight not found");
                });

        return flightMapper.toResponse(flight);
    }

    @Override
    @Transactional
    public void deleteFlight(Long id) {

        log.info("Deleting (deactivating) flight with ID: {}", id);

        Flight flight = repo.findById(id)
                .orElseThrow(() -> {
                    log.error("Flight not found with id {}", id);
                    return new FlightNotFoundException("Flight not found");
                });

        flight.setStatus(FlightStatus.CANCELLED);
        repo.save(flight);
        auditLogService.logAction(AuditActions.DELETE_FLIGHT, AuditEntity.FLIGHT,
                id, authUser.currentOrNull(), LogType.WARN);

        log.info("Flight {} deactivated successfully", id);
    }
}
