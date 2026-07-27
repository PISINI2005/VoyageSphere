package com.cts.entity;

import java.time.LocalDateTime;

import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "AuditLog")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuditLog {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long auditId;

	private String action;

	private LocalDateTime timestamp;
	@PrePersist
	public void prePersist() {
	    this.timestamp = LocalDateTime.now();
	}
	@Enumerated(EnumType.STRING)
	private AuditEntity entityType;

	@Enumerated(EnumType.STRING)
	private LogType logType;

	private Long entityId;

	@Column(name = "user_id")
	private Long userId;

	private String userEmail;
}
