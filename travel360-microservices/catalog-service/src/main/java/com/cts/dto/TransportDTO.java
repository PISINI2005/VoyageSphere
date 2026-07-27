package com.cts.dto;

import java.time.LocalTime;
import java.util.List;

import com.cts.enums.TransportStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class TransportDTO {

    @NotNull(message = "Transport number is required")
    @Min(value = 100, message = "Transport number must be at least a 3-digit valid identifier")
    private Integer transportNumber;

    @NotBlank(message = "Source location is required")
    private String source;

    @NotBlank(message = "Destination location is required")
    private String destination;

    @NotBlank(message = "Transport type is required")
    @Pattern(regexp = "^BUS$", message = "Transport type must be BUS")
    private String transportType;

    @NotNull(message = "Departure time is required")
    private LocalTime departureTime;

    @NotNull(message = "Arrival time is required")
    private LocalTime arrivalTime;

    @NotNull(message = "Transport status is required")
    private TransportStatus transportStatus;

    @NotNull(message = "Partner id is required")
    private Long partnerId;

    // Seat classes define this transport's pricing and inventory.
    // At least one class (e.g. SEATER) is required to make it bookable.
    @NotEmpty(message = "At least one seat class is required")
    @Valid
    private List<TransportSeatDTO> seats;
}
