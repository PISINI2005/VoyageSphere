package com.cts.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.cts.dto.NotificationRequestDTO;
import com.cts.dto.NotificationResponseDTO;
import com.cts.entity.Notification;
import com.cts.enums.NotificationCategory;
import com.cts.enums.NotificationStatus;

/**
 * Maps between {@link Notification} entities and DTOs. Stateless.
 */
@Component
public class NotificationMapper {

    /**
     * Builds an UNREAD notification addressed to the given user.
     */
    public Notification toEntity(Long userId, String message, NotificationCategory category) {
        return Notification.builder()
                .userId(userId)
                .message(message)
                .category(category)
                .status(NotificationStatus.UNREAD)
                .createdDate(LocalDateTime.now())
                .build();
    }

    /**
     * Builds an UNREAD notification from the inbound request DTO.
     */
    public Notification toEntity(NotificationRequestDTO dto) {
        return toEntity(dto.getUserId(), dto.getMessage(), dto.getCategory());
    }

    public NotificationResponseDTO toResponse(Notification n) {
        return NotificationResponseDTO.builder()
                .notificationId(n.getNotificationId())
                .message(n.getMessage())
                .category(n.getCategory())
                .status(n.getStatus())
                .createdDate(n.getCreatedDate())
                .build();
    }
}
