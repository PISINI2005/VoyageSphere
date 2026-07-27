package com.cts.service;

import com.cts.dto.PassengerProfileRequestDTO;
import com.cts.dto.PassengerProfileResponseDTO;

import java.util.List;

public interface PassengerProfileService {

    PassengerProfileResponseDTO createProfile(PassengerProfileRequestDTO dto);

    List<PassengerProfileResponseDTO> getMyProfiles();

    PassengerProfileResponseDTO getProfileById(Long id);

    PassengerProfileResponseDTO updateProfile(Long id, PassengerProfileRequestDTO dto);

    void deleteProfile(Long id);
}
