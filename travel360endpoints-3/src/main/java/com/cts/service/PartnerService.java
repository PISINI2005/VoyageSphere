package com.cts.service;

import java.util.List;

import com.cts.dto.PartnerDTO;
import com.cts.dto.PartnerResponseDTO;
import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;

public interface PartnerService {

    PartnerResponseDTO createPartner(PartnerDTO dto);

    PartnerResponseDTO updatePartner(Long id, PartnerDTO dto);

    PartnerResponseDTO updatePartnerStatus(Long id, PartnerStatus status);

    void deletePartner(Long id);

    List<PartnerResponseDTO> getPartnerByCategory(PartnerType type);

    PartnerResponseDTO getPartnerById(Long id);

    List<PartnerResponseDTO> getAll();
}