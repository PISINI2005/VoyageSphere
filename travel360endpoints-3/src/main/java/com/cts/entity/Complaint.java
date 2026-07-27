package com.cts.entity;

import java.time.LocalDateTime;

import com.cts.enums.ComplaintStatus;
import com.cts.enums.ComplaintTargetType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "Complaint")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long complaintId;

    private String subject;

    private String description;

    @Enumerated(EnumType.STRING)
    private ComplaintStatus status;

    // What the complaint is about. Both null = a general complaint (not tied to a record).
    // This is a soft reference (type + id), mirroring the audit-log convention; integrity
    // is enforced in the service layer, not by a DB foreign key.
    @Enumerated(EnumType.STRING)
    private ComplaintTargetType targetType;

    private Long targetId;

    private LocalDateTime createdDate;

    private LocalDateTime resolvedDate;

    @Column(length = 1000)
    private String resolutionNote;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
