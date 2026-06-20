package com.example.aeternus_back_end.controller;

import com.example.aeternus_back_end.model.AdminUser;
import com.example.aeternus_back_end.repository.AdminUserRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUser>> getAllAdmins() {
        return ResponseEntity.ok(adminUserRepository.findAll());
    }

    @PostMapping("/users")
    public ResponseEntity<?> addAdmin(@RequestBody AddAdminRequest request) {
        if (adminUserRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists");
        }
        if (adminUserRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Email already exists");
        }

        AdminUser newUser = AdminUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role("admin")
                .build();

        adminUserRepository.save(newUser);
        return ResponseEntity.ok("Admin added successfully");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        AdminUser admin = adminUserRepository.findByUsername(request.getUsername())
                .orElse(null);
        if (admin == null) {
            return ResponseEntity.badRequest().body("User not found");
        }

        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        adminUserRepository.save(admin);
        
        return ResponseEntity.ok("Password reset successfully");
    }
}

@Data
class AddAdminRequest {
    private String username;
    private String email;
    private String password;
}

@Data
class ResetPasswordRequest {
    private String username;
    private String newPassword;
}
