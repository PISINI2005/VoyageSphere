package com.cts.service;

import java.util.List;

import com.cts.dto.AuthResponseDTO;
import com.cts.dto.ChangePasswordDTO;
import com.cts.dto.CreateUserDTO;
import com.cts.dto.UpdateProfileDTO;
import com.cts.dto.UserDTO;
import com.cts.dto.UserResponseDTO;
import com.cts.enums.Role;
import com.cts.enums.UserStatus;

public interface UserService {

    UserResponseDTO register(UserDTO dto);

    UserResponseDTO createUser(CreateUserDTO dto);

    AuthResponseDTO login(String email, String password);

    List<UserResponseDTO> getAllUsers(Role role);

    UserResponseDTO updateUserStatus(Long id, UserStatus status);

    UserResponseDTO getUserProfile(String email);

    UserResponseDTO updateProfile(String email, UpdateProfileDTO dto);

    void changePassword(String email, ChangePasswordDTO dto);
}