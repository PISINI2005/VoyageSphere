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
import com.cts.entity.TravelPackage;
import com.cts.enums.TravelPackageCategory;

import jakarta.persistence.LockModeType;

public interface TravelPackageRepository extends JpaRepository<TravelPackage, Long> {

    Page<TravelPackage> findByCategory(TravelPackageCategory category, Pageable pageable);

    List<TravelPackage> findByPartner(Partner partner);

    List<TravelPackage> findBySourceAndDestination(String source, String destination);

    @Query("select p from TravelPackage p where (:min is null or p.price >= :min) and (:max is null or p.price <= :max)")
    Page<TravelPackage> findAllWithPriceFilter(@Param("min") Double min, @Param("max") Double max, Pageable pageable);

    @Query("select p from TravelPackage p where p.category = :category and (:min is null or p.price >= :min) and (:max is null or p.price <= :max)")
    Page<TravelPackage> findByCategoryWithPriceFilter(@Param("category") TravelPackageCategory category, @Param("min") Double min, @Param("max") Double max, Pageable pageable);

    @Query("select p from TravelPackage p where p.source = :source and p.destination = :destination and (:min is null or p.price >= :min) and (:max is null or p.price <= :max)")
    List<TravelPackage> findBySourceAndDestinationWithPriceFilter(@Param("source") String source, @Param("destination") String destination, @Param("min") Double min, @Param("max") Double max);

    @Query("select p from TravelPackage p where p.source = :source and p.destination = :destination and p.category = :category and (:min is null or p.price >= :min) and (:max is null or p.price <= :max)")
    List<TravelPackage> findBySourceAndDestinationAndCategoryWithPriceFilter(@Param("source") String source, @Param("destination") String destination, @Param("category") TravelPackageCategory category, @Param("min") Double min, @Param("max") Double max);

    // Pessimistic write-lock for the booking flow: serializes concurrent bookings of the
    // same package so slot availability can't be oversold (SELECT ... FOR UPDATE).
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from TravelPackage p where p.packageId = :id")
    Optional<TravelPackage> findByIdForUpdate(@Param("id") Long id);
}