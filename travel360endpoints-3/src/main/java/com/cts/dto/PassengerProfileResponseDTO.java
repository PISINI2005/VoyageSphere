package com.cts.dto;

import com.cts.enums.Gender;
import com.cts.enums.IdentificationType;
import com.cts.enums.Nationality;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class PassengerProfileResponseDTO {

    private Long passengerProfileId;
    private String passengerName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String contactNo;
    private String emailAddress;
    private Nationality nationality;
    private IdentificationType identificationType;
    private String identificationNumber;
}
