package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardStatusRequestDto;
import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.service.AdminCardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
public class AdminCardsController {
    private final AdminCardService adminCardService;

    /**
     * Получить все карты (с пагинацией и фильтрацией).
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardDto>> getAllAdminCards(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CardStatus status,
            @RequestParam(required = false) Long ownerID,
            @RequestParam(required = false) LocalDate expirationDate

    ) {
        return ResponseEntity.ok().body(adminCardService.getAllCards(page, size, status, ownerID, expirationDate));
    }

    /**
     * Получить все запросы на изменение статуса карт.
     */
    @GetMapping(path = "/status-requests", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CardStatusRequestDto>> getAllCardsRequestByAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) CardOperation status,
            @RequestParam(required = false) Long ownerID,
            @RequestParam(required = false) Long cardID,
            @RequestParam(required = false) LocalDateTime requestedAt

    ) {
        return ResponseEntity.ok().body(
                adminCardService.getAllCardsRequests(page, size, status, ownerID, cardID, requestedAt)
        );
    }

    /**
     * Получить карту по её ID.
     */
    @GetMapping(path = "/{cardID}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long cardID) {
        return ResponseEntity.ok().body(adminCardService.getCardById(cardID));
    }

    /**
     * Создать новую карту для пользователя по его ID.
     */
    @PostMapping(path = "/{ownerID}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@PathVariable("ownerID") Long ownerID) {
        CardDto created = adminCardService.createCard(ownerID);

        var location = URI.create(String.format("/api/v1/admin/cards/%d", created.getCardID()));
        return ResponseEntity.created(location).body(created);
    }

    /**
     * Выполнить операцию с картой (например, блокировка/активация).
     */
    @PatchMapping("/{cardID}/operation")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> cardOperation(@PathVariable("cardID") Long cardID) {
        return ResponseEntity.ok(adminCardService.performOperation(cardID));
    }

}
