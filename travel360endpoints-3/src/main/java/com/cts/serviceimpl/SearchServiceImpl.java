package com.cts.serviceimpl;

import org.springframework.stereotype.Service;
import java.time.LocalDate;

import com.cts.enums.TravelPackageCategory;
import com.cts.service.FlightService;
import com.cts.service.HotelService;
import com.cts.service.SearchService;
import com.cts.service.TransportService;
import com.cts.service.TravelPackageService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {

	private final FlightService flightService;
	private final HotelService hotelService;
	private final TravelPackageService packageService;
	private final TransportService transportService;

	@Override
	public Object search(String type, String source, String destination, String city, Double min, Double max,
			Integer ratings, TravelPackageCategory category, LocalDate date, LocalDate checkInDate,
			LocalDate checkOutDate, int page, int size) {

		log.info("Performing search of type '{}' on date '{}' (page={}, size={})", type, date, page, size);

		switch (type.toLowerCase()) {

		case "flight":
			log.debug("Routing search to flight service with price criteria");
			validateFlight(source, destination, date);
			// Returns Page<FlightResponseDTO>
			return flightService.searchFlightsWithAvailability(source, destination, min, max, date, page, size);

		case "hotel":
			log.debug("Routing search to hotel service");
			validateHotel(city, checkInDate, checkOutDate);
			// Returns Page<HotelResponseDTO>
			return hotelService.getFilteredHotelsWithAvailability(city, ratings, min, max, checkInDate, checkOutDate,
					page, size);

		case "package":
			log.debug("Routing search to travel package service");

			if (source != null && destination != null) {
				log.debug("Performing route-based search for packages");
				return packageService.getPackagesByRoute(source, destination, date, category, min, max);
			}

			// NOTE: All down-stream packageService calls should return Page<TravelPackageResponseDTO>
			if (category != null && date != null) {
				return packageService.searchByCategoryWithAvailability(category, date, page, size, min, max);
			}

			if (category != null) {
				return packageService.searchByCategory(category, page, size, min, max);
			}

			if (date != null) {
				return packageService.getAllPackagesWithAvailability(date, page, size, min, max);
			}

			return packageService.getAllPackages(page, size, min, max);

		case "transport":
			log.debug("Routing search to transport service with price criteria");

			validateTransport(source, destination, date);

			// UPDATED: Now passing min and max to the updated transport service call
			return transportService.findByRouteWithAvailability(source, destination, min, max, date, page, size);

		default:
			log.error("Invalid search type requested: {}", type);
			throw new IllegalArgumentException("Invalid search type");
		}
	}

	private void validateFlight(String source, String destination, LocalDate date) {
		if (source == null || destination == null || date == null) {
			log.error("Flight search validation failed: source={}, destination={}, date={}", source, destination, date);
			throw new IllegalArgumentException("Source, Destination, and Travel Date are required");
		}
	}

	private void validateHotel(String city, LocalDate checkInDate, LocalDate checkOutDate) {
		if (city == null) {
			throw new IllegalArgumentException("City is required");
		}
		if (checkInDate == null || checkOutDate == null) {
			throw new IllegalArgumentException("Check-in and Check-out dates are required");
		}
		if (!checkOutDate.isAfter(checkInDate)) {
			throw new IllegalArgumentException("Check-out date must be after check-in date");
		}
	}

	private void validateTransport(String source, String destination, LocalDate date) {
		if (source == null || destination == null || date == null) {
			throw new IllegalArgumentException("Source, Destination and Travel Date are required");
		}
	}

	public void validatePackage(TravelPackageCategory category, LocalDate date) {
		if (category == null || date == null) {
			throw new IllegalArgumentException("Category and Date are Required for Travel Package Search");
		}
	}
}
