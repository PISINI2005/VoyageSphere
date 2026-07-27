package com.cts.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransportStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private com.cts.enums.TransportStatus status;
}
