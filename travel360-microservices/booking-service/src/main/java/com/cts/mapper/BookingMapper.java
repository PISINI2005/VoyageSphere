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
import com.cts.dto.FlightResponseDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.dto.TransportResponseDTO;
import com.cts.dto.TravelPackageResponseDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.Booking;

import lombok.RequiredArgsConstructor;

/**
 * Maps {@link Booking} entities to the various booking response DTOs. Stateless.
 * Delegates passenger mapping to {@link PassengerMapper}.
 *
 * <p>In the microservice split the cross-domain entities (User/Flight/Hotel/
 * Transport/TravelPackage) are no longer JPA associations on {@link Booking};
 * the resolved remote details are passed in as Feign response DTOs, and the
 * owning user id/email come from the {@link UserResponseDTO} fetched via Feign.
 */
@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final PassengerMapper passengerMapper;

    public BookingFlightResponseDTO toFlightResponse(Booking booking, UserResponseDTO user, FlightResponseDTO flight, BookingFlightDTO dto) {
        return BookingFlightResponseDTO.builder().bookingId(booking.getBookingId())
                .bookingType(booking.getBookingType()).amount(booking.getAmount()).status(booking.getStatus())
                .userId(booking.getUserId()).email(user.getEmail()).units(dto.getUnits())
                .seatType(booking.getSeatType())
                .createdAt(booking.getCreatedAt())
                .bookingDate(booking.getBookingDate()).arrivalTime(flight.getArrivalTime())
                .departureTime(flight.getDepartureTime()).travelDate(dto.getTravelDate())
                .bookingName(booking.getBookingName()).gender(booking.getGender()).flightId(flight.getFlightId())
                .flightNumber(flight.getFlightNumber()).source(flight.getSource()).destination(flight.getDestination())
                .passengers(passengerMapper.toResponses(booking.getPassengers())).build();
    }

    public BookingHotelResponseDTO toHotelResponse(Booking booking, UserResponseDTO user, HotelResponseDTO hotel, BookingHotelDTO dto) {
        return BookingHotelResponseDTO.builder().bookingId(booking.getBookingId()).bookingType(booking.getBookingType())
                .amount(booking.getAmount()).status(booking.getStatus()).userId(booking.getUserId())
                .email(user.getEmail())
                .units(dto.getUnits()).roomType(booking.getRoomType()).days(booking.getDays())
                .checkInDate(booking.getCheckInDate())
                .checkOutDate(booking.getCheckOutDate()).bookingName(booking.getBookingName())
                .gender(booking.getGender()).hotelId(hotel.getHotelId()).hotelName(hotel.getHotelName())
                .city(hotel.getCity()).build();
    }

    public BookingPackageResponseDTO toPackageResponse(Booking booking, UserResponseDTO user, TravelPackageResponseDTO tpackage, BookingPackageDTO dto) {
        return BookingPackageResponseDTO.builder().bookingId(booking.getBookingId())
                .bookingType(booking.getBookingType()).amount(booking.getAmount()).status(booking.getStatus())
                .bookingDate(booking.getBookingDate()).userId(booking.getUserId())
                .email(user.getEmail()).units(dto.getUnits())
                .bookingName(booking.getBookingName()).gender(booking.getGender()).packageId(tpackage.getPackageId())
                .packageName(tpackage.getPackageName()).source(tpackage.getSource())
                .destination(tpackage.getDestination()).durationDays(tpackage.getDurationDays())
                .category(tpackage.getCategory())
                .packageStatus(tpackage.getStatus()).build();
    }

    public BookingTransportResponseDTO toTransportResponse(Booking booking, UserResponseDTO user, TransportResponseDTO transport, BookingTransportDTO dto) {
        return BookingTransportResponseDTO.builder().bookingId(booking.getBookingId())
                .bookingType(booking.getBookingType()).amount(booking.getAmount()).status(booking.getStatus())
                .bookingDate(booking.getBookingDate()).travelDate(dto.getTravelDate())
                .userId(booking.getUserId()).email(user.getEmail())
                .units(dto.getUnits()).transportClass(booking.getTransportClass())
                .bookingName(booking.getBookingName()).gender(booking.getGender())
                .transportId(transport.getTransportId()).source(transport.getSource())
                .destination(transport.getDestination()).transportType(transport.getTransportType())
                .departureTime(transport.getDepartureTime()).arrivalTime(transport.getArrivalTime())
                .passengers(passengerMapper.toResponses(booking.getPassengers())).build();
    }

    /**
     * Full booking summary, including passengers when present.
     *
     * <p>The booking now carries only scalar cross-service ids, so the
     * flight number / hotel name / transport type / package name (which used to
     * be read off the associated entities) are not available locally and are
     * omitted; the corresponding ids are still emitted.
     */
    public BookingResponseDTO toResponse(Booking booking) {
        return BookingResponseDTO.builder().bookingId(booking.getBookingId()).bookingType(booking.getBookingType())
                .amount(booking.getAmount()).status(booking.getStatus()).userId(booking.getUserId())
                .units(booking.getUnits())
                .flightId(booking.getFlightId())
                .hotelId(booking.getHotelId())
                .transportId(booking.getTransportId())
                .packageId(booking.getPackageId())
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
                .amount(booking.getAmount()).status(booking.getStatus()).userId(booking.getUserId())
                .units(booking.getUnits())
                .flightId(booking.getFlightId())
                .hotelId(booking.getHotelId())
                .transportId(booking.getTransportId())
                .packageId(booking.getPackageId())
                .itineraryId(booking.getItinerary() != null ? booking.getItinerary().getItineraryId() : null).build();
    }
}
