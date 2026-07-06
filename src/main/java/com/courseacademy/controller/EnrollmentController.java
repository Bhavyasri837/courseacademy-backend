package com.courseacademy.controller;

import com.courseacademy.dto.ApiResponse;
import com.courseacademy.dto.EnrollmentRequest;
import com.courseacademy.dto.EnrollmentStatusRequest;
import com.courseacademy.entity.Course;
import com.courseacademy.entity.Enrollment;
import com.courseacademy.entity.Student;
import com.courseacademy.service.CourseService;
import com.courseacademy.service.EnrollmentService;
import com.courseacademy.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final StudentService studentService;
    private final CourseService courseService;

    /** POST /api/enrollments — student submits enrollment */
    @PostMapping
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Enrollment> enroll(@Valid @RequestBody EnrollmentRequest req,
                                             Principal principal) {
        Student student = studentService.getStudentByEmail(principal.getName());
        Course course   = courseService.getCourseById(req.getCourseId());
        Enrollment enrollment = enrollmentService.enroll(student, course, req);
        return ResponseEntity.status(HttpStatus.CREATED).body(enrollment);
    }

    /** GET /api/enrollments/my — student's own enrollments */
    @GetMapping("/my")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<List<Enrollment>> getMyEnrollments(Principal principal) {
        Student student = studentService.getStudentByEmail(principal.getName());
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(student));
    }

    /** GET /api/enrollments/all — admin: all enrollments */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Enrollment>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    /** GET /api/enrollments/course/{courseId} — admin: enrollments for a specific course */
    @GetMapping("/course/{courseId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Enrollment>> getByCourse(@PathVariable Long courseId) {
        Course course = courseService.getCourseById(courseId);
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByCourse(course));
    }

    /** PUT /api/enrollments/{id}/status — admin: approve or reject */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Enrollment> updateStatus(@PathVariable Long id,
                                                   @Valid @RequestBody EnrollmentStatusRequest req) {
        return ResponseEntity.ok(enrollmentService.updateStatus(id, req.getStatus()));
    }

    /** GET /api/enrollments/revenue — admin: revenue summary */
    @GetMapping("/revenue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> getRevenueSummary() {
        Map<String, Object> summary = enrollmentService.getRevenueSummary();
        return ResponseEntity.ok(ApiResponse.ok("Revenue summary fetched", summary));
    }
}
