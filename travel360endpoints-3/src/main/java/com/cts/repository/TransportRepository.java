package com.cts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cts.entity.Partner;
import com.cts.entity.Transport;
import com.cts.enums.TransportStatus;

import jakarta.persistence.LockModeType;

public interface TransportRepository extends JpaRepository<Transport, Long> {

    List<Transport> findByPartner(Partner partner);

    // Pessimistic write-lock for the booking flow: serializes concurrent bookings of the
    // same transport so seat availability can't be oversold (SELECT ... FOR UPDATE).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from Transport t where t.transportId = :id")
    Optional<Transport> findByIdForUpdate(@Param("id") Long id);

    Page<Transport> findBySourceAndDestination(String source, String destination, Pageable pageable);

    // ADDED: JPQL query to filter transports by route and seat price range
    @Query("SELECT DISTINCT t FROM Transport t JOIN t.seats s WHERE t.source = :source " +
           "AND t.destination = :destination AND s.price BETWEEN :min AND :max")
    Page<Transport> findByRouteAndSeatPriceBetween(
            @Param("source") String source, 
            @Param("destination") String destination, 
            @Param("min") Double min, 
            @Param("max") Double max, 
            Pageable pageable);

    Page<Transport> findByTransportStatus(TransportStatus status, Pageable pageable);
    
    Page<Transport> findAll(Pageable pageable);
}