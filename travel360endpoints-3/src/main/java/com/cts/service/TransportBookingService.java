package com.cts.service;

import com.cts.dto.BookingTransportDTO;
import com.cts.dto.BookingTransportResponseDTO;

public interface TransportBookingService {
    BookingTransportResponseDTO createTransportBooking(BookingTransportDTO dto);
}
