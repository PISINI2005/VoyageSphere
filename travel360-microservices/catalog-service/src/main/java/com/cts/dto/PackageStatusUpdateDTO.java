package com.cts.dto;

import com.cts.enums.PackageStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PackageStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private PackageStatus status;
}
