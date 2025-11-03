package com.example.bankcards.controller;

import com.example.bankcards.dto.LoginRequestDto;
import com.example.bankcards.dto.LoginResponseDto;
import com.example.bankcards.dto.SignupResponseDto;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.AuthService;
import com.example.bankcards.security.AuthUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private AuthUtil authUtil;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    @DisplayName("POST /api/v1/auth/login — должен вернуть статус 200, ID и JWT токен")
    void login_ReturnsLoginResponse() throws Exception {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("username")
                .password("password")
                .build();

        LoginResponseDto responseDto = LoginResponseDto.builder()
                .userId(1L)
                .jwt("jwtToken")
                .build();

        Mockito.when(authService.login(loginRequest)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.jwt").value("jwtToken"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/signup — должен вернуть статус 200, ID и username")
    void signun_ReturnsSignupResponseDto() throws Exception {
        LoginRequestDto loginRequest = LoginRequestDto.builder()
                .username("username")
                .password("password")
                .build();

        SignupResponseDto responseDto = SignupResponseDto.builder()
                .userId(1L)
                .username("username")
                .build();

        Mockito.when(authService.signup(loginRequest)).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.username").value("username"));
    }

}