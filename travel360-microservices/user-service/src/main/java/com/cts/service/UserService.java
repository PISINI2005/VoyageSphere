package com.cts.service;

import com.cts.dto.AuthResponseDTO;
import com.cts.dto.CreateUserDTO;
import com.cts.dto.UserDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.enums.UserStatus;

import java.util.List;

public interface UserService {

    UserResponseDTO register(UserDTO dto);

    UserResponseDTO createUser(CreateUserDTO dto);

    AuthResponseDTO login(String email, String password);

    List<UserResponseDTO> getAllUsers();

    UserResponseDTO getUserById(Long userId);

    UserResponseDTO updateUserStatus(Long id, UserStatus status);
}
