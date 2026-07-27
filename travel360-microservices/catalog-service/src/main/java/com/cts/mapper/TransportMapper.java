package com.cts.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.cts.dto.TransportDTO;
import com.cts.dto.TransportResponseDTO;
import com.cts.dto.TransportSeatDTO;
import com.cts.entity.Partner;
import com.cts.entity.Transport;
import com.cts.entity.TransportSeat;

/**
 * Maps between {@link Transport} entities (and their seat classes) and DTOs.
 * Stateless and side-effect free except for {@link #applySeats}, which
 * mutates the managed entity's seat collection by design.
 */
@Component
public class TransportMapper {

    public Transport toEntity(TransportDTO dto, Partner partner) {
        Transport transport = Transport.builder()
                .transportNumber(dto.getTransportNumber())
                .source(dto.getSource())
                .destination(dto.getDestination())
                .transportType(dto.getTransportType())
                .departureTime(dto.getDepartureTime())
                .arrivalTime(dto.getArrivalTime())
                .transportStatus(dto.getTransportStatus())
                .partner(partner)
                .build();

        applySeats(transport, dto.getSeats());

        return transport;
    }

    public void updateEntity(Transport transport, TransportDTO dto, Partner partner) {
        transport.setTransportNumber(dto.getTransportNumber());
        transport.setSource(dto.getSource());
        transport.setDestination(dto.getDestination());
        transport.setTransportType(dto.getTransportType());
        transport.setDepartureTime(dto.getDepartureTime());
        transport.setArrivalTime(dto.getArrivalTime());
        transport.setTransportStatus(dto.getTransportStatus());
        transport.setPartner(partner);

        applySeats(transport, dto.getSeats());
    }

    public TransportResponseDTO toResponse(Transport t) {
        return TransportResponseDTO.builder()
                .transportId(t.getTransportId())
                .transportNumber(t.getTransportNumber())
                .source(t.getSource())
                .destination(t.getDestination())
                .transportType(t.getTransportType())
                .departureTime(t.getDepartureTime())
                .arrivalTime(t.getArrivalTime())
                .transportStatus(t.getTransportStatus())
                .seats(mapSeats(t))
                .build();
    }

    // Replaces the transport's seat classes with the ones from the request.
    // orphanRemoval on Transport.seats deletes any classes no longer present.
    public void applySeats(Transport transport, List<TransportSeatDTO> seatDtos) {

        transport.getSeats().clear();

        if (seatDtos == null) {
            return;
        }

        for (TransportSeatDTO s : seatDtos) {
            transport.getSeats().add(TransportSeat.builder()
                    .transportClass(s.getTransportClass())
                    .price(s.getPrice())
                    .totalSeats(s.getTotalSeats())
                    .transport(transport)
                    .build());
        }
    }

    private List<TransportSeatDTO> mapSeats(Transport transport) {

        if (transport.getSeats() == null) {
            return List.of();
        }

        return transport.getSeats().stream()
                .map(s -> TransportSeatDTO.builder()
                        .transportClass(s.getTransportClass())
                        .price(s.getPrice())
                        .totalSeats(s.getTotalSeats())
                        .build())
                .toList();
    }
}
