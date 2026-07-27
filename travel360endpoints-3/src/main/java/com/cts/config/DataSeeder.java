package com.cts.config;

import java.time.LocalDateTime;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.cts.entity.User;
import com.cts.enums.Role;
import com.cts.enums.UserStatus;
import com.cts.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Seeds one user for every privileged (non-customer) role at startup.
 * Customers are never seeded — they self-register via the public endpoint.
 * Idempotent: existing users (matched by email) are skipped, so restarts are safe.
 */
@Component
@Order(1)
@AllArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    /** Shared default password assigned to every system-generated user. */
    public static final String DEFAULT_PASSWORD = "Welcome@123";

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    @Override
    public void run(String... args) {

        seed("Admin", "User", "admin@travel360.com", Role.ADMIN, 1000000001L);
        seed("Travel", "Agent", "agent@travel360.com", Role.TRAVEL_AGENT, 1000000002L);
        seed("Finance", "Officer", "finance@travel360.com", Role.FINANCE_OFFICER, 1000000003L);
        seed("Compliance", "Officer", "compliance@travel360.com", Role.COMPLIANCE_OFFICER, 1000000004L);

        // Seed Test Customers
        seed("John", "Customer", "customer1@travel360.com", Role.CUSTOMER, 9000000001L);
        seed("Jane", "Customer", "customer2@travel360.com", Role.CUSTOMER, 9000000002L);
    }

    private void seed(String firstName, String lastName,
                      String email, Role role, Long phoneNo) {

        if (repo.existsByEmail(email)) {
            log.info("Seed user already exists, skipping: {}", email);
            return;
        }

        User user = User.builder()
                .firstName(firstName)
                .lastName(lastName)
                .email(email)
                .password(encoder.encode(DEFAULT_PASSWORD))
                .role(role)
                .phoneNo(phoneNo)
                .status(UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        repo.save(user);

        log.info("Seeded {} user: {}", role, email);
    }
}