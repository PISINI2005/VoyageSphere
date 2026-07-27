package com.cts.entity;

import java.time.LocalDateTime;

import com.cts.enums.Role;
import com.cts.enums.UserStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

	@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)
private Long userId;

@Column(unique = true, nullable = false)
private String email;

private String password;

@Column(nullable = false)
private String firstName;

@Column(nullable = false)
private String lastName;

@Enumerated(EnumType.STRING)
private Role role;

private Long phoneNo;   // keep as it is

private LocalDateTime createdAt = LocalDateTime.now();

@Enumerated(EnumType.STRING)
private UserStatus status;
}
