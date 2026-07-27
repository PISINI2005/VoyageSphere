package com.cts.service;

import java.util.List;

import com.cts.dto.PassengerProfileRequestDTO;
import com.cts.dto.PassengerProfileResponseDTO;

public interface PassengerProfileService {

    PassengerProfileResponseDTO createProfile(PassengerProfileRequestDTO dto);

    List<PassengerProfileResponseDTO> getMyProfiles(Long userId);

   PassengerProfileResponseDTO getProfileById(Long id, Long userId);

PassengerProfileResponseDTO updateProfile(Long id,
                                          PassengerProfileRequestDTO dto,
                                          Long userId);

void deleteProfile(Long id, Long userId);
}
