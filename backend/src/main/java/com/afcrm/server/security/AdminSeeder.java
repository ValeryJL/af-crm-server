package com.afcrm.server.security;

import com.afcrm.server.model.Role;
import com.afcrm.server.model.User;
import com.afcrm.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:admin@afcrm.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:adminpassword}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("No users found. Seeding initial admin from environment variables.");
            
            User admin = User.builder()
                    .email(adminEmail)
                    .password(passwordEncoder.encode(adminPassword))
                    .role(Role.ADMIN)
                    .build();

            userRepository.save(admin);
            log.info("Admin user created successfully with email: {}", adminEmail);
        } else {
            log.info("Users already exist. Skipping admin seed.");
        }
    }
}
