package com.cts.dto;

import com.cts.enums.NotificationCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequestDTO {

    private Long userId;
    private String message;
    private NotificationCategory category;
}
