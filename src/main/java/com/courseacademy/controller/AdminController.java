package com.courseacademy.controller;

import com.courseacademy.dto.ApiResponse;
import com.courseacademy.dto.AuthResponse;
import com.courseacademy.dto.LoginRequest;
import com.courseacademy.repository.EnrollmentRepository;
import com.courseacademy.repository.StudentRepository;
import com.courseacademy.repository.CourseRepository;
import com.courseacademy.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final EnrollmentRepository enrollmentRepository;

    /** POST /api/auth/admin/login */
    @PostMapping("/auth/admin/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(adminService.login(req));
    }

    /** GET /api/admin/dashboard — aggregate stats */
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getDashboard() {
        long totalStudents = studentRepository.count();
        long totalCourses  = courseRepository.count();
        long pending       = enrollmentRepository.countByStatus("PENDING");
        long approved      = enrollmentRepository.countByStatus("APPROVED");
        Double revenue     = enrollmentRepository.getTotalApprovedRevenue();

        return ResponseEntity.ok(
                adminService.getDashboardStats(totalStudents, totalCourses, pending, approved, revenue)
        );
    }
}
