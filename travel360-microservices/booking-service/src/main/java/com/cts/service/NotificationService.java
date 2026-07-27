package com.cts.service;

import com.cts.enums.NotificationCategory;

public interface NotificationService {

	void sendNotification(Long userId, String message, NotificationCategory category);

}
