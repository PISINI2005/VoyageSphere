package com.cts.service;


import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.cts.dto.TransportDTO;
import com.cts.dto.TransportResponseDTO;
import com.cts.dto.PriceDateDTO;
import com.cts.entity.Transport;
import com.cts.enums.TransportStatus;

public interface TransportService {

    Transport addTransport(TransportDTO dto);

    Transport updateTransport(Long id, TransportDTO dto);

    Transport updateTransportStatus(Long id, TransportStatus status);

  

    TransportResponseDTO getTransportById(Long id, LocalDate date);

    List<PriceDateDTO> getPriceCalendar(Long id, String transportClass, LocalDate startDate, LocalDate endDate);

    Page<TransportResponseDTO> getAllTransports(int page, int size);

    Page<TransportResponseDTO> findByRoute(String source, String destination, int page, int size);

    Page<TransportResponseDTO> findByStatus(TransportStatus status, int page, int size);

    Page<TransportResponseDTO> findByRouteWithAvailability(
    	    String source, 
    	    String destination, 
    	    Double min, 
    	    Double max, 
    	    LocalDate date, 
    	    int page, 
    	    int size
    	);

    void deleteTransport(Long id);
}