package com.cts.dto;

import com.cts.enums.HotelRoomType;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HotelRoomDTO {

    @NotNull(message = "Room type is required")
    private HotelRoomType roomType;

    @NotNull(message = "Price cannot be empty")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price per night must be greater than 0")
    private Double price;

    @NotNull(message = "Total rooms cannot be empty")
    @Min(value = 1, message = "Total rooms must be at least 1")
    private Integer totalRooms;
    
    private Integer availableRooms;
}
