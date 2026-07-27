package com.cts.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateItineraryDTO {

	// Optional: If null, will be auto-injected from JWT in service layer
	private Long userId;

	@NotBlank(message = "Trip name is required")
	@Size(min = 2, max = 50, message = "Trip name must be between 2 and 50 characters")
	private String tripName;

	private String description;

	@NotNull(message = "Start date is required")
	private LocalDate startDate;

	@NotNull(message = "End date is required")
	private LocalDate endDate;
}
