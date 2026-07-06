package com.courseacademy.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$", message = "Password must be at least 8 characters and include uppercase, lowercase, number and special character")
    private String password;

    @NotBlank(message = "Roll number is required")
    private String rollNumber;

    @Pattern(regexp = "[0-9]{10}", message = "Mobile number must be 10 digits")
    private String mobileNumber;

    @Pattern(regexp = "[0-9]{10}", message = "WhatsApp number must be 10 digits")
    private String whatsappNumber;

    @NotBlank(message = "College is required")
    private String college;

    @NotBlank(message = "Branch is required")
    private String branch;

    private String section;
    private String courseType;
    private String year;
    private String gender;
    private String address;
}
