package com.courseacademy.service;

import com.courseacademy.dto.ApiResponse;
import com.courseacademy.dto.AuthResponse;
import com.courseacademy.dto.LoginRequest;
import com.courseacademy.entity.Admin;
import com.courseacademy.exception.BadRequestException;
import com.courseacademy.exception.ResourceNotFoundException;
import com.courseacademy.repository.AdminRepository;
import com.courseacademy.security.JwtUtil;

import lombok.RequiredArgsConstructor;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final LoginLockoutService loginLockoutService;
    private final AdminAuditLogService adminAuditLogService;




    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {
        String normalizedEmail = req.getEmail().trim().toLowerCase(Locale.ROOT);
        String lockKey = "ADMIN:" + normalizedEmail;
        loginLockoutService.assertNotLocked(lockKey);

        Admin admin = adminRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("Invalid admin credentials."));

        if (!admin.isEnabled() || !passwordEncoder.matches(req.getPassword(), admin.getPassword())) {
            loginLockoutService.registerFailedAttempt(lockKey);
            throw new BadRequestException("Invalid admin credentials.");
        }

        loginLockoutService.registerSuccessfulLogin(lockKey);

        String token = jwtUtil.generateToken(admin.getId(), admin.getEmail(), "ADMIN");

        // Audit entry for successful login
        adminAuditLogService.log(
                admin.getName(),
                "ADMIN_LOGIN",
                "ADMIN",
                String.valueOf(admin.getId()),
                null
        );

        return AuthResponse.builder()

                .token(token)
                .user(AuthResponse.UserDto.builder()
                        .id(admin.getId())
                        .name(admin.getName())
                        .email(admin.getEmail())
                        .role("ADMIN")
                        .build())
                .build();
    }


    @Transactional
    public Admin createAdmin(String name, String email, String password) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (adminRepository.existsByEmail(normalizedEmail)) {
            throw new BadRequestException("An admin with this email already exists.");
        }

        Admin admin = Admin.builder()
                .name(name)
                .email(normalizedEmail)
                .password(passwordEncoder.encode(password))
                .enabled(true)
                .build();

        return adminRepository.save(admin);
    }

    @Transactional
    public Admin updateAdminPassword(Long adminId, String newPassword) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminId));
        admin.setPassword(passwordEncoder.encode(newPassword));
        return adminRepository.save(admin);
    }

    @Transactional
    public Admin setAdminStatus(Long adminId, boolean enabled) {
        Admin admin = adminRepository.findById(adminId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin", adminId));
        admin.setEnabled(enabled);
        return adminRepository.save(admin);
    }

    public Admin getAdminByEmail(String email) {
        return adminRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("Admin", "email", email));
    }

    public ApiResponse getDashboardStats(
            long totalStudents,
            long totalCourses,
            long pendingEnrollments,
            long approvedEnrollments,
            Double totalRevenue
    ) {
        var stats = java.util.Map.of(
                "totalStudents", totalStudents,
                "totalCourses", totalCourses,
                "pendingEnrollments", pendingEnrollments,
                "approvedEnrollments", approvedEnrollments,
                "totalRevenue", totalRevenue != null ? totalRevenue : 0.0
        );
        return ApiResponse.ok("Dashboard stats fetched", stats);
    }
}
