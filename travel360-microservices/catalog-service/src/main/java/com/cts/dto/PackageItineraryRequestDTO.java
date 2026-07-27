package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PackageItineraryRequestDTO {

    private String notes;

    @NotBlank(message = "Detailed description is required")
    private String detailedDescription;

    private String keyHighlights;
    private String guideName;
    private String supportContact;

    @NotBlank(message = "Day wise plan is required")
    private String dayWisePlan;

    @NotNull(message = "Package ID is required")
    private Long packageId;
}
