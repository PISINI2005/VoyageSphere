package com.cts.service;

import com.cts.dto.BookingHotelDTO;
import com.cts.dto.BookingHotelResponseDTO;

public interface HotelBookingService {

    BookingHotelResponseDTO createHotelBooking(BookingHotelDTO dto);
}
