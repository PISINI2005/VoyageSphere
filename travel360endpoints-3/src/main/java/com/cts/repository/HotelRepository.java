package com.cts.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cts.entity.Hotel;
import com.cts.entity.Partner;

import jakarta.persistence.LockModeType;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

	List<Hotel> findByPartner(Partner partner);

	// Pessimistic write-lock for the booking flow: serializes concurrent bookings of the
	// same hotel so room availability can't be oversold (SELECT ... FOR UPDATE).
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select h from Hotel h where h.hotelId = :id")
	Optional<Hotel> findByIdForUpdate(@Param("id") Long id);

	Page<Hotel> findByCity(String city,Pageable pageable);

	// Filters on per-room price: matches hotels offering at least one room type in [minPrice, maxPrice].
	@Query("""
		    SELECT DISTINCT h FROM Hotel h LEFT JOIN h.rooms r
		    WHERE (:location IS NULL OR h.city = :location)
		    AND (:ratings IS NULL OR h.ratings = :ratings)
		    AND (:minPrice IS NULL OR r.price >= :minPrice)
		    AND (:maxPrice IS NULL OR r.price <= :maxPrice)
		""")
		Page<Hotel> filterHotels(String location, Integer ratings, Double minPrice, Double maxPrice,Pageable pageable);

	

}
