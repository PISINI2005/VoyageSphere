package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cts.config.FeignClientConfig;
import com.cts.dto.AuditLogRequestDTO;

@FeignClient(name = "NOTIFICATION-SERVICE", contextId = "auditClient", configuration = FeignClientConfig.class, fallback = AuditClientFallback.class)
public interface AuditClient {

    @PostMapping("/api/v1/auditLogs")
    void record(@RequestBody AuditLogRequestDTO dto);
}
