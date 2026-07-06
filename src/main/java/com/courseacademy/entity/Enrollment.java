package com.courseacademy.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "student_name")
    private String studentName;

    @Column(name = "student_email")
    private String studentEmail;

    @Column(name = "roll_number")
    private String rollNumber;

    private String phone;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    private String college;

    @Column(name = "course_type")
    private String courseType;

    private String year;
    private String branch;
    private String section;
    private String address;

    private Double amount;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "payment_screenshot_link")
    private String paymentScreenshotLink;

    @Column(name = "payment_date")
    private String paymentDate;

    @Column(name = "payment_status")
    @Builder.Default
    private String paymentStatus = "SUBMITTED";

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";

    @Column(name = "enrolled_at", updatable = false)
    private LocalDateTime enrolledAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.enrolledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
