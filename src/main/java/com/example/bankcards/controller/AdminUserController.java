package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.service.AdminUserService;
import jakarta.validation.constraints.PastOrPresent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST-контроллер для административных операций над пользователями.
 * <p>
 * Предоставляет API для получения списка пользователей,
 * просмотра конкретного пользователя и удаления пользователя.
 * <p>
 * Доступ к методам контроллера ограничен ролью {@code ADMIN}.
 * <p><b>Базовый URL:</b> {@code /api/v1/admin/users}</p>
 * @see AdminUserService
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {
    private final AdminUserService adminUserService;

    /**
     * Возвращает список пользователей с возможностью фильтрации и пагинации.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDto>> getAllUsers(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam(required = false) Boolean enabled,
                                                     @RequestParam(required = false)
                                                     @PastOrPresent(message = "Дата создания не может быть в будущем")
                                                     LocalDateTime createdDate) {
        log.info("[INFO] GET запрос на получение списка всех пользователей");
        return ResponseEntity.ok(adminUserService.getAllUsers(page, size, enabled, createdDate));
    }

    /**
     * Возвращает информацию о пользователе по его идентификатору.
     */
    @GetMapping(path = "{userID}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDto> getUserById(@PathVariable("userID") Long userID) {
        log.info("[INFO] GET запрос на получение пользователя по его ID");
        return ResponseEntity.ok(
                adminUserService.getUserByID(userID)
        );
    }


    /**
     * Удаляет пользователя по его идентификатору.
     */
    @DeleteMapping(path = "{userID}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> userOperation(@PathVariable("userID") Long userID) {
        log.info("[INFO] DELETE запрос на удаление пользователя");
        adminUserService.deleteUserByID(userID);
        return ResponseEntity.noContent().build();
    }

}
