package com.cts.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.cts.config.FeignClientConfig;
import com.cts.dto.NotificationRequestDTO;

@FeignClient(name = "NOTIFICATION-SERVICE", configuration = FeignClientConfig.class, fallback = NotificationClientFallback.class)
public interface NotificationClient {

    @PostMapping("/api/v1/notifications")
    void send(@RequestBody NotificationRequestDTO dto);
}
