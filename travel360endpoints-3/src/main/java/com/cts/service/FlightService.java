package com.cts.service;

import com.cts.dto.FlightDTO;
import com.cts.dto.FlightResponseDTO;
import com.cts.dto.PriceDateDTO;
import com.cts.entity.Flight;
import com.cts.enums.FlightStatus;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlightService {

	Flight addFlight(FlightDTO dto);

	Flight updateFlight(Long id, FlightDTO dto);

	Flight updateFlightStatus(Long id, FlightStatus status);

	List<FlightResponseDTO> searchFlights(String source, String destination, int page, int size);

	Page<FlightResponseDTO> getAllFlights(Pageable pageable);

	List<FlightResponseDTO> filterFlights(String source, String destination, Double min, Double max, int page,
			int size);
	// with availability just checking for now
	Page<FlightResponseDTO> searchFlightsWithAvailability(
			String source,
			String destination,
			Double min,
			Double max,
			java.time.LocalDate date,
			int page,
			int size
			);
	// Inside FlightService.java
	FlightResponseDTO getFlightById(Long id, LocalDate date);

	List<PriceDateDTO> getPriceCalendar(Long id, String seatType, LocalDate startDate, LocalDate endDate);

	void deleteFlight(Long id);
}
