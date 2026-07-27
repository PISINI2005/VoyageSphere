package com.cts.entity;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PackageItinerary {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long packageItineraryId;
	private String notes;
	private LocalDateTime createdAt;
	private String detailedDescription;
	private String keyHighlights;
	private String guideName;
	private String supportContact;

	// JSON array stored as TEXT: [{day:1, title:"...", activities:"..."}, ...]
	@Lob
	@Column(columnDefinition = "TEXT")
	private String dayWisePlan;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "package_id" ,nullable = false)
    @JsonManagedReference
    private TravelPackage travelPackage;


}
