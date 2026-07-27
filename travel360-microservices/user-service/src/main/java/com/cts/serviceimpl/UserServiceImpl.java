package com.cts.serviceimpl;

import com.cts.config.AuthenticatedUserProvider;
import com.cts.config.JWTUtil;
import com.cts.constants.AuditActions;
import com.cts.dto.AuthResponseDTO;
import com.cts.dto.CreateUserDTO;
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

	/** Shared default password assigned to every system-generated user. */
	private static final String DEFAULT_PASSWORD = "Welcome@123";

	private final UserRepository repo;
	private final PasswordEncoder encoder;
	private final JWTUtil jwtUtil;
	private final AuditLogService auditLogService;
	private final UserMapper userMapper;
	private final AuthenticatedUserProvider authuser;

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
		User user = userMapper.toEntity(dto, encoder.encode(DEFAULT_PASSWORD));

		user = repo.save(user);
		auditLogService.logAction(AuditActions.REGISTER_USER, AuditEntity.USER, user.getUserId(), user, LogType.INFO);

		log.info("User created successfully with ID: {}", user.getUserId());

		return userMapper.toResponse(user);
	}

	@Override
	public AuthResponseDTO login(String email, String password) {

		log.info("Login attempt for email: {}", email);

		User user = repo.findByEmail(email);

		if (user == null || !encoder.matches(password, user.getPassword())) {
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
	public List<UserResponseDTO> getAllUsers() {

		log.info("Fetching all users");

		List<UserResponseDTO> users = repo.findAll().stream().map(userMapper::toResponse).toList();

		log.info("Total users fetched: {}", users.size());

		return users;
	}

	@Override
	public UserResponseDTO getUserById(Long userId) {
		authuser.assertCanActAs(userId);

		log.info("Fetching user with ID: {}", userId);

		User user = repo.findById(userId).orElseThrow(() -> {
			log.error("User not found with id {}", userId);
			return new UserNotFoundException("User not found");
		});

		return userMapper.toResponse(user);
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
}
