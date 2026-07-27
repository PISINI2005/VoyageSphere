package com.cts.dto;

import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload posted to the notification-service to record an audit entry.
 * Replaces the in-process {@code AuditLog} entity write the monolith performed.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLogRequestDTO {

    private String action;
    private AuditEntity entityType;
    private Long entityId;
    private Long userId;
    private LogType logType;
}
