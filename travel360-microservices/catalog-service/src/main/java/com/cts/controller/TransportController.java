package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.TransportDTO;
import com.cts.dto.TransportResponseDTO;
import com.cts.dto.TransportStatusUpdateDTO;
import com.cts.entity.Transport;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.enums.TransportStatus;
import com.cts.service.AuditLogService;
import com.cts.service.TransportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/transports")
@AllArgsConstructor
@Validated
@Tag(name = "Transport Controller", description = "Manage bus transport inventory and search by route or status")
@Slf4j
public class TransportController {

    private final TransportService service;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;

    @Operation(summary = "Add a new transport vehicle to the inventory")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<Transport> addTransport(@RequestBody @Valid TransportDTO dto) {
        log.info("addTransport() is called");
        log.debug("Request payload: {}", dto);
        auditLogService.logAction(AuditActions.CREATE_TRANSPORT, AuditEntity.TRANSPORT, null, authUser.currentOrNull(), LogType.INFO);
        return new ResponseEntity<>(service.addTransport(dto), HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing transport by ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<Transport> updateTransport(@PathVariable Long id, @RequestBody @Valid TransportDTO dto) {
        log.info("updateTransport() is called for id: {}", id);
        log.debug("Update payload: {}", dto);
        auditLogService.logAction(AuditActions.UPDATE_TRANSPORT, AuditEntity.TRANSPORT, id, authUser.currentOrNull(), LogType.INFO);
        return new ResponseEntity<>(service.updateTransport(id, dto), HttpStatus.OK);
    }

    @Operation(summary = "Update only a transport's status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<Transport> updateTransportStatus(@PathVariable Long id, @RequestBody @Valid TransportStatusUpdateDTO dto) {
        log.info("updateTransportStatus() is called for id: {}", id);
        log.debug("Status update payload: {}", dto);
        auditLogService.logAction(AuditActions.UPDATE_TRANSPORT, AuditEntity.TRANSPORT, id, authUser.currentOrNull(), LogType.INFO);
        return new ResponseEntity<>(service.updateTransportStatus(id, dto.getStatus()), HttpStatus.OK);
    }

    @Operation(summary = "Get, search, or filter transports — optionally by route (source + destination) and/or status")
    @GetMapping
    public ResponseEntity<List<TransportResponseDTO>> getTransports(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) TransportStatus status,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size) {

        List<TransportResponseDTO> result;
        if (source != null && destination != null) {
            log.info("Searching transports by route: {} → {} (page={}, size={})", source, destination, page, size);
            result = service.findByRoute(source, destination, page, size);
        } else if (status != null) {
            log.info("Filtering transports by status: {} (page={}, size={})", status, page, size);
            result = service.findByStatus(status, page, size);
        } else {
            log.info("Fetching all transports (page={}, size={})", page, size);
            result = service.getAllTransports(page, size);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @Operation(summary = "Soft-delete a transport by setting its status to OUT_OF_SERVICE")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteTransport(@PathVariable Long id) {

        log.info("Received request to delete transport with ID: {}", id);
        auditLogService.logAction(AuditActions.DELETE_TRANSPORT, AuditEntity.TRANSPORT, id, authUser.currentOrNull(), LogType.WARN);

        service.deleteTransport(id);

        log.info("Transport {} deleted (deactivated) successfully", id);

        return new ResponseEntity<>("Transport deactivated successfully", HttpStatus.OK);
    }
    
    @Operation(summary = "Get a transport by ID")
    @GetMapping("/{id}")
    public ResponseEntity<TransportResponseDTO> getTransportById(@PathVariable @Min(1) Long id) {

        log.info("Received request to fetch transport with ID: {}", id);

        TransportResponseDTO response = service.getTransportById(id);

        log.info("Transport fetched successfully with ID: {}", id);

        return ResponseEntity.ok(response);
    }
}
