package com.cts.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingCancelDTO {
	// Optional: If null, will be auto-injected from JWT in service layer
	private long userId;
	@NotNull(message="BookingId cannot be empty")
	private long bookingId;
	

}
