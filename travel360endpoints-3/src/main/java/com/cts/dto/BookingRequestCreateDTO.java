package com.cts.dto;

import com.cts.enums.BookingRequestType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestCreateDTO {
    @NotNull(message = "Request type is required")
    private BookingRequestType type;

    @NotNull(message = "Budget is required")
    @Positive(message = "Budget must be a positive number")
    private Double budget;

    @NotBlank(message = "Request details are required")
    private String requestDetails;
}
