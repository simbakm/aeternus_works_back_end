package com.example.aeternus_back_end.dto;

import lombok.Data;

@Data
public class CreateAdminUserRequest {
    private String username;
    private String email;
    private String role; // "ADMIN" or "SUPER_ADMIN"
}
