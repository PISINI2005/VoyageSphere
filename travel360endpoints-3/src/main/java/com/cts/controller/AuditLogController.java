package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.dto.AuditLogResponseDTO;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.service.AuditLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/auditLogs")
@AllArgsConstructor
@Tag(name = "Audit Log Controller", description = "Read-only access to system audit logs filtered by entity, user, action, or severity")
@Slf4j
public class AuditLogController {

	private AuditLogService auditservice;

	@Operation(summary = "Query audit logs — filter by entityType+entityId, userId, action, or logType (all optional; returns all logs if none provided)")
	@GetMapping
	@PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
	public ResponseEntity<List<AuditLogResponseDTO>> getLogs(
			@RequestParam(required = false) AuditEntity entityType,
			@RequestParam(required = false) Long entityId,
			@RequestParam(required = false) Long userId,
			@RequestParam(required = false) String action,
			@RequestParam(required = false) LogType logType) {

		List<AuditLogResponseDTO> list;

		if (entityType != null && entityId != null) {
			log.info("Fetching audit logs for entityType={}, entityId={}", entityType, entityId);
			list = auditservice.getByEntity(entityType, entityId);
		} else if (userId != null) {
			log.info("Fetching audit logs for userId={}", userId);
			list = auditservice.getByUser(userId);
		} else if (action != null) {
			log.info("Fetching audit logs for action={}", action);
			list = auditservice.getByAction(action);
		} else if (logType != null) {
			log.info("Fetching audit logs for logType={}", logType);
			list = auditservice.getByLogType(logType);
		} else {
			log.info("Fetching all audit logs");
			list = auditservice.getAllLogs();
		}

		return new ResponseEntity<>(list, HttpStatus.OK);
	}
}
