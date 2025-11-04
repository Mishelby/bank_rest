package com.example.bankcards.controller;

import com.example.bankcards.dto.SignupResponseDto;
import com.example.bankcards.dto.LoginRequestDto;
import com.example.bankcards.dto.LoginResponseDto;
import com.example.bankcards.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<LoginResponseDto> login(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        log.info("[INFO] POST запрос на вход в систему");
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping(path = "/signup", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<SignupResponseDto> signup(@RequestBody @Valid LoginRequestDto loginRequestDto) {
        log.info("[INFO] POST запрос на создание нового аккаунта");
        return ResponseEntity.ok(authService.signup(loginRequestDto));
    }
}
