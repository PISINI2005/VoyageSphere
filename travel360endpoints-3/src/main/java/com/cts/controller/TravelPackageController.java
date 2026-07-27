package com.cts.controller;

import java.time.LocalDate;
import org.springframework.data.domain.Page; // Added import for Page wrapper
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.*;
import com.cts.entity.TravelPackage;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.enums.TravelPackageCategory;
import com.cts.service.AuditLogService;
import com.cts.service.TravelPackageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping("/api/v1/packages")
@AllArgsConstructor
@Validated
@Tag(name = "Travel Package Controller", description = "Manage curated travel packages and browse by category")
@Slf4j
public class TravelPackageController {

    private final TravelPackageService service;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;

    @Operation(summary = "Add a new travel package")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<TravelPackage> addPackage(
            @RequestBody @Valid TravelPackageDTO dto) {

        log.info("Received request to add travel package");
        auditLogService.logAction(AuditActions.CREATE_PACKAGE, AuditEntity.TRAVELPACKAGE, null, authUser.currentOrNull(), LogType.INFO);

        TravelPackage response = service.addPackage(dto);

        log.info("Travel package created successfully with ID: {}", response.getPackageId());

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Update an existing travel package by ID")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<TravelPackage> updatePackage(
            @PathVariable Long id,
            @RequestBody @Valid TravelPackageDTO dto) {

        log.info("Received request to update travel package with ID: {}", id);
        auditLogService.logAction(AuditActions.UPDATE_PACKAGE, AuditEntity.TRAVELPACKAGE, id, authUser.currentOrNull(), LogType.INFO);

        TravelPackage response = service.updatePackage(id, dto);

        log.info("Travel package updated successfully with ID: {}", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Update only a travel package's status")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<TravelPackage> updatePackageStatus(
            @PathVariable Long id,
            @RequestBody @Valid PackageStatusUpdateDTO dto) {

        log.info("Received request to update status of travel package with ID: {}", id);
        auditLogService.logAction(AuditActions.UPDATE_PACKAGE, AuditEntity.TRAVELPACKAGE, id, authUser.currentOrNull(), LogType.INFO);

        TravelPackage response = service.updatePackageStatus(id, dto.getStatus());

        log.info("Travel package status updated successfully with ID: {}", id);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get all travel packages, optionally filtered by category")
    @GetMapping
    // FIX: Changed raw list assignment to explicit Page wildcard wrapper response mapping
    public ResponseEntity<Page<TravelPackageResponseDTO>> getAll(
          @RequestParam(required = false) TravelPackageCategory category,
          @RequestParam(defaultValue = "0") @Min(0) int page,
          @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size,
          @RequestParam(required = false) Double min, // Add this
          @RequestParam(required = false) Double max  // Add this
  ) {
      Page<TravelPackageResponseDTO> packagePage;
      if (category != null) {
          packagePage = service.searchByCategory(category, page, size, min, max); // Pass min, max
      } else {
          packagePage = service.getAllPackages(page, size, min, max); // Pass min, max
      }
      // .

        // FIX: Replaced .size() method query with total elements lookup from Page envelope
        log.info("Returned {} total travel package records matching current query context", packagePage.getTotalElements());

        return new ResponseEntity<>(packagePage, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TravelPackageResponseDTO> getById(
            @PathVariable Long id,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {

        TravelPackageResponseDTO response = service.getTravelPackageById(id, date);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Soft-delete a travel package by setting its status to INACTIVE")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deletePackage(@PathVariable Long id) {

        log.info("Received request to delete travel package with ID: {}", id);
        auditLogService.logAction(AuditActions.DELETE_PACKAGE, AuditEntity.TRAVELPACKAGE, id, authUser.currentOrNull(), LogType.WARN);

        service.deletePackage(id);

        log.info("Package {} deleted (deactivated) successfully", id);

        return new ResponseEntity<>("Travel package deactivated successfully", HttpStatus.OK);
    }
}