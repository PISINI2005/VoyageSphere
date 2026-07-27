package com.cts.client;

import org.springframework.stereotype.Component;

import com.cts.dto.NotificationRequestDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationClientFallback implements NotificationClient {
    @Override
    public void send(NotificationRequestDTO dto) {
        log.error("Circuit Breaker Triggered: Notification Service is unavailable. Notification failed for: {}", dto.getUserId());
    }
}
