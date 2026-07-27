package com.cts.mapper;

import org.springframework.stereotype.Component;

import com.cts.dto.PartnerDTO;
import com.cts.dto.PartnerResponseDTO;
import com.cts.entity.Partner;

/**
 * Maps between {@link Partner} entities and DTOs. Stateless.
 */
@Component
public class PartnerMapper {

    public Partner toEntity(PartnerDTO dto) {
        return Partner.builder()
                .name(dto.getName())
                .type(dto.getType())
                .status(dto.getStatus())
                .build();
    }

    public void updateEntity(Partner partner, PartnerDTO dto) {
        partner.setName(dto.getName());
        partner.setType(dto.getType());
        partner.setStatus(dto.getStatus());
    }

    public PartnerResponseDTO toResponse(Partner partner) {
        return PartnerResponseDTO.builder()
                .partnerId(partner.getPartnerId())
                .name(partner.getName())
                .type(partner.getType())
                .status(partner.getStatus())
                .build();
    }
}
