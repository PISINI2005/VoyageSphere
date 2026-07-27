package com.cts.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.cts.dto.CreateUserDTO;
import com.cts.dto.UserDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.User;
import com.cts.enums.Role;
import com.cts.enums.UserStatus;

/**
 * Maps between {@link User} entities and their DTO representations.
 * Stateless and side-effect free.
 */
@Component
public class UserMapper {

    /**
     * Builds a new {@link User} from a self-registration request.
     * The role is decided by the caller (never the client) and the password is
     * encoded by the caller so the encoder stays in the service layer.
     */
    public User toEntity(UserDTO dto, String encodedPassword, Role role) {
        return User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .role(role)
                .phoneNo(dto.getPhoneNo())
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Builds a new {@link User} from an admin-driven creation request.
     * The role comes from the request; the password (a default) is encoded by the caller.
     */
    public User toEntity(CreateUserDTO dto, String encodedPassword) {
        return User.builder()
                .email(dto.getEmail())
                .password(encodedPassword)
                .role(dto.getRole())
                .phoneNo(dto.getPhoneNo())
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public UserResponseDTO toResponse(User user) {
        return UserResponseDTO.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .build();
    }
}
