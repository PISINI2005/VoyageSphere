package com.cts.serviceimpl;

import org.springframework.stereotype.Service;

import com.cts.client.AuditClient;
import com.cts.dto.AuditLogRequestDTO;
import com.cts.entity.User;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.service.AuditLogService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign-backed audit logger. The monolith persisted an {@code AuditLog} entity
 * asynchronously; in the microservices topology this becomes a best-effort
 * remote call to the notification-service. Any failure is logged and swallowed
 * so audit logging never breaks the main business flow (preserving the original
 * {@code @Async} fire-and-forget semantics).
 */
@Service
@AllArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditClient auditClient;

    @Override
    public void logAction(String action,
                          AuditEntity entityType,
                          Long entityId,
                          User user,
                          LogType logType) {

        AuditLogRequestDTO dto = AuditLogRequestDTO.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .userId(user != null ? user.getUserId() : null)
                .logType(logType)
                .build();

        try {
            auditClient.record(dto);
        } catch (Exception ex) {
            log.warn("Failed to record audit log for action {} on {}: {}", action, entityType, ex.getMessage());
        }
    }
}
