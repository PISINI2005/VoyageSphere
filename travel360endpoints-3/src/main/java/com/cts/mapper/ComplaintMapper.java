package com.cts.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.cts.dto.ComplaintRequestDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.entity.Complaint;
import com.cts.entity.User;
import com.cts.enums.ComplaintStatus;

/**
 * Maps between {@link Complaint} entities and DTOs. Stateless.
 */
@Component
public class ComplaintMapper {

    /**
     * Builds a PENDING complaint filed by the given user.
     */
    public Complaint toEntity(ComplaintRequestDTO dto, User user) {
        return Complaint.builder()
                .user(user)
                .subject(dto.getSubject())
                .description(dto.getDescription())
                .status(ComplaintStatus.PENDING)
                .targetType(dto.getTargetType())
                .targetId(dto.getTargetId())
                .createdDate(LocalDateTime.now())
                .build();
    }

    public ComplaintResponseDTO toResponse(Complaint c) {
        return ComplaintResponseDTO.builder()
                .complaintId(c.getComplaintId())
                .subject(c.getSubject())
                .description(c.getDescription())
                .status(c.getStatus())
                .targetType(c.getTargetType())
                .targetId(c.getTargetId())
                .resolutionNote(c.getResolutionNote())
                .createdDate(c.getCreatedDate())
                .resolvedDate(c.getResolvedDate())
                .userId(c.getUser() != null ? c.getUser().getUserId() : null)
                .build();
    }
}
