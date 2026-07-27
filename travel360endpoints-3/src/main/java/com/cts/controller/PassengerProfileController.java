package com.cts.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cts.dto.PassengerProfileRequestDTO;
import com.cts.dto.PassengerProfileResponseDTO;
import com.cts.service.PassengerProfileService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
@RestController
@RequestMapping("/api/v1/passengers/profiles")
@AllArgsConstructor
public class PassengerProfileController {

    private final PassengerProfileService profileService;

    @PostMapping
    @PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
    public ResponseEntity<PassengerProfileResponseDTO> createProfile(
            @Valid @RequestBody PassengerProfileRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.createProfile(dto));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
    public ResponseEntity<List<PassengerProfileResponseDTO>> getMyProfiles(
            @RequestParam(required = false) Long userId) {

        return ResponseEntity.ok(
                profileService.getMyProfiles(userId)
        );
    }

   @GetMapping("/{id}")
@PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
public ResponseEntity<PassengerProfileResponseDTO> getProfileById(
        @PathVariable Long id,
        @RequestParam(required = false) Long userId) {

    return ResponseEntity.ok(profileService.getProfileById(id, userId));
}
@PutMapping("/{id}")
@PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
public ResponseEntity<PassengerProfileResponseDTO> updateProfile(
        @PathVariable Long id,
        @RequestParam(required = false) Long userId,
        @Valid @RequestBody PassengerProfileRequestDTO dto) {

    return ResponseEntity.ok(profileService.updateProfile(id, dto, userId));
}

   @DeleteMapping("/{id}")
@PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT')")
public ResponseEntity<Void> deleteProfile(
        @PathVariable Long id,
        @RequestParam(required = false) Long userId) {

    profileService.deleteProfile(id, userId);
    return ResponseEntity.noContent().build();
}
}