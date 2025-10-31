package com.example.bankcards.controller;

import com.example.bankcards.entity.dto.CardDto;
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
import java.util.List;

import static com.example.bankcards.entity.enums.CardOperation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/admin/cards")
@RequiredArgsConstructor
public class AdminCardsController {
    private final AdminCardService adminCardService;

    @GetMapping
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

    @GetMapping(path = "/{cardID}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> getCardById(@PathVariable Long cardID) {
        return ResponseEntity.ok().body(adminCardService.getCardById(cardID));
    }

    @PostMapping(path = "/{ownerID}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardDto> createCard(@PathVariable("ownerID") Long ownerID) {
        CardDto created = adminCardService.createCard(ownerID);

        var location = URI.create(String.format("/api/v1/admin/cards/%d", created.getCardID()));
        return ResponseEntity.created(location).body(created);
    }

    @PatchMapping("/{cardID}/block")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cardOperation(@PathVariable("cardID") Long cardID) {
        adminCardService.performOperation(cardID, BLOCK);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{cardID}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> activateCard(@PathVariable("cardID") Long cardID) {
        adminCardService.performOperation(cardID, ACTIVATE);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{cardID}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCard(@PathVariable("cardID") Long cardID) {
        adminCardService.performOperation(cardID, DELETE);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{cardID}/deep-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deepDeleteCard(@PathVariable("cardID") Long cardID) {
        adminCardService.performOperation(cardID, DEEP_DELETE);
        return ResponseEntity.noContent().build();
    }
}
