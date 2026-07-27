package com.cts.serviceimpl;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cts.entity.Booking;
import com.cts.entity.Passenger;
import com.cts.entity.PassengerProfile;
import com.cts.enums.PassengerStatus;
import com.cts.exception.InvalidBookingException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.PassengerProfileRepository;

import lombok.RequiredArgsConstructor;

/**
 * Turns the passenger profile IDs of a booking request into persisted {@link Passenger}
 * entities. Each ID must reference an existing {@link PassengerProfile} owned by the
 * booking user — travellers are created up-front via the passenger-profile endpoints,
 * never inline during booking. This is booking business logic (it reads the profile
 * store) so it lives in the service layer rather than in a mapper.
 *
 * <p>In the microservice split the owning user is identified by a scalar {@code userId}
 * (resolved from the JWT) rather than a {@code User} entity.
 */
@Component
@RequiredArgsConstructor
public class PassengerResolver {

    private final PassengerProfileRepository profileRepo;

    public List<Passenger> resolve(List<Long> profileIds, Booking booking, Long userId) {
        List<Long> duplicates = profileIds.stream()
                .filter(id -> Collections.frequency(profileIds, id) > 1)
                .distinct()
                .toList();
        if (!duplicates.isEmpty()) {
            throw new InvalidBookingException(
                    "Duplicate passenger profile IDs are not allowed in a single booking: " + duplicates);
        }

        return profileIds.stream()
                .map(id -> resolveOne(id, booking, userId))
                .collect(Collectors.toList());
    }

    private Passenger resolveOne(Long profileId, Booking booking, Long userId) {
        PassengerProfile profile = profileRepo
                .findByPassengerProfileIdAndUserId(profileId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Passenger profile " + profileId + " not found"));

        return Passenger.builder()
                .profile(profile)
                .booking(booking)
                .status(PassengerStatus.ACTIVE)
                .build();
    }
}
