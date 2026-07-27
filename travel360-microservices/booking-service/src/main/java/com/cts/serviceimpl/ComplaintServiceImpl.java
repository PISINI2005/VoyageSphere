package com.cts.serviceimpl;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cts.client.UserClient;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.dto.ComplaintRequestDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.dto.ComplaintStatusUpdateDTO;
import com.cts.entity.Complaint;
import com.cts.enums.ComplaintStatus;
import com.cts.enums.ComplaintTargetType;
import com.cts.enums.NotificationCategory;
import com.cts.exception.InvalidComplaintException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.exception.UserNotFoundException;
import com.cts.mapper.ComplaintMapper;
import com.cts.repository.BookingRepository;
import com.cts.repository.ComplaintRepository;
import com.cts.repository.InvoiceRepository;
import com.cts.repository.PaymentRepository;
import com.cts.service.ComplaintService;
import com.cts.service.NotificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ComplaintServiceImpl implements ComplaintService {

    private final ComplaintRepository complaintRepo;
    private final BookingRepository bookingRepo;
    private final InvoiceRepository invoiceRepo;
    private final PaymentRepository paymentRepo;
    private final ComplaintMapper complaintMapper;
    private final AuthenticatedUserProvider authUser;
    private final NotificationService notificationService;
    private final UserClient userClient;

    /**
     * Allowed status transitions. RESOLVED and REJECTED are terminal (no outgoing moves).
     */
    private static final Map<ComplaintStatus, Set<ComplaintStatus>> ALLOWED_TRANSITIONS =
            new EnumMap<>(ComplaintStatus.class);
    static {
        ALLOWED_TRANSITIONS.put(ComplaintStatus.PENDING,
                EnumSet.of(ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED, ComplaintStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ComplaintStatus.IN_PROGRESS,
                EnumSet.of(ComplaintStatus.RESOLVED, ComplaintStatus.REJECTED));
        ALLOWED_TRANSITIONS.put(ComplaintStatus.RESOLVED, EnumSet.noneOf(ComplaintStatus.class));
        ALLOWED_TRANSITIONS.put(ComplaintStatus.REJECTED, EnumSet.noneOf(ComplaintStatus.class));
    }

    @Override
    @Transactional
    public ComplaintResponseDTO createComplaint(ComplaintRequestDTO dto) {

        Long userId = authUser.current().getUserId();
        log.info("Creating complaint for userId: {}", userId);

        // The User lives in another service/database; verify existence remotely via Feign.
        verifyUserExists(userId);

        validateTarget(dto.getTargetType(), dto.getTargetId());

        Complaint complaint = complaintMapper.toEntity(dto, userId);

        Complaint saved = complaintRepo.save(complaint);

        log.info("Complaint created successfully with ID: {}", saved.getComplaintId());

        return complaintMapper.toResponse(saved);
    }

    @Override
    public List<ComplaintResponseDTO> getMyComplaints() {

        Long userId = authUser.current().getUserId();
        log.info("Fetching complaints for current userId: {}", userId);

        List<ComplaintResponseDTO> complaints = complaintRepo.findByUserId(userId)
                .stream()
                .map(complaintMapper::toResponse)
                .toList();

        log.info("Total complaints fetched for userId {}: {}", userId, complaints.size());

        return complaints;
    }

    @Override
    public List<ComplaintResponseDTO> getComplaints(ComplaintStatus status) {

        log.info("Fetching complaints with status filter: {}", status);

        List<Complaint> complaints = (status == null)
                ? complaintRepo.findAll()
                : complaintRepo.findByStatus(status);

        log.info("Total complaints fetched: {}", complaints.size());

        return complaints.stream()
                .map(complaintMapper::toResponse)
                .toList();
    }

    @Override
    public ComplaintResponseDTO getComplaintById(Long complaintId) {

        log.info("Fetching complaint with ID: {}", complaintId);

        Complaint complaint = complaintRepo.findById(complaintId).orElseThrow(() -> {
            log.error("Complaint not found with id {}", complaintId);
            return new ResourceNotFoundException("Complaint not found");
        });

        return complaintMapper.toResponse(complaint);
    }

    @Override
    @Transactional
    public ComplaintResponseDTO updateStatus(Long complaintId, ComplaintStatusUpdateDTO dto) {

        log.info("Updating status of complaintId: {} to {}", complaintId, dto.getStatus());

        Complaint complaint = complaintRepo.findById(complaintId).orElseThrow(() -> {
            log.error("Complaint not found with id {}", complaintId);
            return new ResourceNotFoundException("Complaint not found");
        });

        ComplaintStatus current = complaint.getStatus();
        ComplaintStatus target = dto.getStatus();

        // Enforce the state machine: reject moves that aren't allowed from the current status.
        if (!ALLOWED_TRANSITIONS.getOrDefault(current, Set.of()).contains(target)) {
            log.error("Illegal complaint status transition {} -> {} for id {}", current, target, complaintId);
            throw new IllegalArgumentException(
                    "Cannot change complaint status from " + current + " to " + target);
        }

        complaint.setStatus(target);

        // Only overwrite the note when one is supplied, so an earlier note is preserved.
        if (dto.getResolutionNote() != null) {
            complaint.setResolutionNote(dto.getResolutionNote());
        }

        // Stamp the resolution time once the complaint reaches a terminal state.
        if (target == ComplaintStatus.RESOLVED || target == ComplaintStatus.REJECTED) {
            complaint.setResolvedDate(LocalDateTime.now());
        }

        Complaint saved = complaintRepo.save(complaint);

        // Notify the customer when their complaint reaches a terminal outcome.
        if (target == ComplaintStatus.RESOLVED || target == ComplaintStatus.REJECTED) {
            String outcome = target == ComplaintStatus.RESOLVED ? "resolved" : "rejected";
            String message = "Your complaint (ID: " + saved.getComplaintId() + ") has been " + outcome + "."
                    + (saved.getResolutionNote() != null ? " " + saved.getResolutionNote() : "");
            notificationService.sendNotification(saved.getUserId(), message, NotificationCategory.COMPLAINT);
        }

        log.info("Complaint {} updated to status {}", complaintId, saved.getStatus());

        return complaintMapper.toResponse(saved);
    }

    /**
     * Verifies the complainant exists in the (remote) user service. Mirrors the
     * monolith's userRepo.findById check, but the User now lives in another service,
     * so existence is confirmed via the Feign-backed {@link UserClient}.
     */
    private void verifyUserExists(Long userId) {
        try {
            if (userClient.getUser(userId) == null) {
                log.error("User not found with id {}", userId);
                throw new UserNotFoundException("User not found");
            }
        } catch (UserNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("User not found with id {}", userId);
            throw new UserNotFoundException("User not found");
        }
    }

    /**
     * Validates the complaint's optional target reference. A complaint may target a
     * BOOKING, INVOICE or PAYMENT — or nothing at all (a general complaint). When a
     * target is supplied:
     * <ul>
     *   <li>both {@code targetType} and {@code targetId} must be present (both-or-neither);</li>
     *   <li>the referenced record must exist (no DB FK enforces this);</li>
     *   <li>it must belong to the complainant — {@link AuthenticatedUserProvider#assertCanActAs(Long)}
     *       lets a customer reference only their own records while ADMIN/TRAVEL_AGENT/FINANCE_OFFICER
     *       may act on anyone's.</li>
     * </ul>
     */
    private void validateTarget(ComplaintTargetType type, Long targetId) {
        if (type == null && targetId == null) {
            return; // general complaint — nothing to validate
        }
        if (type == null || targetId == null) {
            throw new InvalidComplaintException("targetId",
                    "targetType and targetId must be provided together");
        }

        Long ownerUserId = switch (type) {
            case BOOKING -> bookingRepo.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Booking " + targetId + " not found"))
                    .getUserId();
            case INVOICE -> invoiceRepo.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice " + targetId + " not found"))
                    .getBooking().getUserId();
            case PAYMENT -> paymentRepo.findById(targetId)
                    .orElseThrow(() -> new ResourceNotFoundException("Payment " + targetId + " not found"))
                    .getInvoice().getBooking().getUserId();
        };

        authUser.assertCanActAs(ownerUserId);
    }
}
