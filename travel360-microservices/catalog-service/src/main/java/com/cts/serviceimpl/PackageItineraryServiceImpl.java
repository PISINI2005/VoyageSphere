package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.PackageItineraryRequestDTO;
import com.cts.dto.PackageItineraryResponceDTO;
import com.cts.entity.PackageItinerary;
import com.cts.entity.TravelPackage;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.exception.PackageItineraryNotFound;
import com.cts.exception.PackageNotFoundException;
import com.cts.mapper.PackageItineraryMapper;
import com.cts.repository.PackageItineraryRepository;
import com.cts.repository.TravelPackageRepository;
import com.cts.service.AuditLogService;
import com.cts.service.PackageItineraryService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PackageItineraryServiceImpl implements PackageItineraryService {

    private final PackageItineraryRepository itineraryRepository;
    private final TravelPackageRepository travelPackageRepository;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;
    private final PackageItineraryMapper packageItineraryMapper;

    @Override
    public PackageItinerary save(PackageItineraryRequestDTO dto) {
        log.info("Saving package itinerary for packageId: {}", dto.getPackageId());

        TravelPackage pkg = travelPackageRepository.findById(dto.getPackageId())
                .orElseThrow(() -> {
                    log.error("Package not found with id {}", dto.getPackageId());
                    return new PackageNotFoundException("Package not found with id: " + dto.getPackageId());
                });

        PackageItinerary itinerary = packageItineraryMapper.toEntity(dto, pkg);

        PackageItinerary saved = itineraryRepository.save(itinerary);
        auditLogService.logAction(AuditActions.CREATE_PACKAGE_ITINERARY, AuditEntity.ITINERARY, saved.getPackageItineraryId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Package itinerary created successfully with ID: {}", saved.getPackageItineraryId());

        return saved;
    }

    @Override
    public List<PackageItinerary> getAll() {
        log.info("Fetching all package itineraries");

        List<PackageItinerary> itineraries = itineraryRepository.findAll();

        log.info("Total package itineraries fetched: {}", itineraries.size());

        return itineraries;
    }

    @Override
    public PackageItineraryResponceDTO getItineraryById(Long id) {
        log.info("Fetching package itinerary with ID: {}", id);

        PackageItinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Package itinerary not found with id {}", id);
                    return new PackageItineraryNotFound("Itinerary not found with id: " + id);
                });

        return packageItineraryMapper.toResponse(itinerary);
    }

    @Override
    public void delete(Long id) {
        log.info("Deleting package itinerary with ID: {}", id);

        if (!itineraryRepository.existsById(id)) {
            log.error("Package itinerary not found with id {}", id);
            throw new PackageItineraryNotFound("Itinerary not found with id: " + id);
        }
        itineraryRepository.deleteById(id);
        auditLogService.logAction(AuditActions.DELETE_PACKAGE_ITINERARY, AuditEntity.ITINERARY, id, authUser.currentOrNull(), LogType.WARN);

        log.info("Package itinerary deleted successfully with ID: {}", id);
    }

    @Override
    public PackageItinerary update(Long id, PackageItineraryRequestDTO dto) {
        log.info("Updating package itinerary with ID: {}", id);

        PackageItinerary existing = itineraryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Package itinerary not found with id {}", id);
                    return new PackageItineraryNotFound("Itinerary not found with id: " + id);
                });

        existing.setNotes(dto.getNotes());
        existing.setDetailedDescription(dto.getDetailedDescription());
        existing.setKeyHighlights(dto.getKeyHighlights());
        existing.setGuideName(dto.getGuideName());
        existing.setSupportContact(dto.getSupportContact());
        existing.setDayWisePlan(dto.getDayWisePlan());

        PackageItinerary updated = itineraryRepository.save(existing);
        auditLogService.logAction(AuditActions.UPDATE_PACKAGE_ITINERARY, AuditEntity.ITINERARY, updated.getPackageItineraryId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Package itinerary updated successfully with ID: {}", id);

        return updated;
    }

}
