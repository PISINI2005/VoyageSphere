package com.cts.service;

import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;

public interface AuditLogService {

    void logAction(String action,
                   AuditEntity entityType,
                   Long entityId,
                   Long userId,
                   LogType logType);
}
