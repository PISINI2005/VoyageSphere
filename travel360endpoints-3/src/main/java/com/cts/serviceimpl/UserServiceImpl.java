package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.config.DataSeeder;
import com.cts.config.JWTUtil;
import com.cts.constants.AuditActions;
import com.cts.dto.AuthResponseDTO;
import com.cts.dto.ChangePasswordDTO;
import com.cts.dto.CreateUserDTO;
import com.cts.dto.UpdateProfileDTO;
import com.cts.dto.UserDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.User;
import com.cts.enums.AuditEntity;
import com.cts.enums.LogType;
import com.cts.enums.Role;
import com.cts.enums.UserStatus;
import com.cts.exception.UserNotFoundException;
import com.cts.mapper.UserMapper;
import com.cts.repository.UserRepository;
import com.cts.service.AuditLogService;
import com.cts.service.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JWTUtil jwtUtil;
    private final AuditLogService auditLogService;
    private final UserMapper userMapper;
    private final AuthenticatedUserProvider authUser;

    @Override
    public UserResponseDTO register(UserDTO dto) {

        log.info("Registering new user with email: {}", dto.getEmail());

        // Self-registration is always a CUSTOMER; the role is never taken from the client.
        User user = userMapper.toEntity(dto, encoder.encode(dto.getPassword()), Role.CUSTOMER);

        user = repo.save(user);
        auditLogService.logAction(AuditActions.REGISTER_USER, AuditEntity.USER, user.getUserId(), user, LogType.INFO);

        log.info("User registered successfully with ID: {}", user.getUserId());

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponseDTO createUser(CreateUserDTO dto) {

        log.info("Admin creating user with email: {} and role: {}", dto.getEmail(), dto.getRole());

        // System-generated users get the shared default password.
        User user = userMapper.toEntity(dto, encoder.encode(DataSeeder.DEFAULT_PASSWORD));

        user = repo.save(user);
        auditLogService.logAction(AuditActions.REGISTER_USER, AuditEntity.USER, user.getUserId(), user, LogType.INFO);

        log.info("User created successfully with ID: {}", user.getUserId());

        return userMapper.toResponse(user);
    }

    @Override
    public AuthResponseDTO login(String email, String password) {

        log.info("Login attempt for email: {}", email);

        User user = repo.findByEmail(email);

        if (user == null || !encoder.matches(password, user.getPassword())||user.getStatus()!=UserStatus.ACTIVE) {
            log.error("Invalid login attempt for email: {}", email);
            throw new UserNotFoundException("Invalid login");
        }
        
        

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getUserId());
        auditLogService.logAction(AuditActions.LOGIN_USER, AuditEntity.USER, user.getUserId(), user, LogType.INFO);

        log.info("User logged in successfully with ID: {}", user.getUserId());

        return AuthResponseDTO.builder()
                .token(token)
                .user(userMapper.toResponse(user))
                .build();
    }

    @Override
    public List<UserResponseDTO> getAllUsers(Role role) {

        log.info("Fetching users, role filter: {}", role);

        List<User> users;

        if (role != null) {
            users = repo.findByRole(role);
        } else {
            users = repo.findAll();
        }

        return users.stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public UserResponseDTO updateUserStatus(Long id, UserStatus status) {

        log.info("Updating status for user with ID: {} to {}", id, status);

        User user = repo.findById(id).orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setStatus(status);
        user = repo.save(user);

        log.info("User status updated successfully for ID: {}", user.getUserId());

        return userMapper.toResponse(user);
    }

    @Override
    public UserResponseDTO getUserProfile(String email) {
        log.info("Fetching profile for user: {}", email);
        User user = repo.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }
        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public UserResponseDTO updateProfile(String email, UpdateProfileDTO dto) {
        log.info("Updating profile for user: {}", email);
        User user = repo.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setPhoneNo(dto.getPhoneNo());
        user = repo.save(user);

        auditLogService.logAction(AuditActions.UPDATE_USER_PROFILE, AuditEntity.USER, user.getUserId(), user, LogType.INFO);
        log.info("User profile updated successfully for email: {}", email);

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(String email, ChangePasswordDTO dto) {
        log.info("Changing password for user: {}", email);
        User user = repo.findByEmail(email);
        if (user == null) {
            throw new UserNotFoundException("User not found with email: " + email);
        }

        if (!encoder.matches(dto.getOldPassword(), user.getPassword())) {
            log.error("Incorrect old password for user: {}", email);
            throw new UserNotFoundException("Incorrect old password");
        }

        user.setPassword(encoder.encode(dto.getNewPassword()));
        user = repo.save(user);

        auditLogService.logAction(AuditActions.CHANGE_PASSWORD, AuditEntity.USER, user.getUserId(), user, LogType.INFO);
        log.info("Password changed successfully for user: {}", email);
    }
}
