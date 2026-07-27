package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.PartnerDTO;
import com.cts.dto.PartnerResponseDTO;
import com.cts.entity.Flight;
import com.cts.entity.Hotel;
import com.cts.entity.Partner;
import com.cts.entity.Transport;
import com.cts.entity.TravelPackage;
import com.cts.enums.AuditEntity;
import com.cts.enums.FlightStatus;
import com.cts.enums.HotelStatus;
import com.cts.enums.LogType;
import com.cts.enums.PackageStatus;
import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;
import com.cts.enums.TransportStatus;
import com.cts.exception.PartnerNotFoundException;
import com.cts.mapper.PartnerMapper;
import com.cts.repository.FlightRepository;
import com.cts.repository.HotelRepository;
import com.cts.repository.PartnerRepository;
import com.cts.repository.TransportRepository;
import com.cts.repository.TravelPackageRepository;
import com.cts.service.AuditLogService;
import com.cts.service.PartnerService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PartnerServiceImpl implements PartnerService {

    private final PartnerRepository partnerRepo;
    private final FlightRepository flightRepo;
    private final HotelRepository hotelRepo;
    private final TransportRepository transportRepo;
    private final TravelPackageRepository packageRepo;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;
    private final PartnerMapper partnerMapper;

   
    @Override
    public PartnerResponseDTO getPartnerById(Long id) {

        log.info("Fetching partner with ID: {}", id);

        Partner partner = partnerRepo.findById(id).orElseThrow(() -> {
            log.error("Partner not found with id {}", id);
            return new PartnerNotFoundException("Partner not found");
        });

        return partnerMapper.toResponse(partner);
    }

   
    @Override
    @Transactional
    public PartnerResponseDTO updatePartner(Long id, PartnerDTO dto) {

        log.info("Updating partner with ID: {}", id);

        Partner partner = partnerRepo.findById(id).orElseThrow(() -> {
            log.error("Partner not found with id {}", id);
            return new PartnerNotFoundException("Partner not found");
        });

        partnerMapper.updateEntity(partner, dto);

        partner = partnerRepo.save(partner);
        auditLogService.logAction(AuditActions.UPDATE_PARTNER, AuditEntity.PARTNER, partner.getPartnerId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Partner updated successfully with ID: {}", partner.getPartnerId());

        if (isDisabled(partner.getStatus())) {
            log.debug("Partner {} is not ACTIVE, deactivating inventory", partner.getPartnerId());
            deactivateInventory(partner);
        }

        return partnerMapper.toResponse(partner);
    }

   
    @Override
    @Transactional
    public PartnerResponseDTO updatePartnerStatus(Long id, PartnerStatus status) {

        log.info("Updating status for partner with ID: {}", id);

        Partner partner = partnerRepo.findById(id).orElseThrow(() -> {
            log.error("Partner not found with id {}", id);
            return new PartnerNotFoundException("Partner not found");
        });

        partner.setStatus(status);

        partner = partnerRepo.save(partner);

        log.info("Partner status updated successfully with ID: {}", partner.getPartnerId());

        if (isDisabled(partner.getStatus())) {
            log.debug("Partner {} is not ACTIVE, deactivating inventory", partner.getPartnerId());
            deactivateInventory(partner);
        }

        return partnerMapper.toResponse(partner);
    }

    
    @Override
    @Transactional
    public void deletePartner(Long id) {

        log.info("Deleting (deactivating) partner with ID: {}", id);

        // Soft-delete sets the partner INACTIVE; updatePartnerStatus also cascades the
        // inventory deactivation, so the rule lives in exactly one place.
        updatePartnerStatus(id, PartnerStatus.INACTIVE);
        auditLogService.logAction(AuditActions.DELETE_PARTNER, AuditEntity.PARTNER, id, authUser.currentOrNull(), LogType.WARN);

        log.info("Partner deactivated successfully with ID: {}", id);
    }

    // A partner that is not ACTIVE can no longer sell inventory.
    private boolean isDisabled(PartnerStatus status) {
        return status != PartnerStatus.ACTIVE;
    }

    // Cascade: take all of this partner's inventory out of sale.
    private void deactivateInventory(Partner partner) {

        log.debug("Deactivating inventory for partner {} of type {}",
                partner.getPartnerId(), partner.getType());

        switch (partner.getType()) {

            case FLIGHT -> {
                List<Flight> flights = flightRepo.findByPartner(partner);
                flights.forEach(f -> f.setStatus(FlightStatus.CANCELLED));
                flightRepo.saveAll(flights);
            }
            case HOTEL -> {
                List<Hotel> hotels = hotelRepo.findByPartner(partner);
                hotels.forEach(h -> h.setStatus(HotelStatus.INACTIVE));
                hotelRepo.saveAll(hotels);
            }
            case BUS -> {
                List<Transport> transports = transportRepo.findByPartner(partner);
                transports.forEach(t -> t.setTransportStatus(TransportStatus.OUT_OF_SERVICE));
                transportRepo.saveAll(transports);
            }
            case PACKAGE -> {
                List<TravelPackage> packages = packageRepo.findByPartner(partner);
                packages.forEach(p -> p.setStatus(PackageStatus.INACTIVE));
                packageRepo.saveAll(packages);
            }
        }
    }

    
    @Override
    public List<PartnerResponseDTO> getPartnerByCategory(PartnerType type) {

        log.info("Fetching partners by category: {}", type);

        List<Partner> list = partnerRepo.findByType(type);

        log.info("Found {} partners for category {}", list.size(), type);

        return list.stream()
                .map(partnerMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PartnerResponseDTO createPartner(PartnerDTO dto) {

    log.info("Creating new partner of type: {}", dto.getType());

    Partner partner = partnerMapper.toEntity(dto);
    partner.setStatus(PartnerStatus.ACTIVE);

    partner = partnerRepo.save(partner);
    auditLogService.logAction(AuditActions.CREATE_PARTNER, AuditEntity.PARTNER, partner.getPartnerId(), authUser.currentOrNull(), LogType.INFO);

    log.info("Partner created successfully with ID: {}", partner.getPartnerId());

    return partnerMapper.toResponse(partner);
}


@Override
@Transactional
public List<PartnerResponseDTO> getAll(){
    List<Partner> list = partnerRepo.findAll();
    log.info("Get All partners");
    
    return list.stream()
                .map(partnerMapper::toResponse)
                .toList();
}
}
