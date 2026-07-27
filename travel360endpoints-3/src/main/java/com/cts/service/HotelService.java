package com.cts.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;

import com.cts.dto.HotelDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.entity.Hotel;
import com.cts.enums.HotelStatus;

public interface HotelService {

	Hotel addHotel(HotelDTO dto);

	Hotel updateHotel(Long id, HotelDTO dto);

	Hotel updateHotelStatus(Long id, HotelStatus status);

	

	HotelResponseDTO getHotelById(Long id, LocalDate checkInDate, LocalDate checkOutDate);


	void deleteHotel(Long id);

	Page<HotelResponseDTO> getFilteredHotels(
		    String location, Integer ratings, Double minPrice, Double maxPrice, int page, int size
		);

		Page<HotelResponseDTO> findByLocation(
		    String location, int page, int size
		);

		Page<HotelResponseDTO> getFilteredHotelsWithAvailability(
		    String city, Integer ratings, Double minPrice, Double maxPrice, 
		    LocalDate checkInDate, LocalDate checkOutDate, int page, int size
		);

}
