package com.cts.dto;

import java.time.LocalDateTime;

import com.cts.enums.ComplaintStatus;
import com.cts.enums.ComplaintTargetType;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ComplaintResponseDTO {

    private Long complaintId;
    private String subject;
    private String description;
    private ComplaintStatus status;
    private ComplaintTargetType targetType;
    private Long targetId;
    private String resolutionNote;
    private LocalDateTime createdDate;
    private LocalDateTime resolvedDate;
    private Long userId;
}
