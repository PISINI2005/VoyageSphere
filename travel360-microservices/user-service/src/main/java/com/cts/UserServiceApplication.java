package com.cts;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Travel360 user-service.
 *
 * <p>Owns the User domain (registration, login, JWT minting). Registers with
 * Eureka, pulls its configuration from the Config Server and talks to the
 * notification-service over Feign for audit logging.</p>
 */
@SpringBootApplication
@EnableFeignClients
public class UserServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(UserServiceApplication.class, args);
	}
}
