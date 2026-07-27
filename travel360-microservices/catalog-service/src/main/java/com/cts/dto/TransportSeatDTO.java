package com.cts.dto;

import com.cts.enums.TransportClass;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportSeatDTO {

    @NotNull(message = "Transport class is required")
    private TransportClass transportClass;

    @NotNull(message = "Price cannot be empty")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private Double price;

    @NotNull(message = "Total seats cannot be empty")
    @Min(value = 1, message = "Total seats must be at least 1")
    private Integer totalSeats;
}
