package com.cts.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.cts.enums.FlightStatus;

@Entity
@Table(name = "flight")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Flight {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long flightId;

	private String flightNumber;
	private String airlineName;

	private String source;
	private String destination;

	private LocalTime arrivalTime;
	private LocalTime departureTime;

	@Enumerated(EnumType.STRING)
	private FlightStatus status;

	@ManyToOne
	@JoinColumn(name = "partner_id")
	private Partner partner;

	@OneToMany(mappedBy = "flight", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<FlightSeat> seats = new ArrayList<>();
}
