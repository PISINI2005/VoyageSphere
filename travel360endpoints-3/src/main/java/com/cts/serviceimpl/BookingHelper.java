package com.cts.serviceimpl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.stereotype.Component;

import com.cts.exception.InvalidBookingException;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class BookingHelper {

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

    public double calculateUrgencyPrice(double basePrice, LocalDate travelDate) {
    if (travelDate == null) return Math.round(basePrice);

    long daysUntilTravel = ChronoUnit.DAYS.between(LocalDate.now(), travelDate);

    double price;

    if (daysUntilTravel <= 3) {
        price = basePrice * 1.5;
    } else if (daysUntilTravel <= 7) {
        price = basePrice * 1.3;
    } else if (daysUntilTravel <= 15) {
        price = basePrice * 1.1;
    } else {
        price = basePrice;
    }

    return Math.round(price);
}
}
