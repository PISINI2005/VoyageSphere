package com.cts.mapper;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.cts.dto.CreateUserDTO;
import com.cts.dto.UserDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.entity.User;
import com.cts.enums.Role;
import com.cts.enums.UserStatus;

@Component
public class UserMapper {

    public User toEntity(UserDTO dto, String encodedPassword, Role role) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .password(encodedPassword)
                .role(role)
                .phoneNo(dto.getPhoneNo())
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public User toEntity(CreateUserDTO dto, String encodedPassword) {
        return User.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
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
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .status(user.getStatus())
                .phoneNo(user.getPhoneNo())
                .build();
    }
}