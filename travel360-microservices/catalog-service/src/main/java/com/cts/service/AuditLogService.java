package com.cts.service;

import com.cts.config.AuthenticatedUser;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;

public interface AuditLogService {

    void logAction(String action,
                   AuditEntity entityType,
                   Long entityId,
                   AuthenticatedUser user,
                   LogType logType);
}
