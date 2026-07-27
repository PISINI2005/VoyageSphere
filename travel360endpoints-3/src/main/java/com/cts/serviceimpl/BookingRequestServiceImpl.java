package com.cts.serviceimpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.dto.BookingRequestCreateDTO;
import com.cts.dto.BookingRequestFeedbackDTO;
import com.cts.dto.BookingRequestRejectDTO;
import com.cts.dto.BookingRequestResponseDTO;
import com.cts.dto.BookingRequestSubmitDTO;
import com.cts.entity.BookingRequest;
import com.cts.entity.User;
import com.cts.enums.AuditEntity;
import com.cts.enums.BookingRequestCustomerStatus;
import com.cts.enums.BookingRequestStatus;
import com.cts.enums.LogType;
import com.cts.enums.NotificationCategory;
import com.cts.mapper.BookingRequestMapper;
import com.cts.repository.BookingRequestRepository;
import com.cts.repository.UserRepository;
import com.cts.service.AuditLogService;
import com.cts.service.BookingRequestService;
import com.cts.service.NotificationService;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class BookingRequestServiceImpl implements BookingRequestService {

    private final BookingRequestRepository repository;
    private final BookingRequestMapper mapper;
    private final NotificationService notificationService;
    private final AuditLogService auditLogService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingRequestResponseDTO createRequest(BookingRequestCreateDTO dto, User customer) {
        log.info("Creating booking request for customer: {}", customer.getUserId());

        BookingRequest request = BookingRequest.builder()
                .customer(customer)
                .type(dto.getType())
                .budget(dto.getBudget())
                .requestDetails(dto.getRequestDetails())
                .status(BookingRequestStatus.PENDING)
                .build();

        BookingRequest saved = repository.save(request);

        auditLogService.logAction("CREATE_BOOKING_REQUEST", AuditEntity.BOOKING, saved.getBookingRequestId(),
                customer, LogType.INFO);

        return mapper.toResponse(saved);
    }

    @Override
    public Page<BookingRequestResponseDTO> getRequests(BookingRequestStatus status, Pageable pageable) {
        Page<BookingRequest> requests;
        if (status != null) {
            requests = repository.findByStatus(status, pageable);
        } else {
            requests = repository.findAll(pageable);
        }

        List<BookingRequestResponseDTO> content = requests.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, requests.getTotalElements());
    }

    @Override
    public Page<BookingRequestResponseDTO> getPendingRequests(Pageable pageable) {
        List<BookingRequest> pending = repository.findByStatus(BookingRequestStatus.PENDING);

        // Since repository.findByStatus returns a List, we convert to Page
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), pending.size());

        if (start > pending.size()) {
            return new PageImpl<>(List.of(), pageable, pending.size());
        }

        List<BookingRequestResponseDTO> content = pending.subList(start, end).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, pending.size());
    }

    @Override
    public BookingRequestResponseDTO getRequestById(Long id) {
        BookingRequest request = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found with id: " + id));
        return mapper.toResponse(request);
    }

    @Override
    @Transactional
    public BookingRequestResponseDTO claimRequest(Long id, User agent) {
        BookingRequest request = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found"));

        if (request.getStatus() != BookingRequestStatus.PENDING) {
            throw new IllegalStateException("Only PENDING requests can be claimed");
        }

        request.setStatus(BookingRequestStatus.ASSIGNED);
        request.setAgent(agent);

        BookingRequest saved = repository.save(request);
        auditLogService.logAction("CLAIM_BOOKING_REQUEST", AuditEntity.BOOKING, id, agent, LogType.INFO);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingRequestResponseDTO acceptRequest(Long id, User agent) {
        BookingRequest request = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found"));

        if (request.getStatus() != BookingRequestStatus.ASSIGNED || !request.getAgent().getUserId().equals(agent.getUserId())) {
            throw new IllegalStateException("Request must be ASSIGNED to the current agent to be accepted");
        }

        request.setStatus(BookingRequestStatus.ACCEPTED);

        BookingRequest saved = repository.save(request);

        notificationService.sendNotification(request.getCustomer(),
                "Your booking request #" + id + " has been accepted by agent " + agent.getFirstName(),
                NotificationCategory.BOOKING);

        auditLogService.logAction("ACCEPT_BOOKING_REQUEST", AuditEntity.BOOKING, id, agent, LogType.INFO);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingRequestResponseDTO submitFulfillment(Long id, BookingRequestSubmitDTO dto, User agent) {
        BookingRequest request = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found"));

        if (request.getStatus() != BookingRequestStatus.ACCEPTED || !request.getAgent().getUserId().equals(agent.getUserId())) {
            throw new IllegalStateException("Only the assigned agent can submit fulfillment for an accepted request");
        }

        request.setAgentRemarks(dto.getAgentRemarks());
        request.setLinkedBookingIds(dto.getLinkedBookingIds());
        request.setStatus(BookingRequestStatus.AWAITING_FEEDBACK);

        BookingRequest saved = repository.save(request);

        notificationService.sendNotification(request.getCustomer(),
                "Your booking request #" + id + " has been fulfilled. Please review the details.",
                NotificationCategory.BOOKING);

        auditLogService.logAction("SUBMIT_BOOKING_REQUEST", AuditEntity.BOOKING, id, agent, LogType.INFO);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public BookingRequestResponseDTO provideFeedback(Long id, BookingRequestFeedbackDTO dto, User customer) {
        BookingRequest request = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found"));

        if (request.getStatus() != BookingRequestStatus.AWAITING_FEEDBACK || !request.getCustomer().getUserId().equals(customer.getUserId())) {
            throw new IllegalStateException("Feedback can only be provided by the customer when the request is awaiting feedback");
        }

        request.setCustomerStatus(dto.getCustomerStatus());

        if (dto.getCustomerStatus() == BookingRequestCustomerStatus.SATISFIED) {
            request.setStatus(BookingRequestStatus.COMPLETED);
        } else if (dto.getCustomerStatus() == BookingRequestCustomerStatus.MODIFICATION_REQUIRED) {
            request.setStatus(BookingRequestStatus.PENDING);
            request.setModificationDetails(dto.getModificationDetails());
            request.setAgent(null); // Return to pool
        }

        BookingRequest saved = repository.save(request);
        auditLogService.logAction("PROVIDE_FEEDBACK", AuditEntity.BOOKING, id, customer, LogType.INFO);

        return mapper.toResponse(saved);
    }

    @Override
    public Page<BookingRequestResponseDTO> getMyRequests(Long userId, Pageable pageable) {
        List<BookingRequest> myRequests = repository.findByCustomerUserId(userId);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), myRequests.size());

        if (start > myRequests.size()) {
            return new PageImpl<>(List.of(), pageable, myRequests.size());
        }

        List<BookingRequestResponseDTO> content = myRequests.subList(start, end).stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, myRequests.size());
    }

    @Override
    public Page<BookingRequestResponseDTO> getAgentRequests(Long agentId, Pageable pageable) {
        Page<BookingRequest> requests = repository.findByAgentUserId(agentId, pageable);

        List<BookingRequestResponseDTO> content = requests.getContent().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(content, pageable, requests.getTotalElements());
    }

    @Override
    @Transactional
    public BookingRequestResponseDTO rejectRequest(Long id, BookingRequestRejectDTO dto, User agent) {
        BookingRequest request = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Booking request not found"));

        if (!request.getAgent().getUserId().equals(agent.getUserId())) {
            throw new IllegalStateException("Only the assigned agent can reject this request");
        }

        request.setStatus(BookingRequestStatus.REJECTED);
        request.setAgentRemarks(dto.getRemarks());

        BookingRequest saved = repository.save(request);

        notificationService.sendNotification(request.getCustomer(),
                "Your booking request #" + id + " has been rejected. Reason: " + dto.getRemarks(),
                NotificationCategory.BOOKING);

        auditLogService.logAction("REJECT_BOOKING_REQUEST", AuditEntity.BOOKING, id, agent, LogType.WARN);

        return mapper.toResponse(saved);
    }
}
