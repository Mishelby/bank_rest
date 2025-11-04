package com.example.bankcards.service;

import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static java.util.Objects.nonNull;

/**
 * Сервис для предварительной загрузки (инициализации) учетной записи администратора в систему.
 * <p>
 * Активируется только при установленном свойстве {@code preload.admin=true}.
 * Реализует интерфейс {@link CommandLineRunner}, поэтому выполняется автоматически
 * при запуске Spring Boot приложения.
 *
 * <p>Основные задачи:
 * <ul>
 *   <li>Проверить наличие пользователя с именем {@code admin} в базе данных.</li>
 *   <li>Если такого пользователя нет — создать его с ролью {@link Role#ADMIN} и паролем
 *       из параметра {@code data.admin.password}.</li>
 * </ul>
 * <p>
 * Пример настройки в application.yml:
 * <pre>
 * preload:
 *   admin: true
 *
 * data:
 *   admin:
 *     password: admin123
 * </pre>
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "preload", name = "admin", havingValue = "true")
public class PreloadAdminService implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${data.admin.password}")
    private String adminPasswordString;

    private char[] adminPassword;

    @PostConstruct
    public void init() {
        if (nonNull(adminPasswordString)) {
            adminPassword = adminPasswordString.toCharArray();
            adminPasswordString = null;
        }
    }

    /**
     * Точка входа при запуске приложения. Запускает процесс инициализации администратора.
     *
     * @param args аргументы командной строки (не используются)
     */
    @Override
    public void run(String... args) throws Exception {
        initializeAdmin();
    }

    /**
     * Проверяет, существует ли учетная запись администратора.
     * Если нет — создает новую запись с зашифрованным паролем.
     */
    private void initializeAdmin() {
        Optional<UserEntity> admin = userRepository.findByUsername("admin");

        if (admin.isEmpty()) {
            userRepository.save(
                    UserEntity.builder()
                            .username("admin")
                            .password(passwordEncoder.encode(new String(adminPassword)))
                            .role(Role.ADMIN)
                            .enabled(true)
                            .build()
            );
        }
    }
}
