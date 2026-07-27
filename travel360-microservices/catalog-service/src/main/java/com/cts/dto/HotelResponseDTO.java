package com.cts.dto;

import java.util.List;

import com.cts.enums.HotelStatus;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HotelResponseDTO {

    private Long hotelId;
    private String hotelName;
    private int ratings;
    private String city;
    private String address;
    private String contactNo;
    private String emailId;
    private HotelStatus status;

    private List<HotelRoomDTO> rooms;
}
