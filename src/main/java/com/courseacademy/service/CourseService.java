package com.courseacademy.service;
import com.courseacademy.entity.Course;
import com.courseacademy.exception.BadRequestException;
import com.courseacademy.exception.ResourceNotFoundException;
import com.courseacademy.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    // Soft-delete actions are logged at admin layer (not wired here yet)


    public List<Course> getAllCourses() {
        // Soft delete: hide DELETED courses from public
        return courseRepository.findAllByOrderByCreatedAtDesc().stream()
                .filter(c -> c.getStatus() == null || !"DELETED".equalsIgnoreCase(c.getStatus()))
                .toList();
    }



    public Course getCourseById(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course", id));

        if ("DELETED".equalsIgnoreCase(course.getStatus())) {
            throw new ResourceNotFoundException("Course", id);
        }
        return course;
    }


    @Transactional
    public Course createCourse(Course course) {
        if (course.getName() == null || course.getName().isBlank()) {
            throw new BadRequestException("Course name is required.");
        }
        // Build empty content days JSON if not provided
        if (course.getContentDays() == null || course.getContentDays().isBlank()) {
            course.setContentDays(buildEmptyContentDays(course.getDurationDays()));
        }
        if (course.getWeekAssignmentUnlocks() == null || course.getWeekAssignmentUnlocks().isBlank()) {
            course.setWeekAssignmentUnlocks("{}");
        }
        if (course.getAssessment() == null || course.getAssessment().isBlank()) {
            course.setAssessment("{\"title\":\"" + course.getName() + " Final Assessment\",\"questions\":[]}");
        }
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateCourse(Long id, Course updated) {
        Course existing = getCourseById(id);
        existing.setName(updated.getName() != null ? updated.getName() : existing.getName());
        existing.setDetails(updated.getDetails() != null ? updated.getDetails() : existing.getDetails());
        existing.setStartDate(updated.getStartDate() != null ? updated.getStartDate() : existing.getStartDate());
        existing.setEndDate(updated.getEndDate() != null ? updated.getEndDate() : existing.getEndDate());
        existing.setMoney(updated.getMoney() != null ? updated.getMoney() : existing.getMoney());
        existing.setStatus(updated.getStatus() != null ? updated.getStatus() : existing.getStatus());
        existing.setGoogleFormLink(updated.getGoogleFormLink() != null ? updated.getGoogleFormLink() : existing.getGoogleFormLink());
        existing.setSyllabus(updated.getSyllabus() != null ? updated.getSyllabus() : existing.getSyllabus());
        existing.setRouteMap(updated.getRouteMap() != null ? updated.getRouteMap() : existing.getRouteMap());
        existing.setDurationDays(updated.getDurationDays() != null ? updated.getDurationDays() : existing.getDurationDays());
        existing.setContentDays(updated.getContentDays() != null ? updated.getContentDays() : existing.getContentDays());
        existing.setAssessment(updated.getAssessment() != null ? updated.getAssessment() : existing.getAssessment());
        existing.setWeekAssignmentUnlocks(updated.getWeekAssignmentUnlocks() != null ? updated.getWeekAssignmentUnlocks() : existing.getWeekAssignmentUnlocks());
        existing.setFinalAssessmentUnlocked(updated.getFinalAssessmentUnlocked() != null ? updated.getFinalAssessmentUnlocked() : existing.getFinalAssessmentUnlocked());
        return courseRepository.save(existing);
    }

    @Transactional
    public void deleteCourse(Long id) {
        Course course = getCourseById(id);
        // Soft delete
        course.setStatus("DELETED");
        courseRepository.save(course);
    }



    @Transactional
    public Course updateCourseStatus(Long id, String status) {
        Course course = getCourseById(id);
        List<String> validStatuses = List.of("COMING_SOON", "ENROLLMENTS_OPEN", "STARTED", "COMPLETED");
        if (!validStatuses.contains(status)) {
            throw new BadRequestException("Invalid status. Must be one of: " + validStatuses);
        }
        course.setStatus(status);
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateWeekUnlock(Long id, Integer week, Boolean unlocked) {
        Course course = getCourseById(id);
        // Parse existing JSON, update, save back
        String existing = course.getWeekAssignmentUnlocks();
        if (existing == null || existing.isBlank()) existing = "{}";
        // Simple string manipulation (avoids extra Jackson dependency in service)
        String trimmed = existing.trim();
        String key = "\"" + week + "\"";
        String value = unlocked.toString();
        if (trimmed.contains(key)) {
            trimmed = trimmed.replaceAll(key + "\\s*:\\s*(true|false)", key + ":" + value);
        } else {
            trimmed = trimmed.substring(0, trimmed.length() - 1) +
                    (trimmed.length() > 2 ? "," : "") + key + ":" + value + "}";
        }
        course.setWeekAssignmentUnlocks(trimmed);
        return courseRepository.save(course);
    }

    @Transactional
    public Course updateFinalAssessmentUnlock(Long id, Boolean unlocked) {
        Course course = getCourseById(id);
        course.setFinalAssessmentUnlocked(unlocked);
        return courseRepository.save(course);
    }

    private String buildEmptyContentDays(Integer durationDays) {
        int days = durationDays != null ? durationDays : 30;
        StringBuilder sb = new StringBuilder("[");
        for (int i = 1; i <= days; i++) {
            if (i > 1) sb.append(",");
            sb.append("{\"day\":").append(i)
              .append(",\"recordingTitle\":\"\",\"recordingLink\":\"\"")
              .append(",\"materialTitle\":\"\",\"materialLink\":\"\"")
              .append(",\"assignmentTitle\":\"\",\"assignmentLink\":\"\",\"assignmentDueDate\":\"\"}");
        }
        sb.append("]");
        return sb.toString();
    }
}
