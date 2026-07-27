package com.cts.mapper;

import com.cts.dto.PassengerProfileRequestDTO;
import com.cts.dto.PassengerProfileResponseDTO;
import com.cts.entity.PassengerProfile;
import com.cts.enums.PassengerProfileStatus;
import com.cts.util.MaskUtil;
import org.springframework.stereotype.Component;

@Component
public class PassengerProfileMapper {

    public PassengerProfile toEntity(PassengerProfileRequestDTO dto, Long userId) {
        return PassengerProfile.builder()
                .passengerName(dto.getPassengerName())
                .dateOfBirth(dto.getDateOfBirth())
                .gender(dto.getGender())
                .contactNo(dto.getContactNo())
                .emailAddress(dto.getEmailAddress())
                .nationality(dto.getNationality())
                .identificationType(dto.getIdentificationType())
                .identificationNumber(dto.getIdentificationNumber())
                .status(PassengerProfileStatus.ACTIVE)
                .userId(userId)
                .build();
    }

    public PassengerProfileResponseDTO toResponse(PassengerProfile profile) {
        return PassengerProfileResponseDTO.builder()
                .passengerProfileId(profile.getPassengerProfileId())
                .passengerName(profile.getPassengerName())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .contactNo(profile.getContactNo())
                .emailAddress(profile.getEmailAddress())
                .nationality(profile.getNationality())
                .identificationType(profile.getIdentificationType())
                .identificationNumber(MaskUtil.maskId(profile.getIdentificationNumber()))
                .build();
    }
}
