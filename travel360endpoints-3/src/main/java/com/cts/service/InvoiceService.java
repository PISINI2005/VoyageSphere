package com.cts.service;

import com.cts.dto.InvoiceDTO;
import com.cts.dto.InvoiceResponseDTO;
import com.cts.entity.Invoice;

import java.util.List;

import org.springframework.data.domain.Page;

public interface InvoiceService {

    InvoiceResponseDTO createInvoice(InvoiceDTO dto);

    List<InvoiceResponseDTO> getInvoicesByBooking(Long bookingId);

    Page<InvoiceResponseDTO> getInvoicesByUser(
            Long userId,
            int page,
            int size);

    List<InvoiceResponseDTO> getAllInvoices();

    InvoiceResponseDTO getInvoiceById(Long id);
}
