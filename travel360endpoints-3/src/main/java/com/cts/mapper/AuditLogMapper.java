package com.cts.mapper;

import org.springframework.stereotype.Component;

import com.cts.dto.AuditLogResponseDTO;
import com.cts.entity.AuditLog;
import com.cts.entity.User;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;

/**
 * Maps between {@link AuditLog} entities and DTOs. Stateless.
 */
@Component
public class AuditLogMapper {

    public AuditLog toEntity(String action,
                             AuditEntity entityType,
                             Long entityId,
                             User user,
                             LogType logType) {
        return AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .user(user)
                .logType(logType)
                .build();
    }

    public AuditLogResponseDTO toResponse(AuditLog log) {
        User user = log.getUser();
        return AuditLogResponseDTO.builder()
                .auditId(log.getAuditId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .logType(log.getLogType())
                .timestamp(log.getTimestamp())
                .userId(user != null ? user.getUserId() : null)
                .userEmail(user != null ? user.getEmail() : null)
                .build();
    }
}
