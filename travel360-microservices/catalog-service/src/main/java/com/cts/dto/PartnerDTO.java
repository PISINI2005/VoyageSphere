package com.cts.dto;

import com.cts.enums.PartnerStatus;
import com.cts.enums.PartnerType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerDTO {
@NotBlank(message = "name cannot be blank")
    private String name;

    @NotNull(message = "partner type is required")
    private PartnerType type;

    @NotNull(message = "partner status is required")
    private PartnerStatus status;
}


