package com.example.aeternus_back_end;

import com.example.aeternus_back_end.model.AdminUser;
import com.example.aeternus_back_end.repository.AdminUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (adminUserRepository.count() == 0) {
            AdminUser defaultAdmin = AdminUser.builder()
                    .username("admin")
                    .email("admin@aeternus.co.zw")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role("admin")
                    .build();

            adminUserRepository.save(defaultAdmin);
            System.out.println("Default admin created: admin / admin123");
        }
    }
}
