package com.cts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.entity.FlightSeat;
import com.cts.enums.SeatType;

@Repository
public interface FlightSeatRepository extends JpaRepository<FlightSeat, Long> {

    Optional<FlightSeat> findByFlightFlightIdAndSeatType(Long flightId, SeatType seatType);
}
