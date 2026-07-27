package com.cts.dto;

import lombok.Data;

import java.time.LocalTime;
import java.util.List;

import com.cts.enums.FlightStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
public class FlightDTO {

    @NotBlank(message = "Flight number is required")
    @Pattern(regexp = "^[A-Z]{2}-\\d{3}$", message = "Flight number must match standard airline format (e.g., AA-123 or DL-1234)")
    private String flightNumber;

    @NotNull(message = "Partner (airline) id is required")
    private Long partnerId;

    @NotBlank(message = "Source location is required")
    private String source;

    @NotBlank(message = "Destination location is required")
    private String destination;

    private LocalTime arrivalTime;
    private LocalTime departureTime;

    @NotNull(message = "Flight status is required")
    private FlightStatus status;

    // Seat classes define this flight's pricing and inventory.
    // At least one class (e.g. ECONOMY) is required to make the flight bookable.
    @NotEmpty(message = "At least one seat class is required")
    @Valid
    private List<FlightSeatDTO> seats;
}
