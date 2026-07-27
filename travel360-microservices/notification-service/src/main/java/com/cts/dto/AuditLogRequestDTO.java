package com.cts.dto;

import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogRequestDTO {

    private String action;
    private AuditEntity entityType;
    private Long entityId;
    private Long userId;
    private LogType logType;
}
