package com.cts.repository;

import com.cts.entity.PassengerProfile;
import com.cts.enums.PassengerProfileStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassengerProfileRepository extends JpaRepository<PassengerProfile, Long> {

    List<PassengerProfile> findByUserIdAndStatus(Long userId, PassengerProfileStatus status);

    Optional<PassengerProfile> findByPassengerProfileIdAndUserId(Long passengerProfileId, Long userId);
}
