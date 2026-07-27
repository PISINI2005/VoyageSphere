package com.cts.serviceimpl;

import com.cts.client.AuditClient;
import com.cts.config.AuthenticatedUser;
import com.cts.dto.AuditLogRequestDTO;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditClient auditClient;

    @Override
    @Async
    public void logAction(String action,
                          AuditEntity entityType,
                          Long entityId,
                          AuthenticatedUser user,
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
        } catch (Exception e) {
            log.error("Failed to record audit log via notification-service: {}", e.getMessage());
        }
    }
}
