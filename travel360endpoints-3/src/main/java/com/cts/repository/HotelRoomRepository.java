package com.cts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.entity.HotelRoom;
import com.cts.enums.HotelRoomType;

@Repository
public interface HotelRoomRepository extends JpaRepository<HotelRoom, Long> {

    Optional<HotelRoom> findByHotelHotelIdAndRoomType(Long hotelId, HotelRoomType roomType);
}
