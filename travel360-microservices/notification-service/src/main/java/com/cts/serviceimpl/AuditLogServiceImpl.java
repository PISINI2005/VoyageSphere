package com.cts.serviceimpl;

import com.cts.dto.AuditLogRequestDTO;
import com.cts.dto.AuditLogResponseDTO;
import com.cts.entity.AuditLog;
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
                          Long userId,
                          LogType logType) {

        AuditLog auditLog = auditLogMapper.toEntity(action, entityType, entityId, userId, logType);

        auditLogRepository.save(auditLog);
    }

    @Override
    @Async
    public void record(AuditLogRequestDTO dto) {

        AuditLog auditLog = auditLogMapper.toEntity(dto);

        auditLogRepository.save(auditLog);
    }

    @Override
    public List<AuditLogResponseDTO> getAllLogs() {
        return auditLogRepository.findAll()
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }

    @Override
    public List<AuditLogResponseDTO> getByEntity(AuditEntity entityType, Long entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }


    @Override
    public List<AuditLogResponseDTO> getByUser(Long userId) {
        return auditLogRepository.findByUserId(userId)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }
    @Override
    public List<AuditLogResponseDTO> getByAction(String action) {
        return auditLogRepository.findByAction(action)
                .stream()
                .map(auditLogMapper::toResponse)
                .toList();
    }



@Override
public List<AuditLogResponseDTO> getByLogType(LogType logType) {
    return auditLogRepository.findByLogType(logType)
            .stream()
            .map(auditLogMapper::toResponse)
            .toList();
}


}
