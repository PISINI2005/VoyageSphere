package com.cts.controller;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.constants.AuditActions;
import com.cts.dto.*;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.enums.Role;
import com.cts.service.AuditLogService;
import com.cts.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@AllArgsConstructor
@Tag(name = "User Controller", description = "Operations related to User Registration Login and getAll")
@Slf4j
public class UserController {

    private final UserService service;
    private final AuthenticatedUserProvider authUser;
    private final AuditLogService auditLogService;

    @Operation(summary = "Register a new user")
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserDTO dto) {

        log.info("Received request to register user with email: {}", dto.getEmail());
        auditLogService.logAction(AuditActions.REGISTER_USER, AuditEntity.USER, null, authUser.currentOrNull(), LogType.INFO);

        UserResponseDTO registeredUser = service.register(dto);

        log.info("User registered successfully with ID: {}", registeredUser.getUserId());

        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }
    @Operation(summary = "Login for User")
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginDTO dto) {

        log.info("Login attempt for email: {}", dto.getEmail());
        auditLogService.logAction(AuditActions.LOGIN_USER, AuditEntity.USER, null, authUser.currentOrNull(), LogType.INFO);

        AuthResponseDTO authResponse = service.login(dto.getEmail(), dto.getPassword());

        log.info("User logged in successfully with ID: {}", authResponse.getUser().getUserId());

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }
    @Operation(summary = "Create a privileged (non-customer) user — admin only")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody @Valid CreateUserDTO dto) {

        log.info("Admin request to create user with email: {} and role: {}", dto.getEmail(), dto.getRole());
        auditLogService.logAction(AuditActions.REGISTER_USER, AuditEntity.USER, null, authUser.currentOrNull(), LogType.INFO);

        UserResponseDTO createdUser = service.createUser(dto);

        log.info("User created successfully with ID: {}", createdUser.getUserId());

        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT')")
    public ResponseEntity<List<UserResponseDTO>> getAll(
            @RequestParam(required = false) Role role) {

        log.info("Fetching users with role: {}", role);

        List<UserResponseDTO> users = service.getAllUsers(role);

        return ResponseEntity.ok(users);
    }


    @Operation(summary = "Update a user's account status (admin only)")
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDTO> updateUserStatus(@PathVariable Long id, @RequestBody @Valid UserStatusUpdateDTO dto) {

        log.info("Admin request to update status for user with ID: {} to {}", id, dto.getStatus());
        auditLogService.logAction(AuditActions.UPDATE_USER_STATUS, AuditEntity.USER, id, authUser.currentOrNull(), LogType.INFO);

        UserResponseDTO updated = service.updateUserStatus(id, dto.getStatus());

        log.info("User status updated successfully for ID: {}", updated.getUserId());

        return new ResponseEntity<>(updated, HttpStatus.OK);
    }

    @Operation(summary = "Get current user profile")
    @GetMapping("/profile")
    public ResponseEntity<UserResponseDTO> getProfile() {
        String email = authUser.current().getEmail();
        log.info("Fetching profile for user: {}", email);
        UserResponseDTO profile = service.getUserProfile(email);
        return ResponseEntity.ok(profile);
    }

    @Operation(summary = "Update current user profile")
    @PutMapping("/profile")
    public ResponseEntity<UserResponseDTO> updateProfile(@RequestBody @Valid UpdateProfileDTO dto) {
        String email = authUser.current().getEmail();
        log.info("Updating profile for user: {}", email);
        UserResponseDTO updated = service.updateProfile(email, dto);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Change current user password")
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordDTO dto) {
        String email = authUser.current().getEmail();
        log.info("Changing password for user: {}", email);
        service.changePassword(email, dto);
        return ResponseEntity.ok().build();
    }
}
