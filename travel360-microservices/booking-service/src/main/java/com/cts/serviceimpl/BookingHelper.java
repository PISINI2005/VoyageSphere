package com.cts.serviceimpl;

import com.cts.client.CatalogClient;
import com.cts.client.UserClient;
import com.cts.config.AuthenticatedUser;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.FlightResponseDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.dto.TransportResponseDTO;
import com.cts.dto.TravelPackageResponseDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.exception.FlightNotFoundException;
import com.cts.exception.HotelNotFoundException;
import com.cts.exception.InvalidBookingException;
import com.cts.exception.PackageNotFoundException;
import com.cts.exception.TransportNotFoundException;
import com.cts.exception.UserNotFoundException;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Shared booking helpers: validation, refund math, the cross-service Feign
 * resolvers, and current-user resolution. Mirrors the monolith's BookingHelper,
 * extended with the Feign fetchers that replace the monolith's direct repo
 * look-ups now that User/Flight/Hotel/Transport/Package live in other services.
 */
@Component
@AllArgsConstructor
@Slf4j
public class BookingHelper {

    private final UserClient userClient;
    private final CatalogClient catalogClient;
    private final AuthenticatedUserProvider authUser;

    public void validatePassengerCount(List<Long> passengerProfileIds, int units) {
        int count = passengerProfileIds == null ? 0 : passengerProfileIds.size();
        if (count != units) {
            log.error("Passenger count mismatch: provided={} expected={}", count, units);
            throw new InvalidBookingException(
                    "Passenger count (" + count + ") must match the number of units (" + units + ")");
        }
    }

    public double calculateRefundAmount(double amount, LocalDate bookingDate) {
        if (bookingDate == null) {
            log.error("Cannot calculate refund: bookingDate is null");
            throw new InvalidBookingException("Invalid booking date");
        }

        long daysBetween = ChronoUnit.DAYS.between(LocalDate.now(), bookingDate);

        if (daysBetween <= 1) {
            // Today or less than a day away: cancellation is still allowed, but no refund.
            return 0.0;
        }

        if (daysBetween > 7) {
            return amount;
        } else if (daysBetween >= 4) {
            return amount * 0.80;
        } else {
            return amount * 0.60;
        }
    }

    /**
     * Classifies a refund relative to the full refundable amount.
     * Uses {@code >=} rather than exact equality to stay robust against floating-point drift.
     */
    public String refundStatus(double refund, double full) {
        if (refund <= 0) {
            return "NONE";
        }
        if (refund >= full) {
            return "FULL";
        }
        return "PARTIAL";
    }

    // ---------------------------------------------------------------------
    // Remote-resolution helpers: cross-domain entities now live in OTHER
    // services and are fetched via Feign. A missing record (null body or a
    // Feign error such as 404) is translated to the same domain exception the
    // monolith threw on an empty Optional.
    // ---------------------------------------------------------------------

    public UserResponseDTO fetchUser(Long userId) {
        try {
            UserResponseDTO user = userClient.getUser(userId);
            if (user == null) {
                log.error("User not found with id {}", userId);
                throw new UserNotFoundException("User not found");
            }
            return user;
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("User not found with id {}", userId);
            throw new UserNotFoundException("User not found");
        }
    }

    public FlightResponseDTO fetchFlight(Long flightId) {
        try {
            FlightResponseDTO flight = catalogClient.getFlight(flightId);
            if (flight == null) {
                log.error("Flight not found with id {}", flightId);
                throw new FlightNotFoundException("Flight not found");
            }
            return flight;
        } catch (FlightNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Flight not found with id {}", flightId);
            throw new FlightNotFoundException("Flight not found");
        }
    }

    public HotelResponseDTO fetchHotel(Long hotelId) {
        try {
            HotelResponseDTO hotel = catalogClient.getHotel(hotelId);
            if (hotel == null) {
                log.error("Hotel not found with id {}", hotelId);
                throw new HotelNotFoundException("Hotel not found");
            }
            return hotel;
        } catch (HotelNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Hotel not found with id {}", hotelId);
            throw new HotelNotFoundException("Hotel not found");
        }
    }

    public TransportResponseDTO fetchTransport(Long transportId) {
        try {
            TransportResponseDTO transport = catalogClient.getTransport(transportId);
            if (transport == null) {
                log.error("Transport not found with id {}", transportId);
                throw new TransportNotFoundException("Transport not found");
            }
            return transport;
        } catch (TransportNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Transport not found with id {}", transportId);
            throw new TransportNotFoundException("Transport not found");
        }
    }

    public TravelPackageResponseDTO fetchPackage(Long packageId) {
        try {
            TravelPackageResponseDTO tpackage = catalogClient.getPackage(packageId);
            if (tpackage == null) {
                log.error("Package not found with id {}", packageId);
                throw new PackageNotFoundException("Package not found");
            }
            return tpackage;
        } catch (PackageNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Package not found with id {}", packageId);
            throw new PackageNotFoundException("Package not found");
        }
    }

    /**
     * Resolves the current authenticated user's id for audit logging, or
     * {@code null} when no principal is present.
     */
    public Long currentUserId() {
        AuthenticatedUser caller = authUser.currentOrNull();
        return caller != null ? caller.getUserId() : null;
    }
}
