package com.cts.entity;

import java.time.LocalDateTime;

import com.cts.enums.NotificationCategory;
import com.cts.enums.NotificationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Notification")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Notification {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long notificationId;

	@Enumerated(EnumType.STRING)
	private NotificationCategory category;

    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    private LocalDateTime createdDate;

    @Column(name = "user_id")
    private Long userId;





    private String message;
}


