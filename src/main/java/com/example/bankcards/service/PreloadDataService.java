package com.example.bankcards.service;

import com.example.bankcards.entity.CardStatusRequestEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.CardStatusRequestRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.bankcards.entity.enums.CardOperation.ACTIVATE;
import static com.example.bankcards.entity.enums.CardOperation.BLOCK;
import static com.example.bankcards.entity.enums.Role.USER;

/**
 * Сервис для автоматической предзагрузки тестовых данных в базу при старте приложения.
 * <p>
 * Реализует интерфейс {@link CommandLineRunner}, что позволяет выполнять
 * инициализацию данных сразу после поднятия контекста Spring Boot.
 *
 * <p>Выполняет три основных действия:
 * <ul>
 *   <li>Создает тестовых пользователей с ролью {@link Role#USER}, если они отсутствуют.</li>
 *   <li>Создает банковские карты для пользователей без карт.</li>
 *   <li>Создает тестовые заявки на изменение статуса карт (блокировка/активация).</li>
 * </ul>
 *
 * <p>Используется только в dev/stage окружениях для генерации демонстрационных данных.
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PreloadDataService implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AdminCardService adminCardService;
    private final PasswordEncoder encoder;
    private final CardStatusRequestRepository cardStatusRequestRepository;

    /**
     * Выполняется при старте приложения. Последовательно вызывает
     * методы предзагрузки пользователей, карт и заявок.
     *
     * @param args аргументы командной строки (не используются)
     */
    @Override
    public void run(String... args) throws Exception {
        preloadUserList();
        preloadCardForUserList();
        preloadCardStatusRequestList();
    }

    /**
     * Создает набор тестовых пользователей, если в базе отсутствуют пользователи с ролью {@link Role#USER}.
     */
    private void preloadUserList() {
        List<UserEntity> allUsers = userRepository.findAll();
        long countOfUsers = allUsers.stream().filter(u -> USER == u.getRole()).count();

        if (countOfUsers == 0) {
            UserEntity user1 = createTestUser("user1", "password1", USER, true, encoder);
            UserEntity user2 = createTestUser("user2", "password2", USER, true, encoder);
            UserEntity user3 = createTestUser("user3", "password3", USER, false, encoder);
            UserEntity user4 = createTestUser("user4", "password4", USER, true, encoder);
            UserEntity user5 = createTestUser("user5", "password5", USER, true, encoder);


            userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
        }
    }

    /**
     * Для каждого пользователя без карт создает по три тестовые карты.
     */
    private void preloadCardForUserList() {
        List<UserEntity> allUsers = userRepository.findAllWithCards();

        allUsers.stream()
                .filter(user -> user.getCards().isEmpty())
                .filter(u -> USER == u.getRole())
                .forEach((UserEntity user) -> {
                    for (int i = 0; i < 3; i++) {
                        adminCardService.createCard(user.getId());
                    }
                });
    }

    /**
     * Создает тестовые заявки на изменение статуса карт (через одну — блокировка/активация).
     */
    private void preloadCardStatusRequestList() {
        var counter = new AtomicInteger();
        userRepository.findAllWithCards().forEach(user -> {
            if (counter.incrementAndGet() % 2 == 0) {
                CardStatusRequestEntity statusRequest = CardStatusRequestEntity.builder()
                        .ownerID(user.getId())
                        .cardID(user.getCards().getFirst().getId())
                        .status(BLOCK)
                        .build();

                cardStatusRequestRepository.save(statusRequest);
            } else {
                CardStatusRequestEntity statusRequest = CardStatusRequestEntity.builder()
                        .ownerID(user.getId())
                        .cardID(user.getCards().getFirst().getId())
                        .status(ACTIVATE)
                        .build();

                cardStatusRequestRepository.save(statusRequest);
            }
        });
    }

    /**
     * Создает сущность пользователя с указанными параметрами.
     *
     * @param username имя пользователя
     * @param password пароль (будет закодирован)
     * @param role     роль пользователя
     * @param enabled  статус активности
     * @param encoder  {@link PasswordEncoder} для шифрования пароля
     * @return новый объект {@link UserEntity}
     */
    private static UserEntity createTestUser(String username,
                                             String password,
                                             Role role,
                                             boolean enabled,
                                             PasswordEncoder encoder) {
        return UserEntity.builder()
                .username(username)
                .password(encoder.encode(password))
                .role(role)
                .enabled(enabled)
                .build();
    }
}
