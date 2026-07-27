package com.cts.client;

import com.cts.dto.AuditLogRequestDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AuditClientFallback implements AuditClient {
    @Override
    public void record(AuditLogRequestDTO dto) {
        log.error("Circuit Breaker Triggered: Audit Service (NOTIFICATION-SERVICE) is unavailable. Logging audit request manually: {}", dto);
    }
}
