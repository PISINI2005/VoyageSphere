package com.cts.dto;

import com.cts.enums.BookingRequestCustomerStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingRequestFeedbackDTO {
    @NotNull(message = "Customer status is required")
    private BookingRequestCustomerStatus customerStatus;

    private String modificationDetails;

    // Custom validation for modificationDetails can be added in the service layer
}
