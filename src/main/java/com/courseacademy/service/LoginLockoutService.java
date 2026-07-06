package com.courseacademy.service;

import com.courseacademy.dto.LoginAttemptRequest;
import com.courseacademy.entity.LoginLockout;
import com.courseacademy.exception.BadRequestException;
import com.courseacademy.repository.LoginLockoutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginLockoutService {

    private final LoginLockoutRepository lockoutRepository;

    // Tuning (as per requirements)
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(10);

    @Transactional
    public void registerFailedAttempt(String key) {
        LoginLockout state = lockoutRepository.findByKey(key).orElseGet(() ->
                LoginLockout.builder()
                        .key(key)
                        .failedAttempts(0)
                        .lockedUntil(null)
                        .build()
        );

        LocalDateTime now = LocalDateTime.now();
        if (state.getLockedUntil() != null && state.getLockedUntil().isAfter(now)) {
            // Already locked; just keep it locked.
            return;
        }

        int attempts = (state.getFailedAttempts() == null ? 0 : state.getFailedAttempts()) + 1;
        state.setFailedAttempts(attempts);

        if (attempts >= MAX_FAILED_ATTEMPTS) {
            state.setLockedUntil(now.plus(LOCK_DURATION));
        }

        lockoutRepository.save(state);
    }

    @Transactional
    public void registerSuccessfulLogin(String key) {
        lockoutRepository.findByKey(key).ifPresent(state -> {
            state.setFailedAttempts(0);
            state.setLockedUntil(null);
            lockoutRepository.save(state);
        });
    }

    public void assertNotLocked(String key) {
        LoginLockout state = lockoutRepository.findByKey(key).orElse(null);
        if (state == null || state.getLockedUntil() == null) return;

        LocalDateTime now = LocalDateTime.now();
        if (state.getLockedUntil().isAfter(now)) {
            long secondsLeft = Duration.between(now, state.getLockedUntil()).getSeconds();
            throw new BadRequestException("Too many failed login attempts. Try again in " + secondsLeft + " seconds.");
        }
    }
}

