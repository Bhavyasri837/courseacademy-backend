package com.courseacademy.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EnrollmentRequest {

    @NotNull(message = "Course ID is required")
    private Long courseId;


    @NotBlank(message = "Student name is required")
    private String studentName;

    @NotBlank(message = "Student email is required")
    private String studentEmail;

    private String rollNumber;
    private String phone;
    private String whatsappNumber;
    private String college;
    private String courseType;
    private String year;
    private String branch;
    private String section;
    private String address;

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    // Razorpay payment verification fields (Feature 2)
    // Backward compatible: may be null for legacy clients.
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

    @NotBlank(message = "Payment screenshot link is required")
    private String paymentScreenshotLink;

    private String paymentDate;
}

