package com.cts.serviceimpl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page; // Added import for Page wrapper
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.TravelPackageDTO;
import com.cts.dto.TravelPackageResponseDTO;
import com.cts.entity.Partner;
import com.cts.entity.TravelPackage;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.enums.PackageStatus;
import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;
import com.cts.enums.TravelPackageCategory;
import com.cts.exception.InvalidPartnerException;
import com.cts.exception.PackageNotFoundException;
import com.cts.exception.PartnerNotFoundException;
import com.cts.mapper.TravelPackageMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.PartnerRepository;
import com.cts.repository.TravelPackageRepository;
import com.cts.service.AuditLogService;
import com.cts.service.TravelPackageService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class TravelPackageServiceImpl implements TravelPackageService {

    private final TravelPackageRepository packageRepo;
    private final PartnerRepository partnerRepo;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;
    private final TravelPackageMapper travelPackageMapper;
    private final BookingRepository bookingRepo;

    @Override
    public TravelPackage addPackage(TravelPackageDTO dto) {

        log.info("Adding new travel package with partnerId: {}", dto.getPartnerId());

        Partner partner = partnerRepo.findById(dto.getPartnerId())
                .orElseThrow(() -> {
                    log.error("Partner not found with id {}", dto.getPartnerId());
                    return new PartnerNotFoundException(
                            "Partner not found with id " + dto.getPartnerId());
                });

        if (partner.getType() != PartnerType.PACKAGE) {
            log.error("Invalid partner type for partnerId: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not a PACKAGE partner");
        }
        if (partner.getStatus() != PartnerStatus.ACTIVE) {
            log.error("Inactive partner: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not active");
        }

        TravelPackage tpackage = travelPackageMapper.toEntity(dto, partner);
        tpackage.setStatus(PackageStatus.AVAILABLE);

        tpackage = packageRepo.save(tpackage);
        auditLogService.logAction(AuditActions.CREATE_PACKAGE, AuditEntity.TRAVELPACKAGE, tpackage.getPackageId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Travel package created successfully with ID: {}", tpackage.getPackageId());

        return tpackage;
    }

    @Override
    @Transactional
    public TravelPackage updatePackage(Long id, TravelPackageDTO dto) {

        log.info("Updating travel package with ID: {}", id);

        TravelPackage tpackage = packageRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Package not found with id {}", id);
                    return new PackageNotFoundException("Package not found");
                });

        Partner partner = partnerRepo.findById(dto.getPartnerId())
                .orElseThrow(() -> {
                    log.error("Partner not found with id {}", dto.getPartnerId());
                    return new PartnerNotFoundException(
                            "Partner not found with id " + dto.getPartnerId());
                });

        if (partner.getType() != PartnerType.PACKAGE) {
            log.error("Invalid partner type for partnerId: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not a PACKAGE partner");
        }
        if (partner.getStatus() != PartnerStatus.ACTIVE) {
            log.error("Inactive partner: {}", partner.getPartnerId());
            throw new InvalidPartnerException(
                    "Partner " + partner.getPartnerId() + " is not active");
        }

        log.debug("Updating travel package details for ID: {}", id);

        travelPackageMapper.updateEntity(tpackage, dto, partner);

        tpackage = packageRepo.save(tpackage);
        auditLogService.logAction(AuditActions.UPDATE_PACKAGE, AuditEntity.TRAVELPACKAGE, tpackage.getPackageId(), authUser.currentOrNull(), LogType.INFO);

        log.info("Travel package updated successfully with ID: {}", id);

        return tpackage;
    }

    @Override
    @Transactional
    public TravelPackage updatePackageStatus(Long id, PackageStatus status) {

        log.info("Updating status of travel package with ID: {} to {}", id, status);

        TravelPackage tpackage = packageRepo.findById(id)
                .orElseThrow(() -> {
                    log.error("Package not found with id {}", id);
                    return new PackageNotFoundException("Package not found");
                });

        tpackage.setStatus(status);

        tpackage = packageRepo.save(tpackage);

        log.info("Travel package status updated successfully with ID: {}", id);

        return tpackage;
    }

    @Override
    public Page<TravelPackageResponseDTO> getAllPackages(int page, int size, Double min, Double max) {

        log.info("Fetching all travel packages (page={}, size={}, min={}, max={})", page, size, min, max);

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPackage> packagePage = packageRepo.findAllWithPriceFilter(min, max, pageable);

        return packagePage.map(travelPackageMapper::toResponse);
    }

    @Override
    public Page<TravelPackageResponseDTO> searchByCategory(TravelPackageCategory category, int page, int size, Double min, Double max) {

        log.info("Searching travel packages by category: {} (page={}, size={}, min={}, max={})", category, page, size, min, max);

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPackage> packagePage = packageRepo.findByCategoryWithPriceFilter(category, min, max, pageable);

        // FIX: Map directly on the Page object to preserve pagination metadata
        return packagePage.map(travelPackageMapper::toResponse);
    }

    @Override
    @Transactional
    public void deletePackage(Long id) {

        log.info("Deleting (deactivating) package with ID: {}", id);

        updatePackageStatus(id, PackageStatus.INACTIVE);
        auditLogService.logAction(AuditActions.DELETE_PACKAGE, AuditEntity.TRAVELPACKAGE,
                id, authUser.currentOrNull(), LogType.WARN);

        log.info("Package {} deactivated successfully", id);
    }

    @Override
    public TravelPackageResponseDTO getTravelPackageById(Long id, LocalDate travelDate) {

        TravelPackage travelPackage = packageRepo.findById(id)
                        .orElseThrow(() -> new PackageNotFoundException("Package not found"));

        if (travelDate != null) {
            return enrichWithAvailability(travelPackage, travelDate);
        }

        return travelPackageMapper.toResponse(travelPackage);
    }

    @Override
    public Page<TravelPackageResponseDTO> searchByCategoryWithAvailability(
            TravelPackageCategory category,
            LocalDate travelDate,
            int page,
            int size,
            Double min,
            Double max) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPackage> packagePage = packageRepo.findByCategoryWithPriceFilter(category, min, max, pageable);

        return packagePage.map(pkg -> enrichWithAvailability(pkg, travelDate));
    }

    @Override
    public Page<TravelPackageResponseDTO> getAllPackagesWithAvailability(
            LocalDate travelDate,
            int page,
            int size,
            Double min,
            Double max) {

        Pageable pageable = PageRequest.of(page, size);
        Page<TravelPackage> packagePage = packageRepo.findAllWithPriceFilter(min, max, pageable);

        return packagePage.map(pkg -> enrichWithAvailability(pkg, travelDate));
    }
    
    private TravelPackageResponseDTO enrichWithAvailability(
            TravelPackage travelPackage,
            LocalDate travelDate) {

        TravelPackageResponseDTO dto = travelPackageMapper.toResponse(travelPackage);

        int bookedSlots = bookingRepo.getBookedSlots(
                    travelPackage.getPackageId(),
                    travelDate);

        log.info("Package={}, TotalSlots={}, BookedSlots={}, Date={}",
            travelPackage.getPackageId(),
            dto.getTotalSlots(),
            bookedSlots,
            travelDate);

        dto.setAvailableSlots(Math.max(0, dto.getTotalSlots() - bookedSlots));

        return dto;
    }

    @Override
    public List<TravelPackageResponseDTO> getPackagesByRoute(String source, String destination, LocalDate travelDate, TravelPackageCategory category, Double min, Double max) {
        log.info("Searching for packages from {} to {} with date {} and category {} (min={}, max={})", source, destination, travelDate, category, min, max);

        List<TravelPackage> packages;
        if (category != null) {
            packages = packageRepo.findBySourceAndDestinationAndCategoryWithPriceFilter(source, destination, category, min, max);
        } else {
            packages = packageRepo.findBySourceAndDestinationWithPriceFilter(source, destination, min, max);
        }

        return packages.stream()
                .map(pkg -> travelDate != null ? enrichWithAvailability(pkg, travelDate) : travelPackageMapper.toResponse(pkg))
                .toList();
    }
}