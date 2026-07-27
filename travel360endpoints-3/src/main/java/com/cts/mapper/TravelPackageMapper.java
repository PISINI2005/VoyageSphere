package com.cts.mapper;

import org.springframework.stereotype.Component;

import com.cts.dto.TravelPackageDTO;
import com.cts.dto.TravelPackageResponseDTO;
import com.cts.entity.Partner;
import com.cts.entity.TravelPackage;

@Component
public class TravelPackageMapper {

    public TravelPackage toEntity(TravelPackageDTO dto, Partner partner) {
        return TravelPackage.builder()
                .packageName(dto.getPackageName())
                .source(dto.getSource())
                .destination(dto.getDestination())
                .price(dto.getPrice())
                .durationDays(dto.getDurationDays())
                .totalSlots(dto.getTotalSlots())
                .description(dto.getDescription())
                .category(dto.getCategory())
                .status(dto.getStatus())
                .dayWisePlan(dto.getDayWisePlan())
                .partner(partner)
                .build();
    }

    public void updateEntity(TravelPackage tpackage, TravelPackageDTO dto, Partner partner) {
        tpackage.setPackageName(dto.getPackageName());
        tpackage.setSource(dto.getSource());
        tpackage.setDestination(dto.getDestination());
        tpackage.setPrice(dto.getPrice());
        tpackage.setDurationDays(dto.getDurationDays());
        tpackage.setTotalSlots(dto.getTotalSlots());
        tpackage.setDescription(dto.getDescription());
        tpackage.setCategory(dto.getCategory());
        tpackage.setStatus(dto.getStatus());
        tpackage.setDayWisePlan(dto.getDayWisePlan());
        tpackage.setPartner(partner);
    }

    public TravelPackageResponseDTO toResponse(TravelPackage t) {
        return TravelPackageResponseDTO.builder()
                .packageId(t.getPackageId())
                .packageName(t.getPackageName())
                .source(t.getSource())
                .destination(t.getDestination())
                .price(t.getPrice())
                .durationDays(t.getDurationDays())
                .totalSlots(t.getTotalSlots())
                .description(t.getDescription())
                .category(t.getCategory())
                .status(t.getStatus())
                .dayWisePlan(t.getDayWisePlan())
                .build();
    }
}
