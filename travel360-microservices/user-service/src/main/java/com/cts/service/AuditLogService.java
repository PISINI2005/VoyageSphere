package com.cts.service;

import com.cts.entity.User;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;

/**
 * Audit logging contract. The method signature is preserved from the monolith
 * so call-sites are unchanged; the implementation now forwards to the
 * notification-service over Feign instead of writing a local AuditLog row.
 */
public interface AuditLogService {

    void logAction(String action, AuditEntity entityType, Long entityId, User user, LogType logType);
}
