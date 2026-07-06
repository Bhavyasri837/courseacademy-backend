package com.courseacademy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserDto user;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDto {
        private Long id;
        private String name;
        private String email;
        private String role;
        private String rollNumber;
        private String mobileNumber;
        private String whatsappNumber;
        private String college;
        private String branch;
        private String section;
        private String courseType;
        private String year;
        private String gender;
        private String address;
    }
}
