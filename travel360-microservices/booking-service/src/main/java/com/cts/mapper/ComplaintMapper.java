package com.cts.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.cts.dto.ComplaintRequestDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.entity.Complaint;
import com.cts.enums.ComplaintStatus;

/**
 * Maps between {@link Complaint} entities and DTOs. Stateless.
 */
@Component
public class ComplaintMapper {

    /**
     * Builds a PENDING complaint filed by the given user (scalar user id; the User
     * lives in another service and is resolved remotely via Feign when needed).
     */
    public Complaint toEntity(ComplaintRequestDTO dto, Long userId) {
        return Complaint.builder()
                .userId(userId)
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
                .userId(c.getUserId())
                .build();
    }
}
