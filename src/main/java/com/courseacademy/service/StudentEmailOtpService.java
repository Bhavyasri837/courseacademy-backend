package com.courseacademy.service;

import com.courseacademy.dto.ApiResponse;
import com.courseacademy.dto.OtpRequest;
import com.courseacademy.dto.SendOtpRequest;
import com.courseacademy.entity.Student;
import com.courseacademy.entity.StudentEmailOtp;
import com.courseacademy.exception.BadRequestException;
import com.courseacademy.repository.StudentEmailOtpRepository;
import com.courseacademy.repository.StudentRepository;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Value("${app.mail.from:}")
    private String mailFrom;

    @Value("${spring.mail.username:}")
    private String smtpUsername;

    private static final Duration OTP_TTL = Duration.ofMinutes(5);

    @Transactional
    public ApiResponse sendOtp(SendOtpRequest req) {

        String normalizedEmail = req.getEmail().trim().toLowerCase(Locale.ROOT);

        Student student = studentRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadRequestException("No account found for this email."));

        int otp = generate6DigitOtp();

        String otpHash = passwordEncoder.encode(String.valueOf(otp));

        LocalDateTime expiresAt = LocalDateTime.now().plus(OTP_TTL);

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

        sendOtpEmail(normalizedEmail, otp);

        return ApiResponse.ok("OTP sent to your email.");
    }

    @Transactional
    public ApiResponse verifyOtp(OtpRequest req) {

        String normalizedEmail = req.getEmail().trim().toLowerCase(Locale.ROOT);

        StudentEmailOtp row = otpRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new BadRequestException("OTP not found. Please request a new one."));

        if (row.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired. Please request a new one.");
        }

        if (!passwordEncoder.matches(req.getOtp(), row.getOtpHash())) {
            throw new BadRequestException("Invalid OTP.");
        }

        row.setVerified(true);

        otpRepository.save(row);

        return ApiResponse.ok("OTP verified successfully.");
    }

    public StudentEmailOtp getValidVerifiedOtpOrThrow(String email) {

        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        StudentEmailOtp row = otpRepository.findByEmail(normalizedEmail)
                .orElseThrow(() ->
                        new BadRequestException("OTP not found."));

        if (row.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP expired.");
        }

        if (!row.isVerified()) {
            throw new BadRequestException("OTP not verified.");
        }

        return row;
    }

    @Transactional
    public void deleteOtpByEmail(String email) {

        otpRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .ifPresent(otpRepository::delete);
    }

    private void sendOtpEmail(String email, int otp) {

        if (mailFrom == null || mailFrom.isBlank()) {
            throw new BadRequestException("MAIL_FROM is not configured.");
        }

        System.out.println("\n================ EMAIL DEBUG ================");
        System.out.println("SMTP USERNAME : " + smtpUsername);
        System.out.println("MAIL FROM     : " + mailFrom);
        System.out.println("SEND TO       : " + email);
        System.out.println("OTP           : " + otp);
        System.out.println("=============================================\n");

        try {

            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(mailFrom);

            helper.setTo(email);

            helper.setSubject("AffordTech Email Verification");

            String html = """
                    <div style="font-family:Arial;padding:25px">
                    
                        <h2 style="color:#4F46E5;">
                            AffordTech Email Verification
                        </h2>

                        <p>Hello,</p>

                        <p>Your verification OTP is:</p>

                        <div style="
                            font-size:34px;
                            font-weight:bold;
                            color:#2563EB;
                            letter-spacing:8px;
                            margin:20px 0;">
                            %s
                        </div>

                        <p>
                            This OTP will expire in
                            <b>5 minutes</b>.
                        </p>

                        <hr>

                        <p style="color:gray">
                            If you didn't request this,
                            simply ignore this email.
                        </p>

                    </div>
                    """.formatted(otp);

            helper.setText(html, true);

            mailSender.send(message);

            System.out.println("EMAIL SENT SUCCESSFULLY");

        } catch (Exception e) {

            System.out.println("\n========== EMAIL ERROR ==========");
            e.printStackTrace();
            System.out.println("=================================\n");

            throw new BadRequestException(
                    "Failed to send OTP email : " + e.getMessage()
            );
        }
    }

    private int generate6DigitOtp() {
        return 100000 + new SecureRandom().nextInt(900000);
    }

}