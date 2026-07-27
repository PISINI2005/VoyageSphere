package com.cts.dto;

import java.time.LocalDate;
import java.util.List;

import com.cts.enums.Gender;
import com.cts.enums.SeatType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BookingFlightDTO {

    // Optional: If null, will be auto-injected from JWT in service layer
    private Long userId;

    @NotNull(message = "Flight ID cannot be empty")
    private Long flightId;

    @NotNull(message = "Units cannot be empty")
    @Min(value = 1, message = "You must book at least 1 seat")
    @Max(value = 10, message = "You cannot book more than 10 seats at once")
    private Integer units; 

    @NotBlank(message = "Booking name is required")
    @Size(min = 2, max = 50, message = "Booking name must be between 2 and 50 characters")
    private String bookingName;

    @NotNull(message = "Travel date is required")
    @Future(message = "Travel date must be in the future")
    private LocalDate travelDate;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Seat type is required")
    private SeatType seatType;

    @NotEmpty(message = "At least one passenger is required")
    @Size(max = 10, message = "You cannot add more than 10 passengers at once")
    private List<@NotNull(message = "Passenger profile ID is required") Long> passengerProfileIds;
}