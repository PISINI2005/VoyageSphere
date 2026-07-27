package com.cts.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.cts.entity.BookingRequest;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    Page<BookingRequest> findByStatus(com.cts.enums.BookingRequestStatus status, Pageable pageable);
    List<BookingRequest> findByStatus(com.cts.enums.BookingRequestStatus status);
    List<BookingRequest> findByCustomerUserId(Long userId);
    Page<BookingRequest> findByAgentUserId(Long agentId, Pageable pageable);
}
