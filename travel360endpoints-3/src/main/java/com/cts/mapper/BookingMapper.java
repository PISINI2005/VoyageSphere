package com.cts.mapper;

import org.springframework.stereotype.Component;

import com.cts.dto.BookingFlightDTO;
import com.cts.dto.BookingFlightResponseDTO;
import com.cts.dto.BookingHotelDTO;
import com.cts.dto.BookingHotelResponseDTO;
import com.cts.dto.BookingPackageDTO;
import com.cts.dto.BookingPackageResponseDTO;
import com.cts.dto.BookingResponseDTO;
import com.cts.dto.BookingTransportDTO;
import com.cts.dto.BookingTransportResponseDTO;
import com.cts.entity.Booking;
import com.cts.entity.Flight;
import com.cts.entity.Hotel;
import com.cts.entity.Transport;
import com.cts.entity.TravelPackage;

import lombok.RequiredArgsConstructor;

/**
 * Maps {@link Booking} entities to the various booking response DTOs. Stateless.
 * Delegates passenger mapping to {@link PassengerMapper}.
 */
@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final PassengerMapper passengerMapper;

    public BookingFlightResponseDTO toFlightResponse(Booking booking, Flight flight, BookingFlightDTO dto) {
        return BookingFlightResponseDTO.builder().bookingId(booking.getBookingId())
                .bookingType(booking.getBookingType()).amount(booking.getAmount()).status(booking.getStatus())
                .userId(booking.getUser().getUserId()).email(booking.getUser().getEmail()).units(dto.getUnits())
                .seatType(booking.getSeatType())
                .createdAt(booking.getCreatedAt())
                .bookingDate(booking.getBookingDate()).arrivalTime(flight.getArrivalTime())
                .departureTime(flight.getDepartureTime()).travelDate(dto.getTravelDate())
                .bookingName(booking.getBookingName()).gender(booking.getGender()).flightId(flight.getFlightId())
                .flightNumber(flight.getFlightNumber()).source(flight.getSource()).destination(flight.getDestination())
                .passengers(passengerMapper.toResponses(booking.getPassengers())).build();
    }

    public BookingHotelResponseDTO toHotelResponse(Booking booking, Hotel hotel, BookingHotelDTO dto) {
        return BookingHotelResponseDTO.builder().bookingId(booking.getBookingId()).bookingType(booking.getBookingType())
                .amount(booking.getAmount()).status(booking.getStatus()).userId(booking.getUser().getUserId())
                .email(booking.getUser().getEmail())
                .units(dto.getUnits()).roomType(booking.getRoomType()).days(booking.getDays())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate()).bookingName(booking.getBookingName())
                .gender(booking.getGender()).hotelId(hotel.getHotelId()).hotelName(hotel.getHotelName())
                .city(hotel.getCity()).build();
    }

    public BookingPackageResponseDTO toPackageResponse(Booking booking, TravelPackage tpackage, BookingPackageDTO dto) {
        return BookingPackageResponseDTO.builder().bookingId(booking.getBookingId())
                .bookingType(booking.getBookingType()).amount(booking.getAmount()).status(booking.getStatus())
                .bookingDate(booking.getBookingDate()).userId(booking.getUser().getUserId())
                .email(booking.getUser().getEmail()).units(dto.getUnits())
                .bookingName(booking.getBookingName()).gender(booking.getGender()).packageId(tpackage.getPackageId())
                .packageName(tpackage.getPackageName()).source(tpackage.getSource())
                .destination(tpackage.getDestination()).durationDays(tpackage.getDurationDays())
                .category(tpackage.getCategory())
                .packageStatus(tpackage.getStatus()).build();
    }

    public BookingTransportResponseDTO toTransportResponse(Booking booking, Transport transport, BookingTransportDTO dto) {
        return BookingTransportResponseDTO.builder().bookingId(booking.getBookingId())
                .bookingType(booking.getBookingType()).amount(booking.getAmount()).status(booking.getStatus())
                .bookingDate(booking.getBookingDate()).travelDate(dto.getTravelDate())
                .userId(booking.getUser().getUserId()).email(booking.getUser().getEmail())
                .units(dto.getUnits()).transportClass(booking.getTransportClass())
                .bookingName(booking.getBookingName()).gender(booking.getGender())
                .transportId(transport.getTransportId()).source(transport.getSource())
                .destination(transport.getDestination()).transportType(transport.getTransportType())
                .departureTime(transport.getDepartureTime()).arrivalTime(transport.getArrivalTime())
                .passengers(passengerMapper.toResponses(booking.getPassengers())).build();
    }

    /**
     * Full booking summary, including passengers when present.
     */
    public BookingResponseDTO toResponse(Booking booking) {
        return BookingResponseDTO.builder().bookingId(booking.getBookingId()).bookingType(booking.getBookingType())
                .amount(booking.getAmount()).status(booking.getStatus()).userId(booking.getUser().getUserId())
                .bookingDate(booking.getBookingDate())
                .email(booking.getUser().getEmail()).units(booking.getUnits())
                .flightId(booking.getFlight() != null ? booking.getFlight().getFlightId() : null)
                .flightNumber(booking.getFlight() != null ? booking.getFlight().getFlightNumber() : null)
                .hotelId(booking.getHotel() != null ? booking.getHotel().getHotelId() : null)
                .hotelName(booking.getHotel() != null ? booking.getHotel().getHotelName() : null)
                .transportId(booking.getTransport() != null ? booking.getTransport().getTransportId() : null)
                .transportType(booking.getTransport() != null ? booking.getTransport().getTransportType() : null)
                .packageId(booking.getTravelPackage() != null ? booking.getTravelPackage().getPackageId() : null)
                .packageName(booking.getTravelPackage() != null ? booking.getTravelPackage().getPackageName() : null)
                .itineraryId(booking.getItinerary() != null ? booking.getItinerary().getItineraryId() : null)
                .passengers(booking.getPassengers() != null && !booking.getPassengers().isEmpty()
                        ? passengerMapper.toResponses(booking.getPassengers())
                        : null)
                .build();
    }

    /**
     * Booking summary as embedded inside an itinerary. Intentionally omits passengers,
     * preserving the original itinerary serialization behavior.
     */
    public BookingResponseDTO toItineraryResponse(Booking booking) {
        return BookingResponseDTO.builder().bookingId(booking.getBookingId()).bookingType(booking.getBookingType())
        		.bookingId(booking.getBookingId())
        		.bookingType(booking.getBookingType())
        		.bookingDate(booking.getBookingDate())
                .amount(booking.getAmount()).status(booking.getStatus()).userId(booking.getUser().getUserId())
                .email(booking.getUser().getEmail()).units(booking.getUnits())
                .flightId(booking.getFlight() != null ? booking.getFlight().getFlightId() : null)
                .flightNumber(booking.getFlight() != null ? booking.getFlight().getFlightNumber() : null)
                .hotelId(booking.getHotel() != null ? booking.getHotel().getHotelId() : null)
                .hotelName(booking.getHotel() != null ? booking.getHotel().getHotelName() : null)
                .transportId(booking.getTransport() != null ? booking.getTransport().getTransportId() : null)
                .transportType(booking.getTransport() != null ? booking.getTransport().getTransportType() : null)
                .packageId(booking.getTravelPackage() != null ? booking.getTravelPackage().getPackageId() : null)
                .packageName(booking.getTravelPackage() != null ? booking.getTravelPackage().getPackageName() : null)
                .itineraryId(booking.getItinerary() != null ? booking.getItinerary().getItineraryId() : null).build();
    }
}
