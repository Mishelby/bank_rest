package com.example.bankcards.controller;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping(path = "/{userID}/cards", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<CardDto>> getAllUserCards(@PathVariable Long userID,
                                                         @RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size,
                                                         @RequestParam(required = false) CardStatus status,
                                                         @RequestParam(required = false) LocalDate expirationDate
    ) {
        log.info("[INFO] GET запрос на получение списка всех карт пользователя");
        return ResponseEntity.ok(userService.findAllUserCards(page, size, userID, status, expirationDate));
    }

    @GetMapping(path = "/{userID}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<UserDto> getUserByID(@PathVariable Long userID) {
        log.info("[INFO] GET запрос на получение пользователя по его ID");
        return ResponseEntity.ok(userService.findUserByID(userID));
    }

    @GetMapping(path = "/{userID}/{cardID}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardDto> getCardByID(
            @PathVariable Long userID,
            @PathVariable Long cardID) {
        log.info("[INFO] GET запрос на получение карты пользователя по его ID");
        return ResponseEntity.ok(userService.findCardByID(userID, cardID));
    }

    @GetMapping(path = "/{userID}/{cardID}/balance", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BigDecimal> getUserBalance(
            @PathVariable Long userID,
            @PathVariable Long cardID) {
        log.info("[INFO] GET запрос на получение баланса карты пользователя по его ID и ID карты");
        return ResponseEntity.ok(userService.findUserCardBalance(userID, cardID));
    }

    @PostMapping(path = "/{userID}/{cardID}/block", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardStatusResponse> requestToBlockCard(
            @PathVariable Long userID,
            @PathVariable Long cardID) {
        log.info("[INFO] POST запрос на отправку запроса на блокироваку карты пользователя");
        return ResponseEntity.ok(userService.requestToBlockCard(userID, cardID));
    }

    @PostMapping(path = "/{userID}/cards/transfer")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<TransferInfoDto> transferMoney(
            @PathVariable Long userID,
            @RequestBody @Valid TransferRequestDto request
    ) {
        log.info("[INFO] POST запрос на перевод средств между своими счетами пользователя");
        return ResponseEntity.ok(userService.transferMoney(userID, request));
    }
}
