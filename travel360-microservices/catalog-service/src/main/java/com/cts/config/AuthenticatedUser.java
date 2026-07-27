package com.cts.config;

import com.cts.enums.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight representation of the currently authenticated user, built directly
 * from JWT claims. This service does not own a {@code User} JPA entity, so this
 * type carries just the identity information the catalog layer needs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser {

    private Long userId;
    private String email;
    private Role role;
}
