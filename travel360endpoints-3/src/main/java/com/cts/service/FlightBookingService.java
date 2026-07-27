package com.cts.service;

import com.cts.dto.BookingFlightDTO;
import com.cts.dto.BookingFlightResponseDTO;

public interface FlightBookingService {
    BookingFlightResponseDTO createFlightBooking(BookingFlightDTO dto);
}
