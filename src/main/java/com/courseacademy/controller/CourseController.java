package com.courseacademy.controller;

import com.courseacademy.dto.ApiResponse;
import com.courseacademy.entity.Course;
import jakarta.validation.Valid;
import com.courseacademy.service.CourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /** GET /api/courses — public */
    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }

    /** GET /api/courses/{id} — public */
    @GetMapping("/{id}")
    public ResponseEntity<Course> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }

    /** POST /api/courses — admin only */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> createCourse(@Valid @RequestBody Course course) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(courseService.createCourse(course));
    }

    /** PUT /api/courses/{id} — admin only */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> updateCourse(@PathVariable Long id,
                                               @Valid @RequestBody Course course) {
        return ResponseEntity.ok(courseService.updateCourse(id, course));
    }

    /** DELETE /api/courses/{id} — admin only */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.ok(ApiResponse.ok("Course deleted successfully."));
    }


    /** PATCH /api/courses/{id}/status — admin only */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> updateStatus(@PathVariable Long id,
                                               @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(courseService.updateCourseStatus(id, status));
    }

    /** PATCH /api/courses/{id}/week-unlock — admin only */
    @PatchMapping("/{id}/week-unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> updateWeekUnlock(@PathVariable Long id,
                                                   @RequestBody Map<String, Object> body) {
        Integer week = Integer.valueOf(body.get("week").toString());
        Boolean unlocked = Boolean.valueOf(body.get("unlocked").toString());
        return ResponseEntity.ok(courseService.updateWeekUnlock(id, week, unlocked));
    }

    /** PATCH /api/courses/{id}/final-assessment-unlock — admin only */
    @PatchMapping("/{id}/final-assessment-unlock")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Course> updateFinalAssessmentUnlock(@PathVariable Long id,
                                                              @RequestBody Map<String, Boolean> body) {
        Boolean unlocked = body.get("unlocked");
        return ResponseEntity.ok(courseService.updateFinalAssessmentUnlock(id, unlocked));
    }
}
