package com.example.aeternus_back_end.repository;

import com.example.aeternus_back_end.model.AdminUser;
import com.example.aeternus_back_end.model.AdminUserOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserOtpRepository extends JpaRepository<AdminUserOtp, String> {
    Optional<AdminUserOtp> findByAdminUserAndOtp(AdminUser adminUser, String otp);
    void deleteByAdminUser(AdminUser adminUser);
}
