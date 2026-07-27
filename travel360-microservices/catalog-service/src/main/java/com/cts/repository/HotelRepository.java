package com.cts.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.cts.entity.Hotel;
import com.cts.entity.Partner;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {

	List<Hotel> findByPartner(Partner partner);

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
