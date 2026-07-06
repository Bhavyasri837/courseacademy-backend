package com.courseacademy.security;

import com.courseacademy.service.LoginLockoutService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Lightweight optional filter placeholder.
 * Current implementation enforces lockout inside AdminService.login.
 */
@Component
@RequiredArgsConstructor
public class AdminLoginLockoutFilter extends OncePerRequestFilter {

    private final LoginLockoutService loginLockoutService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }
}

