package com.courseacademy.service;

import com.courseacademy.dto.EnrollmentRequest;
import com.courseacademy.entity.Course;
import com.courseacademy.entity.Enrollment;
import com.courseacademy.entity.Student;
import com.courseacademy.exception.BadRequestException;
import com.courseacademy.exception.ResourceNotFoundException;
import com.courseacademy.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final RazorpayPaymentVerifier razorpayPaymentVerifier;

    @Transactional
    public Enrollment enroll(Student student, Course course, EnrollmentRequest req) {

        // Prevent duplicate enrollment
        Optional<Enrollment> existing = enrollmentRepository.findByStudentAndCourse(student, course);
        if (existing.isPresent()) {
            String status = existing.get().getStatus();
            if ("PENDING".equals(status)) {
                throw new IllegalStateException("Your enrollment is already submitted and pending approval.");
            }
            if ("APPROVED".equals(status)) {
                throw new IllegalStateException("You are already enrolled and approved for this course.");
            }
            // REJECTED — allow re-enrollment
        }

        // ==============================
        // Feature 2: Payment verification before enrollment
        // Backend must never trust frontend.
        // If signature fields are missing/invalid -> reject with HTTP 400.
        // ==============================
        if (req.getRazorpayOrderId() == null || req.getRazorpayPaymentId() == null || req.getRazorpaySignature() == null) {
            throw new BadRequestException("Payment verification failed.");
        }
        // Verify signature using Razorpay standard HMAC algorithm.
        razorpayPaymentVerifier.verifyOrThrow(
                req.getRazorpayOrderId(),
                req.getRazorpayPaymentId(),
                req.getRazorpaySignature()
        );

        Enrollment enrollment = Enrollment.builder()
                .student(student)
                .course(course)
                .studentName(req.getStudentName())
                .studentEmail(req.getStudentEmail())
                .rollNumber(req.getRollNumber())
                .phone(req.getPhone())
                .whatsappNumber(req.getWhatsappNumber())
                .college(req.getCollege())
                .courseType(req.getCourseType())
                .year(req.getYear())
                .branch(req.getBranch())
                .section(req.getSection())
                .address(req.getAddress())
                .amount(course.getMoney())
                // Store payment identifiers (minimal DB impact: reuse transactionId field)
                .transactionId(req.getRazorpayPaymentId() != null ? req.getRazorpayPaymentId() : req.getTransactionId())
                .paymentScreenshotLink(req.getPaymentScreenshotLink())
                .paymentDate(req.getPaymentDate() != null ? req.getPaymentDate() : LocalDate.now().toString())
                .paymentStatus("VERIFIED")
                .status("PENDING")
                .build();

        return enrollmentRepository.save(enrollment);

    }

    public List<Enrollment> getMyEnrollments(Student student) {
        return enrollmentRepository.findByStudent(student);
    }

    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAllByOrderByEnrolledAtDesc();
    }

    public List<Enrollment> getEnrollmentsByCourse(Course course) {
        return enrollmentRepository.findByCourse(course);
    }

    @Transactional
    public Enrollment updateStatus(Long enrollmentId, String status) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", enrollmentId));

        List<String> validStatuses = List.of("PENDING", "APPROVED", "REJECTED");
        if (!validStatuses.contains(status)) {
            throw new BadRequestException("Invalid status. Must be one of: " + validStatuses);
        }

        enrollment.setStatus(status);
        if ("APPROVED".equals(status)) {
            enrollment.setPaymentStatus("VERIFIED");
        }
        return enrollmentRepository.save(enrollment);
    }

    public boolean isStudentApprovedForCourse(Student student, Course course) {
        return enrollmentRepository
                .findByStudentAndCourseAndStatus(student, course, "APPROVED")
                .isPresent();
    }

    public Map<String, Object> getRevenueSummary() {
        List<Enrollment> all = enrollmentRepository.findAll();
        List<Enrollment> approved = all.stream()
                .filter(e -> "APPROVED".equals(e.getStatus()))
                .collect(Collectors.toList());

        double totalRevenue = approved.stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0)
                .sum();

        Map<String, Double> byCourse = approved.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getCourse().getName(),
                        Collectors.summingDouble(e -> e.getAmount() != null ? e.getAmount() : 0)
                ));

        return Map.of(
                "totalRevenue", totalRevenue,
                "approvedCount", (long) approved.size(),
                "pendingCount", all.stream().filter(e -> "PENDING".equals(e.getStatus())).count(),
                "rejectedCount", all.stream().filter(e -> "REJECTED".equals(e.getStatus())).count(),
                "byCourse", byCourse
        );
    }
}
