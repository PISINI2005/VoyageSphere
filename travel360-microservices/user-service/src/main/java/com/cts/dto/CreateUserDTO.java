package com.cts.dto;

import com.cts.enums.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request used by an ADMIN to create privileged (non-customer) users.
 * No password field: system-generated users receive a default password.
 */
@Data
public class CreateUserDTO {

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@NotNull(message = "Role is required")
	private Role role;

	@NotNull(message = "Phone number cannot be empty")
	@Min(value = 1000000000L, message = "Phone number must be a valid 10-digit number")
	@Max(value = 9999999999L, message = "Phone number must be a valid 10-digit number")
	private Long phoneNo;

}
