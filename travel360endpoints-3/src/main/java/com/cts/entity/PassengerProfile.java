package com.cts.entity;

import com.cts.enums.Gender;
import com.cts.enums.IdentificationType;
import com.cts.enums.Nationality;
import com.cts.enums.PassengerProfileStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "passenger_profile")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PassengerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long passengerProfileId;

    private String passengerName;
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String contactNo;
    private String emailAddress;

    @Enumerated(EnumType.STRING)
    private Nationality nationality;

    @Enumerated(EnumType.STRING)
    private IdentificationType identificationType;

    private String identificationNumber;

    @Enumerated(EnumType.STRING)
    private PassengerProfileStatus status;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
