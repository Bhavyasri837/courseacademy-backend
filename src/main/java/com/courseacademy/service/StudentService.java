package com.courseacademy.service;

import com.courseacademy.dto.*;
import com.courseacademy.entity.Student;
import com.courseacademy.exception.BadRequestException;
import com.courseacademy.exception.ResourceNotFoundException;
import com.courseacademy.repository.StudentRepository;
import com.courseacademy.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    private final LoginLockoutService loginLockoutService;
    private final StudentEmailOtpService studentEmailOtpService;



    @Transactional
    public AuthResponse register(RegisterRequest req) {


        if (studentRepository.existsByEmail(req.getEmail())) {
            throw new BadRequestException("Email already registered. Please login.");
        }
        if (req.getRollNumber() != null && studentRepository.existsByRollNumber(req.getRollNumber())) {
            throw new BadRequestException("Roll number already registered.");
        }

        // ==============================
        // FEATURE 1: OTP MUST BE VERIFIED BEFORE REGISTRATION
        // Backend must never trust frontend.
        // Consume the verified OTP for the exact email being registered.
        // ==============================
        var otpRow = studentEmailOtpService.getValidVerifiedOtpOrThrow(req.getEmail());

        Student student = Student.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .rollNumber(req.getRollNumber())
                .mobileNumber(req.getMobileNumber())
                .whatsappNumber(req.getWhatsappNumber())
                .college(req.getCollege())
                .branch(req.getBranch())
                .section(req.getSection())
                .courseType(req.getCourseType())
                .year(req.getYear())
                .gender(req.getGender())
                .address(req.getAddress())
                .build();

        // Only after OTP verification we mark the student verified and persist.
        student.setVerified(true);
        student = studentRepository.save(student);

        // Enforce single-use OTP by deleting it after successful registration.
        studentEmailOtpService.deleteOtpByEmail(req.getEmail());

        // Return user without a token; client should login afterwards.
        return AuthResponse.builder()
                .token(null)
                .user(AuthResponse.UserDto.builder()
                        .id(student.getId())
                        .name(student.getName())
                        .email(student.getEmail())
                        .role("STUDENT")
                        .rollNumber(student.getRollNumber())
                        .mobileNumber(student.getMobileNumber())
                        .whatsappNumber(student.getWhatsappNumber())
                        .college(student.getCollege())
                        .branch(student.getBranch())
                        .section(student.getSection())
                        .courseType(student.getCourseType())
                        .year(student.getYear())
                        .gender(student.getGender())
                        .address(student.getAddress())
                        .build())
                .build();


    }

    public AuthResponse login(LoginRequest req) {

        Student student = studentRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password."));

        String lockKey = "STUDENT:" + student.getEmail().trim().toLowerCase(java.util.Locale.ROOT);
        loginLockoutService.assertNotLocked(lockKey);

        if (!passwordEncoder.matches(req.getPassword(), student.getPassword())) {
            loginLockoutService.registerFailedAttempt(lockKey);
            throw new BadRequestException("Invalid email or password.");
        }

        if (!student.isVerified()) {
            throw new BadRequestException("Email not verified. Please verify OTP before logging in.");
        }

        loginLockoutService.registerSuccessfulLogin(lockKey);

        String token = jwtUtil.generateToken(student.getId(), student.getEmail(), "STUDENT");
        return buildAuthResponse(student, token);

    }


    @Transactional
    public ApiResponse resetPassword(ForgotPasswordRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank()) {
            throw new BadRequestException("Email is required.");
        }

        // OTP must be verified before allowing password change.
        // This is server-side enforcement; frontend cannot bypass it.
        studentEmailOtpService.getValidVerifiedOtpOrThrow(req.getEmail());

        Student student = null;

        if (req.getRollNumber() != null && !req.getRollNumber().isBlank()) {
            student = studentRepository
                    .findByEmailAndRollNumber(req.getEmail(), req.getRollNumber())
                    .orElse(null);
        }

        if (student == null && req.getMobileNumber() != null && !req.getMobileNumber().isBlank()) {
            student = studentRepository
                    .findByEmailAndMobileNumber(req.getEmail(), req.getMobileNumber())
                    .orElse(null);
        }

        if (student == null) {
            throw new BadRequestException("No matching student found. Check your email, roll number, or mobile number.");
        }

        if (req.getNewPassword() == null || req.getNewPassword().isBlank()) {
        throw new BadRequestException("New password is required.");
        }
        student.setPassword(passwordEncoder.encode(req.getNewPassword()));
        studentRepository.save(student);

        // Single-use OTP: remove after successful password reset
        studentEmailOtpService.deleteOtpByEmail(req.getEmail());

        return ApiResponse.ok("Password updated successfully. Please login with your new password.");
    }


    public ApiResponse sendOtp(SendOtpRequest req) {
        return studentEmailOtpService.sendOtp(req);
    }

    public ApiResponse verifyOtp(OtpRequest req) {
        return studentEmailOtpService.verifyOtp(req);
    }

    public Student getStudentByEmail(String email) {

        return studentRepository.findByEmail(email)

                .orElseThrow(() -> new ResourceNotFoundException("Student", "email", email));
    }

    private AuthResponse buildAuthResponse(Student student, String token) {
        return AuthResponse.builder()
                .token(token)
                .user(AuthResponse.UserDto.builder()
                        .id(student.getId())
                        .name(student.getName())
                        .email(student.getEmail())
                        .role("STUDENT")
                        .rollNumber(student.getRollNumber())
                        .mobileNumber(student.getMobileNumber())
                        .whatsappNumber(student.getWhatsappNumber())
                        .college(student.getCollege())
                        .branch(student.getBranch())
                        .section(student.getSection())
                        .courseType(student.getCourseType())
                        .year(student.getYear())
                        .gender(student.getGender())
                        .address(student.getAddress())
                        .build())
                .build();
    }
}
