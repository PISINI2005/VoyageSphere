package com.cts.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.entity.TransportSeat;
import com.cts.enums.TransportClass;

@Repository
public interface TransportSeatRepository extends JpaRepository<TransportSeat, Long> {

    Optional<TransportSeat> findByTransportTransportIdAndTransportClass(Long transportId, TransportClass transportClass);
}
