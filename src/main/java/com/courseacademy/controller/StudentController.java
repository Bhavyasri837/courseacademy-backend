package com.courseacademy.controller;

import com.courseacademy.dto.*;
import com.courseacademy.entity.Student;
import com.courseacademy.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;

    /** POST /api/auth/student/register */
    @PostMapping("/auth/student/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(studentService.register(req));
    }

    /** POST /api/auth/student/login */
    @PostMapping("/auth/student/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(studentService.login(req));
    }

    /** POST /api/auth/student/send-otp */
    @PostMapping("/auth/student/send-otp")
    public ResponseEntity<com.courseacademy.dto.ApiResponse> sendOtp(
            @Valid @RequestBody SendOtpRequest req
    ) {
        return ResponseEntity.ok(studentService.sendOtp(req));
    }

    /** POST /api/auth/student/verify-otp */
    @PostMapping("/auth/student/verify-otp")
    public ResponseEntity<com.courseacademy.dto.ApiResponse> verifyOtp(
            @Valid @RequestBody OtpRequest req
    ) {
        return ResponseEntity.ok(studentService.verifyOtp(req));
    }


    /** POST /api/auth/student/forgot-password */
    @PostMapping("/auth/student/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        return ResponseEntity.ok(studentService.resetPassword(req));
    }

    /** GET /api/students/profile  (student's own profile) */
    @GetMapping("/students/profile")
    public ResponseEntity<Student> getProfile(Principal principal) {
        Student student = studentService.getStudentByEmail(principal.getName());
        student.setPassword(null); // never expose password
        return ResponseEntity.ok(student);
    }
}
