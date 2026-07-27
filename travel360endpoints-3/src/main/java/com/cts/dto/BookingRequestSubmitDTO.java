package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestSubmitDTO {
    @NotBlank(message = "Agent remarks are required")
    private String agentRemarks;

    @NotEmpty(message = "At least one linked booking ID is required")
    private List<Long> linkedBookingIds;
}
