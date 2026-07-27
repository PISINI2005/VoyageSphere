package com.cts.dto;

import com.cts.enums.HotelStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private HotelStatus status;
}
