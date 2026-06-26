package com.example.aeternus_back_end.controller;

import com.example.aeternus_back_end.dto.AuthRequest;
import com.example.aeternus_back_end.dto.AuthResponse;
import com.example.aeternus_back_end.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import com.example.aeternus_back_end.repository.AdminUserRepository;
import com.example.aeternus_back_end.repository.AdminUserOtpRepository;
import com.example.aeternus_back_end.model.AdminUser;
import com.example.aeternus_back_end.model.AdminUserOtp;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.Random;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AdminUserRepository adminUserRepository;
    private final AdminUserOtpRepository adminUserOtpRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.from}")
    private String fromAddress;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        String usernameOrEmail = request.getUsername();
        
        AdminUser user = adminUserRepository.findByUsername(usernameOrEmail)
                .orElseGet(() -> adminUserRepository.findByEmail(usernameOrEmail).orElse(null));

        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Make sure that the email is correct. If you don't have an account, contact your admin."));
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String jwtToken = jwtService.generateTokenWithRole(userDetails, user.getRole());

            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwtToken)
                    .username(userDetails.getUsername())
                    .role(user.getRole())
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }
    }

    @GetMapping("/me")
    public ResponseEntity<String> getMe(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(authentication.getName());
    }

    @PostMapping("/forgot-password")
    @Transactional
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        AdminUser user = adminUserRepository.findByUsername(username)
                .orElseGet(() -> adminUserRepository.findByEmail(username).orElse(null));

        if (user == null) {
            return ResponseEntity.status(400).body(Map.of("error", "Make sure that the email is correct. If you don't have an account, contact your admin."));
        }

        // Generate 6 digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        
        // Delete old OTPs for user
        adminUserOtpRepository.deleteByAdminUser(user);

        // Save new OTP
        AdminUserOtp userOtp = AdminUserOtp.builder()
                .adminUser(user)
                .otp(otp)
                .expiryTime(LocalDateTime.now().plusMinutes(5))
                .build();
        adminUserOtpRepository.save(userOtp);

        // Send Email
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");
            helper.setFrom(fromAddress);
            helper.setTo(user.getEmail());
            helper.setSubject("Password Reset OTP");
            helper.setText("Your password reset OTP is: <b>" + otp + "</b><br>It will expire in 5 minutes.", true);
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to send email"));
        }

        return ResponseEntity.ok(Map.of("message", "If the account exists, an OTP has been sent."));
    }

    @PostMapping("/reset-password")
    @Transactional
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String otp = request.get("otp");
        String newPassword = request.get("newPassword");

        if (username == null || otp == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }

        AdminUser user = adminUserRepository.findByUsername(username)
                .orElseGet(() -> adminUserRepository.findByEmail(username).orElse(null));

        if (user == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid request"));
        }

        AdminUserOtp userOtp = adminUserOtpRepository.findByAdminUserAndOtp(user, otp).orElse(null);
        if (userOtp == null || userOtp.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired OTP"));
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        adminUserRepository.save(user);

        // Clear OTP
        adminUserOtpRepository.delete(userOtp);

        return ResponseEntity.ok(Map.of("message", "Password reset successfully"));
    }
}
