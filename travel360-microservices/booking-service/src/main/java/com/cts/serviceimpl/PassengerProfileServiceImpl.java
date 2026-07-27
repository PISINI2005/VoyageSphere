package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.PassengerProfileRequestDTO;
import com.cts.dto.PassengerProfileResponseDTO;
import com.cts.entity.PassengerProfile;
import com.cts.enums.AuditEntity;
import com.cts.enums.IdentificationType;
import com.cts.enums.LogType;
import com.cts.enums.Nationality;
import com.cts.enums.PassengerProfileStatus;
import com.cts.exception.InvalidPassengerException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.PassengerProfileMapper;
import com.cts.repository.PassengerProfileRepository;
import com.cts.service.AuditLogService;
import com.cts.service.PassengerProfileService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class PassengerProfileServiceImpl implements PassengerProfileService {

    private final PassengerProfileRepository profileRepo;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;
    private final PassengerProfileMapper profileMapper;

    @Override
    @Transactional
    public PassengerProfileResponseDTO createProfile(PassengerProfileRequestDTO dto) {
        Long userId = authUser.current().getUserId();
        log.info("Creating passenger profile for userId: {}", userId);

        validateIdentification(dto);
        PassengerProfile profile = profileMapper.toEntity(dto, userId);
        profile = profileRepo.save(profile);
        auditLogService.logAction(AuditActions.CREATE_PASSENGER_PROFILE, AuditEntity.PASSENGER_PROFILE,
                profile.getPassengerProfileId(), userId, LogType.INFO);

        log.info("Passenger profile created with id: {}", profile.getPassengerProfileId());
        return profileMapper.toResponse(profile);
    }

    @Override
    public List<PassengerProfileResponseDTO> getMyProfiles() {
        Long userId = authUser.current().getUserId();
        log.info("Fetching profiles for userId: {}", userId);
        return profileRepo.findByUserIdAndStatus(userId, PassengerProfileStatus.ACTIVE)
                .stream().map(profileMapper::toResponse).toList();
    }

    @Override
    public PassengerProfileResponseDTO getProfileById(Long id) {
        log.info("Fetching passenger profile id: {}", id);
        PassengerProfile profile = profileRepo
                .findByPassengerProfileIdAndUserId(id, authUser.current().getUserId())
                .orElseThrow(() -> {
                    log.error("Passenger profile {} not found for current user", id);
                    return new ResourceNotFoundException("Passenger profile not found");
                });
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public PassengerProfileResponseDTO updateProfile(Long id, PassengerProfileRequestDTO dto) {
        Long userId = authUser.current().getUserId();
        log.info("Updating passenger profile id: {}", id);
        PassengerProfile profile = profileRepo
                .findByPassengerProfileIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.error("Passenger profile {} not found for current user", id);
                    return new ResourceNotFoundException("Passenger profile not found");
                });

        validateIdentification(dto);
        profile.setPassengerName(dto.getPassengerName());
        profile.setDateOfBirth(dto.getDateOfBirth());
        profile.setGender(dto.getGender());
        profile.setContactNo(dto.getContactNo());
        profile.setEmailAddress(dto.getEmailAddress());
        profile.setNationality(dto.getNationality());
        profile.setIdentificationType(dto.getIdentificationType());
        profile.setIdentificationNumber(dto.getIdentificationNumber());

        profile = profileRepo.save(profile);
        auditLogService.logAction(AuditActions.UPDATE_PASSENGER_PROFILE, AuditEntity.PASSENGER_PROFILE,
                profile.getPassengerProfileId(), userId, LogType.INFO);

        log.info("Passenger profile {} updated", id);
        return profileMapper.toResponse(profile);
    }

    @Override
    @Transactional
    public void deleteProfile(Long id) {
        Long userId = authUser.current().getUserId();
        log.info("Soft-deleting passenger profile id: {}", id);
        PassengerProfile profile = profileRepo
                .findByPassengerProfileIdAndUserId(id, userId)
                .orElseThrow(() -> {
                    log.error("Passenger profile {} not found for current user", id);
                    return new ResourceNotFoundException("Passenger profile not found");
                });

        profile.setStatus(PassengerProfileStatus.INACTIVE);
        profileRepo.save(profile);
        auditLogService.logAction(AuditActions.DELETE_PASSENGER_PROFILE, AuditEntity.PASSENGER_PROFILE,
                profile.getPassengerProfileId(), userId, LogType.INFO);

        log.info("Passenger profile {} soft-deleted", id);
    }

    /**
     * Enforces the identity-document business rules. Field presence (non-null type,
     * nationality and number) is already guaranteed by the DTO's bean-validation
     * annotations, so this only covers the cross-field and type-specific format rules:
     * <ul>
     *   <li>Foreign nationals must use a PASSPORT.</li>
     *   <li>The identification number must match its type's format
     *       (see {@link IdentificationType#matches(String)}).</li>
     * </ul>
     * Violations attach to the {@code identificationNumber} field so they surface in the
     * same {@code validationErrors} shape as bean-validation failures.
     */
    private void validateIdentification(PassengerProfileRequestDTO dto) {
        Nationality nationality = dto.getNationality();
        IdentificationType type = dto.getIdentificationType();
        String number = dto.getIdentificationNumber();

        if (nationality == Nationality.FOREIGN && type != IdentificationType.PASSPORT) {
            log.warn("Rejected profile: FOREIGN national with non-passport document {}", type);
            throw new InvalidPassengerException("identificationNumber",
                    "Foreign nationals must provide a PASSPORT");
        }

        if (!type.matches(number)) {
            log.warn("Rejected profile: identification number does not match {} format", type);
            throw new InvalidPassengerException("identificationNumber",
                    "Invalid " + type + " number format");
        }
    }
}
