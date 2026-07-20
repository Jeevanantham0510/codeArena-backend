package com.codearena.config;

import com.codearena.entity.Role;
import com.codearena.entity.User;
import com.codearena.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Registration always creates ROLE_USER accounts, so there's no way to reach
 * an admin-gated route on a fresh database. This seeds one admin account on
 * startup so the console is reachable — promote further admins from there.
 * Remove or gate behind a profile before shipping to production.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.countByRole(Role.ADMIN) == 0) {
            User admin = User.builder()
                    .username("admin")
                    .email("admin@codearena.local")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .build();
            userRepository.save(admin);
            System.out.println(">>> Seeded default admin: admin@codearena.local / admin123 (change this immediately)");
        }
    }
}
