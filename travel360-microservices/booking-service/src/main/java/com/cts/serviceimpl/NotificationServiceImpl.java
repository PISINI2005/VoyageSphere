package com.cts.serviceimpl;

import org.springframework.stereotype.Service;

import com.cts.client.NotificationClient;
import com.cts.dto.NotificationRequestDTO;
import com.cts.enums.NotificationCategory;
import com.cts.service.NotificationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationClient notificationClient;

    @Override
    public void sendNotification(Long userId, String message, NotificationCategory category) {

        log.info("Sending notification to userId: {} with category: {}", userId, category);

        NotificationRequestDTO dto = NotificationRequestDTO.builder()
                .userId(userId)
                .message(message)
                .category(category)
                .build();

        // Best-effort, like the monolith's design: a notification failure must not
        // break the booking/payment flow.
        try {
            notificationClient.send(dto);
            log.info("Notification dispatched successfully for userId: {}", userId);
        } catch (Exception e) {
            log.error("Failed to send notification via notification-service: {}", e.getMessage());
        }
    }
}
