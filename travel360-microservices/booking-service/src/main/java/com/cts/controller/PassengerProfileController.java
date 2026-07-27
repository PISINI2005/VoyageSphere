package com.cts.controller;

import com.cts.dto.PassengerProfileRequestDTO;
import com.cts.dto.PassengerProfileResponseDTO;
import com.cts.service.PassengerProfileService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/passengers/profiles")
@AllArgsConstructor
public class PassengerProfileController {

    private final PassengerProfileService profileService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PassengerProfileResponseDTO> createProfile(
            @Valid @RequestBody PassengerProfileRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(profileService.createProfile(dto));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<PassengerProfileResponseDTO>> getMyProfiles() {
        return ResponseEntity.ok(profileService.getMyProfiles());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PassengerProfileResponseDTO> getProfileById(@PathVariable Long id) {
        return ResponseEntity.ok(profileService.getProfileById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PassengerProfileResponseDTO> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody PassengerProfileRequestDTO dto) {
        return ResponseEntity.ok(profileService.updateProfile(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Void> deleteProfile(@PathVariable Long id) {
        profileService.deleteProfile(id);
        return ResponseEntity.noContent().build();
    }
}
