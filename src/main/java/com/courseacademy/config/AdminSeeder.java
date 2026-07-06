package com.courseacademy.config;

import com.courseacademy.entity.Admin;
import com.courseacademy.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Initializes the database with a default admin account if none exists.
 *
 * Admin account creation is idempotent:
 * - If an admin with the configured email already exists, it will NOT be modified (password never overwritten).
 * - If config values are missing, bootstrap is skipped.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final String DEFAULT_ADMIN_NAME = "System Admin";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.name:}")
    private String adminName;

    @Value("${admin.email:}")
    private String adminEmail;

    @Value("${admin.password:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        createAdminIfNeeded();
    }

    void createAdminIfNeeded() {
        if (adminEmail == null || adminPassword == null || adminEmail.isBlank() || adminPassword.isBlank()) {
            return;
        }

        String normalizedEmail = adminEmail.trim().toLowerCase(Locale.ROOT);

        // Only create on first run.
        boolean exists = adminRepository.findByEmail(normalizedEmail).isPresent();
        if (exists) {
            return;
        }

        Admin admin = Admin.builder()
                .name(adminName == null || adminName.isBlank() ? DEFAULT_ADMIN_NAME : adminName)
                .email(normalizedEmail)
                .password(passwordEncoder.encode(adminPassword)) // BCrypt
                .enabled(true)
                .build();

        adminRepository.save(admin);
    }
}

