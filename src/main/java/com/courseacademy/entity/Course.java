package com.courseacademy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(name = "start_date")
    private String startDate;

    @Column(name = "end_date")
    private String endDate;

    private Double money;

    @Column(nullable = false)
    @Builder.Default
    private String status = "COMING_SOON";

    @Column(name = "google_form_link")
    private String googleFormLink;

    @Column(columnDefinition = "TEXT")
    private String syllabus;

    @Column(name = "route_map", columnDefinition = "TEXT")
    private String routeMap;

    @Column(name = "duration_days")
    @Builder.Default
    private Integer durationDays = 30;

    @Column(name = "content_days", columnDefinition = "LONGTEXT")
    private String contentDays;

    @Column(name = "assessment", columnDefinition = "LONGTEXT")
    private String assessment;

    @Column(name = "week_assignment_unlocks", columnDefinition = "TEXT")
    private String weekAssignmentUnlocks;

    @Column(name = "final_assessment_unlocked")
    @Builder.Default
    private Boolean finalAssessmentUnlocked = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
