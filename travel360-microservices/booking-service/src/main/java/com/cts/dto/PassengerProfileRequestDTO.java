package com.cts.dto;

import com.cts.enums.Gender;
import com.cts.enums.IdentificationType;
import com.cts.enums.Nationality;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PassengerProfileRequestDTO {

    @NotBlank(message = "Full name is required")
    private String passengerName;

    @NotNull(message = "Date of birth is required")
    @Past(message = "DOB must be in the past")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Contact Number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Invalid contact number")
    private String contactNo;

    @Email(message = "Invalid email")
    @NotBlank(message = "Email is required")
    private String emailAddress;

    @NotNull(message = "Nationality is required")
    private Nationality nationality;

    @NotNull(message = "Identification type is required")
    private IdentificationType identificationType;

    @NotBlank(message = "Identification number is required")
    private String identificationNumber;
}
