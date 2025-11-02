package com.example.bankcards.service;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "preload", name = "admin", havingValue = "true")
public class PreloadAdminService implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${data.admin.password}")
    private String adminPassword;

    @Override
    public void run(String... args) throws Exception {
        initializeAdmin();
    }

    private void initializeAdmin() {
        Optional<UserEntity> admin = userRepository.findByUsername("admin");

        if (admin.isEmpty()) {
            userRepository.save(
                    UserEntity.builder()
                            .username("admin")
                            .password(passwordEncoder.encode(adminPassword))
                            .role(Role.ADMIN)
                            .enabled(true)
                            .build()
            );
        }
    }
}
