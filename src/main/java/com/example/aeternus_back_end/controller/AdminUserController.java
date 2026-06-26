package com.example.aeternus_back_end.controller;

import com.example.aeternus_back_end.dto.AdminUserDto;
import com.example.aeternus_back_end.dto.CreateAdminUserRequest;
import com.example.aeternus_back_end.dto.UpdateAdminUserRequest;
import com.example.aeternus_back_end.model.AdminUser;
import com.example.aeternus_back_end.repository.AdminUserRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin-users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromAddress;

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";

    // GET /api/admin-users — accessible by all authenticated admins (read-only for ADMIN, full access for SUPER_ADMIN)
    @GetMapping
    public ResponseEntity<List<AdminUserDto>> getAllAdmins() {
        List<AdminUserDto> admins = adminUserRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(admins);
    }

    // GET /api/admin-users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> getAdmin(@PathVariable String id) {
        return adminUserRepository.findById(id)
                .map(this::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/admin-users — SUPER_ADMIN only (enforced in SecurityConfig)
    @PostMapping
    public ResponseEntity<?> createAdmin(@RequestBody CreateAdminUserRequest request) {
        // Validate uniqueness
        if (adminUserRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }
        if (adminUserRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        // Validate role
        String role = request.getRole();
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role. Must be ADMIN or SUPER_ADMIN"));
        }

        // Generate a random password
        String rawPassword = generatePassword(12);

        AdminUser newAdmin = AdminUser.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(role)
                .build();

        adminUserRepository.save(newAdmin);

        // Return the credentials so they can be copied
        Map<String, String> credentials = Map.of(
                "username", request.getUsername(),
                "email", request.getEmail(),
                "password", rawPassword,
                "role", role
        );

        return ResponseEntity.ok(credentials);
    }

    // POST /api/admin-users/{id}/send-credentials — SUPER_ADMIN only
    @PostMapping("/{id}/send-credentials")
    public ResponseEntity<?> sendCredentials(@PathVariable String id, @RequestBody Map<String, String> body) {
        AdminUser admin = adminUserRepository.findById(id).orElse(null);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }

        String rawPassword = body.get("password");
        if (rawPassword == null || rawPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Password is required to send credentials"));
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(admin.getEmail());
            helper.setSubject("Your Aeternus Works Admin Credentials");
            String html = "<div style='font-family:Arial,sans-serif;max-width:600px;margin:0 auto;'>"
                    + "<h2 style='color:#f97316;'>Welcome to Aeternus Works Admin</h2>"
                    + "<p>Your admin account has been created. Here are your login credentials:</p>"
                    + "<table style='border-collapse:collapse;width:100%;'>"
                    + "<tr><td style='padding:8px;border:1px solid #ddd;font-weight:bold;'>Username</td>"
                    + "<td style='padding:8px;border:1px solid #ddd;'>" + admin.getUsername() + "</td></tr>"
                    + "<tr><td style='padding:8px;border:1px solid #ddd;font-weight:bold;'>Email</td>"
                    + "<td style='padding:8px;border:1px solid #ddd;'>" + admin.getEmail() + "</td></tr>"
                    + "<tr><td style='padding:8px;border:1px solid #ddd;font-weight:bold;'>Password</td>"
                    + "<td style='padding:8px;border:1px solid #ddd;'><code>" + rawPassword + "</code></td></tr>"
                    + "<tr><td style='padding:8px;border:1px solid #ddd;font-weight:bold;'>Role</td>"
                    + "<td style='padding:8px;border:1px solid #ddd;'>" + admin.getRole() + "</td></tr>"
                    + "</table>"
                    + "<p style='color:#666;margin-top:20px;'>Please log in and change your password immediately.</p>"
                    + "<p style='color:#999;font-size:12px;'>This is an automated message from Aeternus Works.</p>"
                    + "</div>";
            helper.setText(html, true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send email: " + e.getMessage()));
        }

        return ResponseEntity.ok(Map.of("message", "Credentials sent to " + admin.getEmail()));
    }

    // PUT /api/admin-users/{id} — SUPER_ADMIN only
    @PutMapping("/{id}")
    public ResponseEntity<?> updateAdmin(@PathVariable String id, @RequestBody UpdateAdminUserRequest request) {
        AdminUser admin = adminUserRepository.findById(id).orElse(null);
        if (admin == null) {
            return ResponseEntity.notFound().build();
        }

        // Check for conflicts (skip current user)
        if (request.getUsername() != null && !request.getUsername().equals(admin.getUsername())) {
            if (adminUserRepository.findByUsername(request.getUsername()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username already taken"));
            }
            admin.setUsername(request.getUsername());
        }

        if (request.getEmail() != null && !request.getEmail().equals(admin.getEmail())) {
            if (adminUserRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email already taken"));
            }
            admin.setEmail(request.getEmail());
        }

        if (request.getRole() != null) {
            if (!"ADMIN".equals(request.getRole()) && !"SUPER_ADMIN".equals(request.getRole())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
            }
            admin.setRole(request.getRole());
        }

        adminUserRepository.save(admin);
        return ResponseEntity.ok(toDto(admin));
    }

    // DELETE /api/admin-users/{id} — SUPER_ADMIN only
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteAdmin(@PathVariable String id) {
        if (!adminUserRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        adminUserRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "Admin user deleted"));
    }

    private AdminUserDto toDto(AdminUser admin) {
        return AdminUserDto.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .email(admin.getEmail())
                .role(admin.getRole())
                .createdAt(admin.getCreatedAt())
                .lastLogin(admin.getLastLogin())
                .build();
    }

    private String generatePassword(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
        }
        return sb.toString();
    }
}
