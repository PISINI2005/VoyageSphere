package com.cts.service;

import com.cts.dto.BookingCancelDTO;
import com.cts.dto.BookingCancelResponseDTO;
import com.cts.dto.BookingDTO;
import com.cts.dto.BookingFlightDTO;
import com.cts.dto.BookingFlightResponseDTO;
import com.cts.dto.BookingHotelDTO;
import com.cts.dto.BookingHotelResponseDTO;
import com.cts.dto.BookingPackageDTO;
import com.cts.dto.BookingPackageResponseDTO;
import com.cts.dto.BookingResponseDTO;
import com.cts.dto.BookingTransportDTO;
import com.cts.dto.BookingTransportResponseDTO;
import com.cts.dto.PassengerCancelResponseDTO;
import com.cts.entity.Booking;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookingService {

	BookingFlightResponseDTO createFlightBooking(BookingFlightDTO dto);

	BookingHotelResponseDTO createHotelBooking(BookingHotelDTO dto);

	BookingPackageResponseDTO createPackageBooking(BookingPackageDTO dto);
	
	BookingTransportResponseDTO createTransportBooking(BookingTransportDTO dto);

	Page<BookingResponseDTO> getBookings(Long userId, Pageable pageable);

	Page<BookingResponseDTO> getAllBookings(Pageable pageable);
	

	BookingCancelResponseDTO deleteBooking(BookingCancelDTO dto);

	PassengerCancelResponseDTO cancelPassenger(Long bookingId, Long passengerId);

	BookingResponseDTO getBookingById(Long bookingId);
}