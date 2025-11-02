package com.example.bankcards.controller;

import com.example.bankcards.dto.SignupResponseDto;
import com.example.bankcards.entity.dto.LoginRequestDto;
import com.example.bankcards.entity.dto.LoginResponseDto;
import com.example.bankcards.security.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;


class AuthControllerTest {
    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_ReturnsLoginResponse() {
        LoginRequestDto request = new LoginRequestDto("user", "pass");
        LoginResponseDto responseDto = new LoginResponseDto("jwt-token", 1L);
        when(authService.login(request)).thenReturn(responseDto);

        ResponseEntity<LoginResponseDto> response = authController.login(request);

        assertEquals(200, response.getStatusCode().value(), "Код ответа должен быть 200");
        assertEquals(responseDto, response.getBody(), "Тело ответа должно совпадать");
    }

    @Test
    void signup_ReturnsSignupResponse() {
        LoginRequestDto request = new LoginRequestDto("newuser", "pass");
        SignupResponseDto responseDto = new SignupResponseDto(2L, "newuser");
        when(authService.signup(request)).thenReturn(responseDto);

        ResponseEntity<SignupResponseDto> response = authController.signup(request);

        assertEquals(200, response.getStatusCode().value(), "Код ответа должен быть 200");
        assertEquals(responseDto, response.getBody(), "Тело ответа должно совпадать");
    }
}