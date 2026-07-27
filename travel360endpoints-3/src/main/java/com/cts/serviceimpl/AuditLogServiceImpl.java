package com.cts.serviceimpl;

import com.cts.dto.AuditLogResponseDTO;
import com.cts.entity.AuditLog;
import com.cts.entity.User;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.mapper.AuditLogMapper;
import com.cts.repository.AuditLogRepository;
import com.cts.service.AuditLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final AuditLogMapper auditLogMapper;

    @Override
    @Async
    public void logAction(String action,
                          AuditEntity entityType,
                          Long entityId,
                          User user,
                          LogType logType) {

        log.info("Audit: action={} entityType={} entityId={} userId={} logType={}",
                action, entityType, entityId, user != null ? user.getUserId() : "system", logType);

        AuditLog auditLog = auditLogMapper.toEntity(action, entityType, entityId, user, logType);
        auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLogResponseDTO> getAllLogs() {
        log.info("Fetching all audit logs");
        List<AuditLogResponseDTO> logs = auditLogRepository.findAll()
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
        log.debug("Returned {} audit log entries", logs.size());
        return logs;
    }

    @Override
    public List<AuditLogResponseDTO> getByEntity(AuditEntity entityType, Long entityId) {
        log.info("Fetching audit logs for entityType={} entityId={}", entityType, entityId);
        List<AuditLogResponseDTO> logs = auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
        log.debug("Returned {} audit log entries for entityType={} entityId={}", logs.size(), entityType, entityId);
        return logs;
    }

    @Override
    public List<AuditLogResponseDTO> getByUser(Long userId) {
        log.info("Fetching audit logs for userId={}", userId);
        List<AuditLogResponseDTO> logs = auditLogRepository.findByUserUserId(userId)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
        log.debug("Returned {} audit log entries for userId={}", logs.size(), userId);
        return logs;
    }

    @Override
    public List<AuditLogResponseDTO> getByAction(String action) {
        log.info("Fetching audit logs for action={}", action);
        List<AuditLogResponseDTO> logs = auditLogRepository.findByAction(action)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
        log.debug("Returned {} audit log entries for action={}", logs.size(), action);
        return logs;
    }

    @Override
    public List<AuditLogResponseDTO> getByLogType(LogType logType) {
        log.info("Fetching audit logs for logType={}", logType);
        List<AuditLogResponseDTO> logs = auditLogRepository.findByLogType(logType)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
        log.debug("Returned {} audit log entries for logType={}", logs.size(), logType);
        return logs;
    }
}
