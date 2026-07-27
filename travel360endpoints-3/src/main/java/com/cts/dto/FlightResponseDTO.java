package com.cts.dto;

import java.time.LocalTime;
import java.util.List;

import com.cts.enums.FlightStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlightResponseDTO {

    private Long flightId;
    private String flightNumber;
    private String airlineName;

    private String source;
    private String destination;

    private LocalTime arrivalTime;
    private LocalTime departureTime;

    private FlightStatus status;

    private List<FlightSeatDTO> seats;
}
