package com.cts.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cts.dto.PassengerResponseDTO;
import com.cts.entity.Passenger;
import com.cts.entity.PassengerProfile;
import com.cts.util.MaskUtil;

/**
 * Maps {@link Passenger} entities to response DTOs. Pure and stateless —
 * profile resolution/creation is owned by the service layer (PassengerResolver).
 */
@Component
public class PassengerMapper {

    public List<PassengerResponseDTO> toResponses(List<Passenger> passengers) {
        return passengers.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public PassengerResponseDTO toResponse(Passenger p) {
        PassengerProfile profile = p.getProfile();
        return PassengerResponseDTO.builder()
                .passengerId(p.getPassengerId())
                .passengerProfileId(profile.getPassengerProfileId())
                .passengerName(profile.getPassengerName())
                .dateOfBirth(profile.getDateOfBirth())
                .gender(profile.getGender())
                .contactNo(profile.getContactNo())
                .emailAddress(profile.getEmailAddress())
                .nationality(profile.getNationality())
                .identificationType(profile.getIdentificationType())
                .identificationNumber(MaskUtil.maskId(profile.getIdentificationNumber()))
                .status(p.getStatus())
                .build();
    }
}
