package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cts.config.AuthenticatedUser;
import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.ComplaintRequestDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.dto.ComplaintStatusUpdateDTO;
import com.cts.enums.AuditEntity;
import com.cts.enums.ComplaintStatus;
import com.cts.enums.LogType;
import com.cts.service.AuditLogService;
import com.cts.service.ComplaintService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/complaints")
@AllArgsConstructor
@Tag(name = "Complaint Controller", description = "Customers raise complaints; compliance officers review and resolve them")
@Slf4j
public class ComplaintController {

    private final ComplaintService complaintService;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;

    @Operation(summary = "Raise a complaint (customer; user id taken from the token)")
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ComplaintResponseDTO> createComplaint(@RequestBody @Valid ComplaintRequestDTO dto) {

        log.info("Received request to create complaint");
        auditLogService.logAction(AuditActions.CREATE_COMPLAINT, AuditEntity.COMPLAINT, null, currentUserId(), LogType.INFO);

        ComplaintResponseDTO response = complaintService.createComplaint(dto);

        log.info("Complaint created successfully with ID: {}", response.getComplaintId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get the current user's complaints (user id taken from the token)")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ComplaintResponseDTO>> getMyComplaints() {

        log.info("Fetching complaints for the current user");

        return new ResponseEntity<>(complaintService.getMyComplaints(), HttpStatus.OK);
    }

    @Operation(summary = "Get all complaints, optionally filtered by status (compliance review queue)")
    @GetMapping
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    public ResponseEntity<List<ComplaintResponseDTO>> getComplaints(
            @RequestParam(required = false) ComplaintStatus status) {

        log.info("Fetching complaints with status filter: {}", status);

        return new ResponseEntity<>(complaintService.getComplaints(status), HttpStatus.OK);
    }

    @Operation(summary = "Get a complaint by ID (compliance review)")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    public ResponseEntity<ComplaintResponseDTO> getComplaintById(@PathVariable Long id) {

        log.info("Fetching complaint with ID: {}", id);

        return new ResponseEntity<>(complaintService.getComplaintById(id), HttpStatus.OK);
    }

    @Operation(summary = "Update a complaint's status (compliance officer resolves/rejects)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('COMPLIANCE_OFFICER','ADMIN')")
    public ResponseEntity<ComplaintResponseDTO> updateStatus(@PathVariable Long id,
                                                             @RequestBody @Valid ComplaintStatusUpdateDTO dto) {

        log.info("Received request to update status of complaint with ID: {}", id);
        auditLogService.logAction(AuditActions.UPDATE_COMPLAINT_STATUS, AuditEntity.COMPLAINT, id, currentUserId(), LogType.INFO);

        ComplaintResponseDTO response = complaintService.updateStatus(id, dto);

        log.info("Complaint {} status updated successfully", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private Long currentUserId() {
        AuthenticatedUser caller = authUser.currentOrNull();
        return caller != null ? caller.getUserId() : null;
    }
}
