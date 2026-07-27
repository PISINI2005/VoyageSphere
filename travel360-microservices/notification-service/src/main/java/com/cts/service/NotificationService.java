package com.cts.service;

import java.util.List;

import com.cts.dto.NotificationRequestDTO;
import com.cts.dto.NotificationResponseDTO;
import com.cts.enums.NotificationCategory;

public interface NotificationService {



	void sendNotification(Long userId, String message, NotificationCategory category);

	NotificationResponseDTO create(NotificationRequestDTO dto);

	List<NotificationResponseDTO> getUserNotifications(Long userId);

	List<NotificationResponseDTO> getMyNotifications();

	NotificationResponseDTO markAsRead(Long notificationId);

	int markAllAsRead();


}
