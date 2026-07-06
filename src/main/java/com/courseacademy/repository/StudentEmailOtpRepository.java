package com.courseacademy.repository;

import com.courseacademy.entity.StudentEmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentEmailOtpRepository extends JpaRepository<StudentEmailOtp, Long> {
    Optional<StudentEmailOtp> findByEmail(String email);
}

