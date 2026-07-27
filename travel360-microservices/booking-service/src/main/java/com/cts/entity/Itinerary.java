package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "itinerary")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Itinerary {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long itineraryId;

	private String tripName;

	private String description;

	private LocalDate startDate;

	private LocalDate endDate;

	private LocalDateTime createdAt;

	// User now lives in the user-service / user database; stored here as a scalar id.
	@Column(name = "user_id")
	private Long userId;

	@OneToMany(mappedBy = "itinerary")
	private List<Booking> bookings;

}
