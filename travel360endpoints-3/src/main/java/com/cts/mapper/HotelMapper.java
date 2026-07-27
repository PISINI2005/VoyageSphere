package com.cts.mapper;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.cts.dto.HotelDTO;
import com.cts.dto.HotelResponseDTO;
import com.cts.dto.HotelRoomDTO;
import com.cts.entity.Hotel;
import com.cts.entity.HotelRoom;
import com.cts.entity.Partner;
import com.cts.enums.HotelRoomType;

/**
 * Maps between {@link Hotel} entities (and their room types) and DTOs.
 * Stateless and side-effect free except for {@link #applyRooms}, which
 * mutates the managed entity's room collection by design.
 */
@Component
public class HotelMapper {

    public Hotel toEntity(HotelDTO dto, Partner partner) {
        Hotel hotel = Hotel.builder()
                .hotelName(dto.getHotelName())
                .city(dto.getCity())
                .address(dto.getAddress())
                .ratings(dto.getRatings())
                .contactNo(dto.getContactNo())
                .emailId(dto.getEmailId())
                .status(dto.getStatus())
                .partner(partner)
                .build();

        applyRooms(hotel, dto.getRooms());

        return hotel;
    }

    public void updateEntity(Hotel hotel, HotelDTO dto, Partner partner) {
        hotel.setHotelName(dto.getHotelName());
        hotel.setCity(dto.getCity());
        hotel.setAddress(dto.getAddress());
        hotel.setRatings(dto.getRatings());
        hotel.setContactNo(dto.getContactNo());
        hotel.setEmailId(dto.getEmailId());
        hotel.setStatus(dto.getStatus());
        hotel.setPartner(partner);

        applyRooms(hotel, dto.getRooms());
    }

    public HotelResponseDTO toResponse(Hotel hotel) {
        return HotelResponseDTO.builder()
                .hotelId(hotel.getHotelId())
                .hotelName(hotel.getHotelName())
                .ratings(hotel.getRatings())
                .city(hotel.getCity())
                .address(hotel.getAddress())
                .contactNo(hotel.getContactNo())
                .emailId(hotel.getEmailId())
                .status(hotel.getStatus())
                .rooms(mapRooms(hotel))
                .build();
    }

    // Replaces the hotel's room types with the ones from the request.
    // orphanRemoval on Hotel.rooms deletes any room types no longer present.
    public void applyRooms(Hotel hotel, List<HotelRoomDTO> roomDtos) {

        if (roomDtos == null || roomDtos.isEmpty()) {
            return;
        }

        Map<HotelRoomType, HotelRoom> existingRooms =
                hotel.getRooms().stream()
                        .collect(Collectors.toMap(
                                HotelRoom::getRoomType,
                                Function.identity()
                        ));

        for (HotelRoomDTO dto : roomDtos) {

            HotelRoom room = existingRooms.get(dto.getRoomType());

            if (room != null) {
                // Existing room type -> update
                room.setPrice(dto.getPrice());
                room.setTotalRooms(dto.getTotalRooms());
            } else {
                // New room type -> add
                hotel.getRooms().add(
                        HotelRoom.builder()
                                .roomType(dto.getRoomType())
                                .price(dto.getPrice())
                                .totalRooms(dto.getTotalRooms())
                                .hotel(hotel)
                                .build()
                );
            }
        }
    }

    private List<HotelRoomDTO> mapRooms(Hotel hotel) {

        if (hotel.getRooms() == null) {
            return List.of();
        }

        return hotel.getRooms().stream()
                .map(r -> HotelRoomDTO.builder()
                        .roomType(r.getRoomType())
                        .price(r.getPrice())
                        .totalRooms(r.getTotalRooms())
                        .build())
                .toList();
    }
}
