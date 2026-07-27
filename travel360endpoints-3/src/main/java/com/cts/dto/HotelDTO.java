package com.cts.dto;

import java.util.List;

import com.cts.enums.HotelStatus;
import lombok.Data;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Data
public class HotelDTO {

    @NotBlank(message = "Hotel name is required")
    private String hotelName;

    @NotNull(message = "Ratings cannot be empty")
    @Min(value = 1, message = "Ratings must be at least 1 star")
    @Max(value = 5, message = "Ratings cannot exceed 5 stars")
    private Integer ratings;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be exactly 10 digits")
    private String contactNo;

    @NotBlank(message = "Email ID is required")
    @Email(message = "Invalid email format")
    private String emailId;

    @NotNull(message = "Hotel status is required")
    private HotelStatus status;

    @NotNull(message = "Partner id is required")
    private Long partnerId;

    // Room types define this hotel's per-night pricing and inventory.
    // At least one room type (e.g. STANDARD) is required to make it bookable.
    @NotEmpty(message = "At least one room type is required")
    @Valid
    private List<HotelRoomDTO> rooms;
}
