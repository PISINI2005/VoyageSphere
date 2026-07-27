package com.cts.repository;

import java.util.List;
import java.util.Optional;

import com.cts.entity.Flight;
import com.cts.entity.Partner;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FlightRepository extends JpaRepository<Flight, Long> {

    List<Flight> findByPartner(Partner partner);

    // Pessimistic write-lock for the booking flow: serializes concurrent bookings of the
    // same flight so seat availability can't be oversold (SELECT ... FOR UPDATE).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Flight f where f.flightId = :id")
    Optional<Flight> findByIdForUpdate(@Param("id") Long id);

    Page<Flight> findBySourceAndDestination(String source, String destination, Pageable pageable);

    // Flights on the route that offer at least one seat class priced within [min, max].
    @Query("SELECT DISTINCT f FROM Flight f JOIN f.seats s " +
           "WHERE f.source = :source AND f.destination = :destination " +
           "AND s.price BETWEEN :min AND :max")
    Page<Flight> findByRouteAndSeatPriceBetween(
            @Param("source") String source,
            @Param("destination") String destination,
            @Param("min") Double min,
            @Param("max") Double max,
            Pageable pageable
    );
}