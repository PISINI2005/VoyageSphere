package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.cts.config.FeignClientConfig;
import com.cts.dto.FlightResponseDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.dto.TransportResponseDTO;
import com.cts.dto.TravelPackageResponseDTO;

@FeignClient(name = "CATALOG-SERVICE", configuration = FeignClientConfig.class, fallback = CatalogClientFallback.class)
public interface CatalogClient {

    @GetMapping("/api/v1/flights/{id}")
    FlightResponseDTO getFlight(@PathVariable Long id);

    @GetMapping("/api/v1/hotels/{id}")
    HotelResponseDTO getHotel(@PathVariable Long id);

    @GetMapping("/api/v1/transports/{id}")
    TransportResponseDTO getTransport(@PathVariable Long id);

    @GetMapping("/api/v1/packages/{id}")
    TravelPackageResponseDTO getPackage(@PathVariable Long id);
}
