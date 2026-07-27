package com.cts.entity;




import com.cts.enums.TransportStatus;




import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "transport")
@Entity
public class Transport {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long transportId;
	
	private int transportNumber;
	private String source;
	private String destination;
	private String transportType;

	private LocalTime departureTime;
	private LocalTime arrivalTime;

	@Enumerated(EnumType.STRING)
	private TransportStatus transportStatus;
	@ManyToOne
	@JoinColumn(name = "partner_id")
	private Partner partner;

	@OneToMany(mappedBy = "transport", cascade = CascadeType.ALL, orphanRemoval = true)
	@Builder.Default
	private List<TransportSeat> seats = new ArrayList<>();
	
		
	
	
}
