package com.cts.dto;

import java.time.LocalTime;
import java.util.List;

import com.cts.enums.TransportStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransportResponseDTO {

    private Long transportId;
    private int transportNumber;
    private String source;
    private String destination;
    private String transportType;
    private LocalTime departureTime;
    private LocalTime arrivalTime;

    private TransportStatus transportStatus;

    private List<TransportSeatDTO> seats;
}
