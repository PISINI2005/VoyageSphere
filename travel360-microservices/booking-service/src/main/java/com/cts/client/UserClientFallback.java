package com.cts.client;

import org.springframework.stereotype.Component;

import com.cts.dto.UserResponseDTO;
import com.cts.enums.Role;

@Component
public class UserClientFallback implements UserClient {
    @Override
    public UserResponseDTO getUser(Long userId) {
        return UserResponseDTO.builder()
                .userId(userId)
                .email("unavailable@travel360.com")
                .role(Role.CUSTOMER)
                .status("Unknown (Service Unavailable)")
                .build();
    }
}
