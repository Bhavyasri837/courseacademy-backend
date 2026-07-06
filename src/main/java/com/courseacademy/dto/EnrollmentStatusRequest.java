package com.courseacademy.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EnrollmentStatusRequest {

    @NotBlank(message = "Status is required")
    private String status;
}
