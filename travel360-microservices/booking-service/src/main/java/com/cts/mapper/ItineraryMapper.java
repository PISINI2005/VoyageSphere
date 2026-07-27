package com.cts.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cts.dto.BookingResponseDTO;
import com.cts.dto.ItineraryResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Itinerary;

import lombok.RequiredArgsConstructor;

/**
 * Maps {@link Itinerary} entities to their DTO representation. Stateless.
 * Delegates per-booking mapping to {@link BookingMapper}.
 */
@Component
@RequiredArgsConstructor
public class ItineraryMapper {

    private final BookingMapper bookingMapper;

    public ItineraryResponseDTO toResponse(Itinerary itinerary) {

        // Read straight from the object list, avoiding extra DB queries.
        List<Booking> bookings = itinerary.getBookings() != null ? itinerary.getBookings() : new ArrayList<>();

        List<BookingResponseDTO> bookingDTOs = bookings.stream().sorted((b1, b2) -> {
            if (b1.getBookingDate() == null)
                return 1;
            if (b2.getBookingDate() == null)
                return -1;
            return b1.getBookingDate().compareTo(b2.getBookingDate());
        }).map(bookingMapper::toItineraryResponse).toList();

        double totalTripAmount = bookings.stream().mapToDouble(Booking::getAmount).sum();

        return ItineraryResponseDTO.builder().itineraryId(itinerary.getItineraryId()).tripName(itinerary.getTripName())
                .description(itinerary.getDescription()).startDate(itinerary.getStartDate())
                .endDate(itinerary.getEndDate()).createdAt(itinerary.getCreatedAt())
                .userId(itinerary.getUserId()).bookings(bookingDTOs)
                .totalTripAmount(totalTripAmount).build();
    }
}
