package com.cts.dto;

import com.cts.enums.Role;
import com.cts.enums.UserStatus;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponseDTO {

    private Long userId;
    private String email;
    private Role role;
    private UserStatus status;
}
