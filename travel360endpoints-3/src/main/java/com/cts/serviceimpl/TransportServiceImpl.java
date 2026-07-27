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
import com.cts.dto.PriceDateDTO;
import com.cts.dto.TransportDTO;
import com.cts.dto.TransportResponseDTO;
import com.cts.dto.TransportSeatDTO;
import com.cts.entity.Partner;
import com.cts.entity.Transport;
import com.cts.entity.TransportSeat;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;
import com.cts.enums.TransportStatus;
import com.cts.exception.InvalidBookingException;
import com.cts.exception.InvalidPartnerException;
import com.cts.exception.PartnerNotFoundException;
import com.cts.exception.TransportNotFoundException;
import com.cts.mapper.TransportMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.PartnerRepository;
import com.cts.repository.TransportRepository;
import com.cts.service.AuditLogService;
import com.cts.service.TransportService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class TransportServiceImpl implements TransportService {

    private final TransportRepository transportRepo;
    private final PartnerRepository partnerRepo;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;
    private final TransportMapper transportMapper;
    private final BookingRepository bookingRepo;
    private final BookingHelper bookingHelper;

    @Override
    public Transport addTransport(TransportDTO dto) {

        log.info("Adding new transport with partnerId: {}", dto.getPartnerId());

        Partner partner = partnerRepo.findById(dto.getPartnerId())
                .orElseThrow(() -> {
                    log.error("Partner not found with id {}", dto.getPartnerId());
                    return new PartnerNotFoundException(
                            "Partner not found with id " + dto.getPartnerId());
                });

        if (partner.getType() != PartnerType.BUS) {
            log.error("Invalid partner type for partnerId: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not a BUS partner");
        }
        if (partner.getStatus() != PartnerStatus.ACTIVE) {
            log.error("Inactive partner: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not active");
        }

        Transport transport = transportMapper.toEntity(dto, partner);
        transport.setTransportStatus(TransportStatus.AVAILABLE);

        transport = transportRepo.save(transport);
        auditLogService.logAction(AuditActions.CREATE_TRANSPORT, AuditEntity.TRANSPORT, transport.getTransportId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Transport created successfully with ID: {}", transport.getTransportId());

        return transport;
    }

    @Override
    @Transactional
    public Transport updateTransport(Long id, TransportDTO dto) {

        log.info("Updating transport with ID: {}", id);

        Transport transport = transportRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Transport not found with id {}", id);
                    return new TransportNotFoundException("Transport not found");
                });

        Partner partner = partnerRepo.findById(dto.getPartnerId())
                .orElseThrow(() -> {
                    log.error("Partner not found with id {}", dto.getPartnerId());
                    return new PartnerNotFoundException(
                            "Partner not found with id " + dto.getPartnerId());
                });

        if (partner.getType() != PartnerType.BUS) {
            log.error("Invalid partner type for partnerId: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not a BUS partner");
        }
        if (partner.getStatus() != PartnerStatus.ACTIVE) {
            log.error("Inactive partner: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not active");
        }

        log.debug("Updating transport details for ID: {}", id);

        transportMapper.updateEntity(transport, dto, partner);

        transport = transportRepo.save(transport);
        auditLogService.logAction(AuditActions.UPDATE_TRANSPORT, AuditEntity.TRANSPORT, transport.getTransportId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Transport updated successfully with ID: {}", id);

        return transport;
    }

    @Override
    @Transactional
    public Transport updateTransportStatus(Long id, TransportStatus status) {

        log.info("Updating status of transport with ID: {}", id);

        Transport transport = transportRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Transport not found with id {}", id);
                    return new TransportNotFoundException("Transport not found");
                });

        transport.setTransportStatus(status);

        transport = transportRepo.save(transport);

        log.info("Transport status updated successfully with ID: {}", id);

        return transport;
    }

    

    @Override
    public List<PriceDateDTO> getPriceCalendar(Long id, String transportClass, LocalDate startDate, LocalDate endDate) {
        log.info("Fetching price calendar for transport ID: {}, transportClass: {} from {} to {}", id, transportClass, startDate, endDate);

        Transport transport = transportRepo.findById(id)
                .orElseThrow(() -> new TransportNotFoundException("Transport not found"));

        TransportSeat seatClass = transport.getSeats().stream()
                .filter(s -> s.getTransportClass().name().equalsIgnoreCase(transportClass))
                .findFirst()
                .orElseThrow(() -> new InvalidBookingException("Class " + transportClass + " is not offered on this transport"));

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
    public TransportResponseDTO getTransportById(
            Long id,
            LocalDate date) {

        log.info(
                "Fetching transport with ID {} for date {}",
                id,
                date);

        Transport transport = transportRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Transport not found with id {}", id);
                    return new TransportNotFoundException(
                            "Transport not found");
                });

        if (date != null) {

            return enrichWithLiveSeats(
                    transport,
                    date);
        }

        return transportMapper.toResponse(transport);
    }

    @Override
    @Transactional
    public void deleteTransport(Long id) {

        log.info("Deleting (deactivating) transport with ID: {}", id);

        // Soft-delete is just a status transition to OUT_OF_SERVICE; reuse the canonical path.
        updateTransportStatus(id, TransportStatus.OUT_OF_SERVICE);
        auditLogService.logAction(AuditActions.DELETE_TRANSPORT, AuditEntity.TRANSPORT,
                id, authUser.currentOrNull(), LogType.WARN);

        log.info("Transport {} deactivated successfully", id);
    }
    
    
    @Override
    public Page<TransportResponseDTO> getAllTransports(int page, int size) {

        log.info("Fetching all transports (page={}, size={})", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Transport> transportPage = transportRepo.findAll(pageable);

        log.info("Total transports elements found: {}", transportPage.getTotalElements());

        // FIX: Map directly on the Page object to preserve pagination metadata
        return transportPage.map(transportMapper::toResponse);
    }

    @Override
    public Page<TransportResponseDTO> findByRoute(String source, String destination, int page, int size) {

        log.info("Finding transports from '{}' to '{}' (page={}, size={})", source, destination, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Transport> transportPage = transportRepo.findBySourceAndDestination(source, destination, pageable);

        log.info("Route search returned {} total elements", transportPage.getTotalElements());

        // FIX: Map directly on the Page object to preserve pagination metadata
        return transportPage.map(transportMapper::toResponse);
    }

    @Override
    public Page<TransportResponseDTO> findByStatus(TransportStatus status, int page, int size) {

        log.info("Finding transports with status: {} (page={}, size={})", status, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<Transport> transportPage = transportRepo.findByTransportStatus(status, pageable);

        log.info("Status search returned {} total elements", transportPage.getTotalElements());

        // FIX: Map directly on the Page object to preserve pagination metadata
        return transportPage.map(transportMapper::toResponse);
    }

    @Override
    public Page<TransportResponseDTO> findByRouteWithAvailability(
            String source,
            String destination,
            Double min,
            Double max,
            LocalDate date,
            int page,
            int size) {

        log.info("Searching transports with availability from '{}' to '{}' on date Context: {} with minPrice={}, maxPrice={}",
                source, destination, date, min, max);

        Pageable pageable = PageRequest.of(page, size);

        // Fetch transports for the route (ignoring base price filter to apply dynamic filter in Java)
        Page<Transport> transportPage = transportRepo.findBySourceAndDestination(source, destination, pageable);

        // Map entities to DTOs and calculate seat capacities/prices on the fly
        List<TransportResponseDTO> filteredTransports = transportPage.getContent().stream()
                .map(transport -> enrichWithLiveSeats(transport, date))
                .filter(dto -> {
                    if (dto.getSeats() == null) return false;
                    return dto.getSeats().stream().anyMatch(seat ->
                        (min == null || seat.getPrice() >= min) &&
                        (max == null || seat.getPrice() <= max)
                    );
                })
                .toList();

        return new org.springframework.data.domain.PageImpl<>(filteredTransports, pageable, filteredTransports.size());
    }
    
    
    
    private TransportResponseDTO enrichWithLiveSeats(
            Transport transport,
            LocalDate bookingDate) {

        TransportResponseDTO responseDTO =
                transportMapper.toResponse(transport);

        if (responseDTO.getSeats() != null) {

            for (TransportSeatDTO seatDTO : responseDTO.getSeats()) {
                // Apply Dynamic Urgency Pricing
                double dynamicPrice = bookingHelper.calculateUrgencyPrice(seatDTO.getPrice(), bookingDate);
                seatDTO.setPrice(dynamicPrice);

                int bookedSeats =
                        bookingRepo.getBookedTransportSeats(
                                transport.getTransportId(),
                                seatDTO.getTransportClass(),
                                bookingDate);

                int remainingSeats =
                        Math.max(
                                0,
                                seatDTO.getTotalSeats() - bookedSeats);

                seatDTO.setAvailableSeats(remainingSeats);
            }
        }

        return responseDTO;
    }

}
