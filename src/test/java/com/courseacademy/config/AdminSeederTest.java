package com.courseacademy.config;

import com.courseacademy.entity.Admin;
import com.courseacademy.repository.AdminRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSeederTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminSeeder adminSeeder;

    @Test
    void shouldCreateAdminIfNotExists() {

        ReflectionTestUtils.setField(adminSeeder, "adminName", "System Admin");
        ReflectionTestUtils.setField(adminSeeder, "adminEmail", "admin@example.com");
        ReflectionTestUtils.setField(adminSeeder, "adminPassword", "StrongPass123");

        when(adminRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("StrongPass123"))
                .thenReturn("encodedPassword");

        adminSeeder.createAdminIfNeeded();

        verify(adminRepository).save(any(Admin.class));
    }

    @Test
    void shouldNotCreateAdminIfAlreadyExists() {

        ReflectionTestUtils.setField(adminSeeder, "adminName", "System Admin");
        ReflectionTestUtils.setField(adminSeeder, "adminEmail", "admin@example.com");
        ReflectionTestUtils.setField(adminSeeder, "adminPassword", "StrongPass123");

        when(adminRepository.findByEmail("admin@example.com"))
                .thenReturn(Optional.of(new Admin()));

        adminSeeder.createAdminIfNeeded();

        verify(adminRepository, never()).save(any());
    }

    @Test
    void shouldSkipWhenEmailMissing() {

        ReflectionTestUtils.setField(adminSeeder, "adminName", "System Admin");
        ReflectionTestUtils.setField(adminSeeder, "adminEmail", "");
        ReflectionTestUtils.setField(adminSeeder, "adminPassword", "");

        adminSeeder.createAdminIfNeeded();

        verify(adminRepository, never()).save(any());
    }
}