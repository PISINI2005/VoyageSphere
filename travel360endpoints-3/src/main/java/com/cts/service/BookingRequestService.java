package com.cts.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.cts.dto.BookingRequestCreateDTO;
import com.cts.dto.BookingRequestFeedbackDTO;
import com.cts.dto.BookingRequestRejectDTO;
import com.cts.dto.BookingRequestResponseDTO;
import com.cts.dto.BookingRequestSubmitDTO;
import com.cts.entity.User;
import com.cts.enums.BookingRequestStatus;

public interface BookingRequestService {
    BookingRequestResponseDTO createRequest(BookingRequestCreateDTO dto, User customer);
    Page<BookingRequestResponseDTO> getRequests(BookingRequestStatus status, Pageable pageable);
    Page<BookingRequestResponseDTO> getPendingRequests(Pageable pageable);
    BookingRequestResponseDTO getRequestById(Long id);
    BookingRequestResponseDTO claimRequest(Long id, User agent);
    BookingRequestResponseDTO acceptRequest(Long id, User agent);
    BookingRequestResponseDTO submitFulfillment(Long id, BookingRequestSubmitDTO dto, User agent);
    BookingRequestResponseDTO provideFeedback(Long id, BookingRequestFeedbackDTO dto, User customer);
    Page<BookingRequestResponseDTO> getMyRequests(Long userId, Pageable pageable);
    Page<BookingRequestResponseDTO> getAgentRequests(Long agentId, Pageable pageable);
    BookingRequestResponseDTO rejectRequest(Long id, BookingRequestRejectDTO dto, User agent);

}
