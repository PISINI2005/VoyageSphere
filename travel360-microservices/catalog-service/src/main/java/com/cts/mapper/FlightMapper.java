package com.cts.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cts.dto.FlightDTO;
import com.cts.dto.FlightResponseDTO;
import com.cts.dto.FlightSeatDTO;
import com.cts.entity.Flight;
import com.cts.entity.FlightSeat;
import com.cts.entity.Partner;

/**
 * Maps between {@link Flight} entities (and their seat classes) and DTOs.
 * Stateless and side-effect free except for {@link #applySeats}, which
 * mutates the managed entity's seat collection by design.
 */
@Component
public class FlightMapper {

    public Flight toEntity(FlightDTO dto, Partner partner) {
        Flight flight = Flight.builder()
                .flightNumber(dto.getFlightNumber())
                .airlineName(partner.getName())
                .source(dto.getSource())
                .destination(dto.getDestination())
                .arrivalTime(dto.getArrivalTime())
                .departureTime(dto.getDepartureTime())
                .status(dto.getStatus())
                .partner(partner)
                .build();

        applySeats(flight, dto.getSeats());

        return flight;
    }

    public void updateEntity(Flight flight, FlightDTO dto, Partner partner) {
        flight.setFlightNumber(dto.getFlightNumber());
        flight.setAirlineName(partner.getName());
        flight.setSource(dto.getSource());
        flight.setDestination(dto.getDestination());
        flight.setArrivalTime(dto.getArrivalTime());
        flight.setDepartureTime(dto.getDepartureTime());
        flight.setStatus(dto.getStatus());
        flight.setPartner(partner);

        applySeats(flight, dto.getSeats());
    }

    public FlightResponseDTO toResponse(Flight flight) {
        return FlightResponseDTO.builder()
                .flightId(flight.getFlightId())
                .flightNumber(flight.getFlightNumber())
                .airlineName(flight.getAirlineName())
                .source(flight.getSource())
                .destination(flight.getDestination())
                .arrivalTime(flight.getArrivalTime())
                .departureTime(flight.getDepartureTime())
                .status(flight.getStatus())
                .seats(mapSeats(flight))
                .build();
    }

    // Replaces the flight's seat classes with the ones from the request.
    // orphanRemoval on Flight.seats deletes any classes no longer present.
    public void applySeats(Flight flight, List<FlightSeatDTO> seatDtos) {

        flight.getSeats().clear();

        if (seatDtos == null) {
            return;
        }

        for (FlightSeatDTO s : seatDtos) {
            flight.getSeats().add(FlightSeat.builder()
                    .seatType(s.getSeatType())
                    .price(s.getPrice())
                    .totalSeats(s.getTotalSeats())
                    .flight(flight)
                    .build());
        }
    }

    private List<FlightSeatDTO> mapSeats(Flight flight) {

        if (flight.getSeats() == null) {
            return List.of();
        }

        return flight.getSeats().stream()
                .map(s -> FlightSeatDTO.builder()
                        .seatType(s.getSeatType())
                        .price(s.getPrice())
                        .totalSeats(s.getTotalSeats())
                        .build())
                .toList();
    }
}
