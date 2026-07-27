package com.cts.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.cts.dto.PackageItineraryRequestDTO;
import com.cts.dto.PackageItineraryResponceDTO;
import com.cts.entity.PackageItinerary;
import com.cts.entity.TravelPackage;

/**
 * Maps between {@link PackageItinerary} entities and DTOs. Stateless.
 */
@Component
public class PackageItineraryMapper {

    public PackageItinerary toEntity(PackageItineraryRequestDTO dto, TravelPackage pkg) {
        return PackageItinerary.builder()
                .notes(dto.getNotes())
                .detailedDescription(dto.getDetailedDescription())
                .keyHighlights(dto.getKeyHighlights())
                .guideName(dto.getGuideName())
                .supportContact(dto.getSupportContact())
                .dayWisePlan(dto.getDayWisePlan())
                .createdAt(LocalDateTime.now())
                .travelPackage(pkg)
                .build();
    }

    public PackageItineraryResponceDTO toResponse(PackageItinerary itinerary) {
        TravelPackage pkg = itinerary.getTravelPackage();

        return new PackageItineraryResponceDTO(
                pkg.getPackageId(),
                pkg.getPackageName(),
                pkg.getDescription(),
                pkg.getDurationDays(),
                pkg.getPrice(),
                pkg.getStatus(),
                pkg.getDestination(),
                itinerary.getPackageItineraryId(),
                itinerary.getNotes(),
                itinerary.getCreatedAt(),
                itinerary.getDetailedDescription(),
                itinerary.getKeyHighlights(),
                itinerary.getGuideName(),
                itinerary.getSupportContact(),
                itinerary.getDayWisePlan()
        );
    }
}
