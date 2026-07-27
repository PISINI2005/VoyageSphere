package com.cts.mapper;

import org.springframework.stereotype.Component;

import com.cts.dto.AuditLogRequestDTO;
import com.cts.dto.AuditLogResponseDTO;
import com.cts.entity.AuditLog;
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
                             Long userId,
                             LogType logType) {
        return AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(userId)
                .logType(logType)
                .build();
    }

    public AuditLog toEntity(AuditLogRequestDTO dto) {
        return toEntity(dto.getAction(), dto.getEntityType(), dto.getEntityId(), dto.getUserId(), dto.getLogType());
    }

    public AuditLogResponseDTO toResponse(AuditLog log) {
        return AuditLogResponseDTO.builder()
                .auditId(log.getAuditId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .logType(log.getLogType())
                .timestamp(log.getTimestamp())
                .userId(log.getUserId())
                .userEmail(log.getUserEmail())
                .build();
    }
}
