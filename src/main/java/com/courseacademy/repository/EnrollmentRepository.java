package com.courseacademy.repository;

import com.courseacademy.entity.Enrollment;
import com.courseacademy.entity.Student;
import com.courseacademy.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    List<Enrollment> findByStudent(Student student);
    List<Enrollment> findByCourse(Course course);
    List<Enrollment> findAllByOrderByEnrolledAtDesc();
    List<Enrollment> findByStatus(String status);

    Optional<Enrollment> findByStudentAndCourse(Student student, Course course);

    boolean existsByStudentAndCourse(Student student, Course course);

    @Query("SELECT e FROM Enrollment e WHERE e.studentEmail = :email")
    List<Enrollment> findByStudentEmail(String email);

    @Query("SELECT e FROM Enrollment e WHERE e.student = :student AND e.course = :course AND e.status = :status")
    Optional<Enrollment> findByStudentAndCourseAndStatus(Student student, Course course, String status);

    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.status = :status")
    long countByStatus(String status);

    @Query("SELECT SUM(e.amount) FROM Enrollment e WHERE e.status = 'APPROVED'")
    Double getTotalApprovedRevenue();
}
