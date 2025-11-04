package com.example.bankcards.controller;

import com.example.bankcards.dto.UserDto;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.AuthUtil;
import com.example.bankcards.service.AdminUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.example.bankcards.entity.enums.Role.USER;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Disabled
@WebMvcTest(controllers = AdminUserController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminUserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminUserService adminUserService;

    @MockitoBean
    private AuthUtil authUtil;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/admin/users")
    void findAllUsersWithPageable_shouldReturnOk() throws Exception {
        UserDto userDto = UserDto.builder()
                .id(1L)
                .username("username")
                .password("password")
                .role(USER)
                .build();

        Mockito.when(adminUserService.getAllUsers(anyInt(), anyInt(), any(), any()))
                .thenReturn(List.of(userDto));

        mockMvc.perform(get("/api/v1/admin/users")
                .param("page", "0")
                .param("size", "10")
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].username").value("username"));
    }
}
