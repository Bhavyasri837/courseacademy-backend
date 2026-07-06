package com.courseacademy.service;

import com.courseacademy.dto.ApiResponse;
import com.courseacademy.dto.OtpRequest;
import com.courseacademy.dto.SendOtpRequest;
import com.courseacademy.entity.Student;
import com.courseacademy.entity.StudentEmailOtp;
import com.courseacademy.exception.BadRequestException;
import com.courseacademy.repository.StudentEmailOtpRepository;
import com.courseacademy.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;


import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;


@Service
@RequiredArgsConstructor
public class StudentEmailOtpService {

    private final StudentRepository studentRepository;
    private final StudentEmailOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @org.springframework.beans.factory.annotation.Value("${app.mail.from:}")
    private String mailFrom;


    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    @Transactional
    public ApiResponse sendOtp(SendOtpRequest req) {
        String normalizedEmail = req.getEmail().trim().toLowerCase(Locale.ROOT);

        Student student = studentRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("No account found for this email."));

        int otp = generate6DigitOtp();
        String otpHash = passwordEncoder.encode(String.valueOf(otp));
        LocalDateTime expiresAt = LocalDateTime.now().plus(OTP_TTL);

        // Upsert OTP row keyed by email
        StudentEmailOtp existing = otpRepository.findByEmail(normalizedEmail).orElse(null);
        if (existing != null) {
            existing.setOtpHash(otpHash);
            existing.setExpiresAt(expiresAt);
            existing.setVerified(false);
            otpRepository.save(existing);
        } else {
            StudentEmailOtp row = StudentEmailOtp.builder()
                    .email(normalizedEmail)
                    .otpHash(otpHash)
                    .expiresAt(expiresAt)
                    .verified(false)
                    .build();
            otpRepository.save(row);
        }

        // Send OTP email (no OTP in response)
        sendOtpEmail(normalizedEmail, otp);

        return ApiResponse.ok("OTP sent to your email.");


    }

    @Transactional
    public ApiResponse verifyOtp(OtpRequest req) {
        String normalizedEmail = req.getEmail().trim().toLowerCase(Locale.ROOT);
        String otp = req.getOtp().trim();

        StudentEmailOtp row = otpRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("OTP not found. Please request a new one."));

        if (row.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired. Please request a new one.");
        }

        // Compare hashed OTP
        if (!passwordEncoder.matches(otp, row.getOtpHash())) {
            throw new BadRequestException("Invalid OTP. Please try again.");
        }

        // Mark OTP as verified; registration flow will consume it.
        row.setVerified(true);
        otpRepository.save(row);

        return ApiResponse.ok("OTP verified. You can now complete registration.");

    }

    public StudentEmailOtp getValidVerifiedOtpOrThrow(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        StudentEmailOtp row = otpRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("OTP not found. Please request a new one."));

        if (row.getExpiresAt() == null || row.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired. Please request a new one.");
        }

        if (!row.isVerified()) {
            throw new BadRequestException("OTP not verified. Please verify OTP before registering.");
        }

        return row;
    }

    @Transactional
    public void deleteOtpByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        otpRepository.findByEmail(normalizedEmail)
                .ifPresent(otpRepository::delete);
    }

    private void sendOtpEmail(String normalizedEmail, int otp) {
        if (mailFrom == null || mailFrom.isBlank()) {
            // Fail fast if misconfigured.
            throw new BadRequestException("Email sender (MAIL_FROM) is not configured.");
        }

        String subject = "AffordTech OTP Verification";
        String text = "Your OTP is: " + otp + "\nOTP expires in 5 minutes.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            helper.setFrom(mailFrom);
            helper.setTo(normalizedEmail);
            helper.setSubject(subject);
            helper.setText(text, false);

            mailSender.send(message);
        } catch (Exception e) {
            throw new BadRequestException("Failed to send OTP email.");
        }
    }

    private int generate6DigitOtp() {
        SecureRandom random = new SecureRandom();
        return 100000 + random.nextInt(900000);
    }

}


