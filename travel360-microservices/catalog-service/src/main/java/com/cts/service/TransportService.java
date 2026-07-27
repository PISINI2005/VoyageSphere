package com.cts.service;


import java.util.List;

import org.springframework.data.domain.Page;

import com.cts.dto.TransportDTO;
import com.cts.dto.TransportResponseDTO;
import com.cts.entity.Transport;
import com.cts.enums.TransportStatus;

import jakarta.validation.constraints.Min;

public interface TransportService {

    Transport addTransport(TransportDTO dto);

    Transport updateTransport(Long id, TransportDTO dto);

    Transport updateTransportStatus(Long id, TransportStatus status);

    List<TransportResponseDTO> getAllTransports(int page,int size);

    List<TransportResponseDTO> findByRoute(String source, String destination,int page,int size);

    List<TransportResponseDTO> findByStatus(TransportStatus status, int page,int size);

    void deleteTransport(Long id);

	TransportResponseDTO getTransportById(@Min(1) Long id);
}
