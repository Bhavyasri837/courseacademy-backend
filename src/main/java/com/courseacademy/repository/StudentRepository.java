package com.courseacademy.repository;

import com.courseacademy.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByRollNumber(String rollNumber);
    Optional<Student> findByEmailAndRollNumber(String email, String rollNumber);
    Optional<Student> findByEmailAndMobileNumber(String email, String mobileNumber);

    Optional<Student> findByEmailAndVerifiedTrue(String email);
}
