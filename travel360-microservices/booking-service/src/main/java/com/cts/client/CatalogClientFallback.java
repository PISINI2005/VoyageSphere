package com.cts.client;

import com.cts.dto.*;
import org.springframework.stereotype.Component;

@Component
public class CatalogClientFallback implements CatalogClient {
    @Override
    public FlightResponseDTO getFlight(Long id) {
        return FlightResponseDTO.builder().flightId(id).flightNumber("N/A").build();
    }

    @Override
    public HotelResponseDTO getHotel(Long id) {
        return HotelResponseDTO.builder().hotelId(id).hotelName("Unknown Hotel").build();
    }

    @Override
    public TransportResponseDTO getTransport(Long id) {
        return TransportResponseDTO.builder().transportId(id).transportType("Unknown").build();
    }

    @Override
    public TravelPackageResponseDTO getPackage(Long id) {
        return TravelPackageResponseDTO.builder().packageId(id).packageName("Unknown Package").build();
    }
}
