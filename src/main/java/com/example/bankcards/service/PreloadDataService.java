package com.example.bankcards.service;

import com.example.bankcards.entity.CardStatusRequestEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.repository.CardStatusRequestRepository;
import com.example.bankcards.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.example.bankcards.entity.enums.CardOperation.ACTIVATE;
import static com.example.bankcards.entity.enums.CardOperation.BLOCK;
import static com.example.bankcards.entity.enums.Role.USER;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreloadDataService implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AdminCardService adminCardService;
    private final CardStatusRequestRepository cardStatusRequestRepository;

    @Override
    public void run(String... args) throws Exception {
        preloadUserList();
        preloadCardForUserList();
        preloadCardStatusRequestList();
    }


    private void preloadUserList() {
        UserEntity user1 = UserEntity.builder()
                .username("user1")
                .password("password1")
                .role(USER)
                .enabled(true)
                .build();

        UserEntity user2 = UserEntity.builder()
                .username("user2")
                .password("password2")
                .role(USER)
                .enabled(true)
                .build();

        UserEntity user3 = UserEntity.builder()
                .username("user3")
                .password("password3")
                .role(USER)
                .enabled(false)
                .build();

        UserEntity user4 = UserEntity.builder()
                .username("user4")
                .password("password4")
                .role(USER)
                .enabled(true)
                .build();

        UserEntity user5 = UserEntity.builder()
                .username("user5")
                .password("password5")
                .role(USER)
                .enabled(true)
                .build();

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
    }

    private void preloadCardForUserList() {
        Iterable<UserEntity> allUsers = userRepository.findAll();

        for (UserEntity user : allUsers) {
            for (int i = 0; i < 3; i++) {
                adminCardService.createCard(user.getId());
            }
        }
    }

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
            counter.getAndIncrement();
        });
    }
}
