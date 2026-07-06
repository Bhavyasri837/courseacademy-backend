package com.courseacademy.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "students")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @Column(name = "roll_number")
    private String rollNumber;

    @Column(name = "mobile_number")
    private String mobileNumber;

    @Column(name = "whatsapp_number")
    private String whatsappNumber;

    private String college;
    private String branch;
    private String section;

    @Column(name = "course_type")
    private String courseType;

    private String year;
    private String gender;
    private String address;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean verified = false;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
