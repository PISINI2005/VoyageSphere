package com.cts.service;

import java.util.List;

import com.cts.dto.HotelDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.entity.Hotel;
import com.cts.enums.HotelStatus;

public interface HotelService {



	    Hotel addHotel(HotelDTO dto);

	    Hotel updateHotel(Long id, HotelDTO dto);

	    Hotel updateHotelStatus(Long id, HotelStatus status);

	    List<HotelResponseDTO> findByLocation(String location, int page, int size);



	    List<HotelResponseDTO> getFilteredHotels(String location, Integer ratings, Double minPrice, Double maxPrice, int page, int size);

	    void deleteHotel(Long id);

		Hotel getHotelById(Long id);

}
