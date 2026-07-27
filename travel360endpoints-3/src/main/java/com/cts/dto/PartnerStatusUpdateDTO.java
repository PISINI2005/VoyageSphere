package com.cts.dto;

import com.cts.enums.PartnerStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private PartnerStatus status;
}
