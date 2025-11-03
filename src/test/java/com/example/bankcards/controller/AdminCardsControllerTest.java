package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.AuthUtil;
import com.example.bankcards.service.AdminCardService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.example.bankcards.entity.enums.CardStatus.*;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(controllers = AdminCardsController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminCardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AdminCardService adminCardService;

    @MockitoBean
    private AuthUtil authUtil;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /api/v1/admin/cards/{ownerID} — должен вернуть статус 201 CREATED и новую карту")
    void createCardByUserID_shouldReturnOk() throws Exception {
        CardDto dto = new CardDto();
        dto.setCardID(1L);
        dto.setNumber("**** **** **** 1234");
        dto.setCardStatus(ACTIVE);
        dto.setBalance(BigDecimal.valueOf(200));
        dto.setExpirationDate(LocalDate.now().plusYears(1));

        Mockito.when(adminCardService.createCard(1L)).thenReturn(dto);

        mockMvc.perform(post("/api/v1/admin/cards/1"))
                .andExpect(status().isCreated())
                .andExpect(content().string(objectMapper.writeValueAsString(dto)))
                .andExpect(jsonPath("$.cardID").value(1L))
                .andExpect(jsonPath("$.cardStatus").value("ACTIVE"))
                .andExpect(header().string("Location", "/api/v1/admin/cards/1"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/admin/cards/{cardID} — должен вернуть статус 200 OK и данные карты")
    void findCardById_shouldReturnOk() throws Exception {
        CardDto dto = new CardDto();
        dto.setCardID(1L);
        dto.setNumber("**** **** **** 1234");
        dto.setCardStatus(ACTIVE);
        dto.setBalance(BigDecimal.valueOf(200));
        dto.setExpirationDate(LocalDate.now().plusYears(1));

        Mockito.when(adminCardService.getCardById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/admin/cards/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardID").value(1L))
                .andExpect(jsonPath("$.cardStatus").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/admin/cards — должен вернуть статус 200 OK и данные всех карты")
    void findAllCardsWithPagination_shouldReturnOk() throws Exception {
        CardDto dto = new CardDto();
        dto.setCardID(1L);
        dto.setNumber("**** **** **** 1234");
        dto.setCardStatus(ACTIVE);
        dto.setBalance(BigDecimal.valueOf(200));
        dto.setExpirationDate(LocalDate.now().plusYears(1));

        Mockito.when(adminCardService.getAllCards(anyInt(), anyInt(), any(), any(), any()))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/admin/cards")
                        .param("page", "0")
                        .param("size", "10")
                        .param("status", "ACTIVE")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].cardID").value(1L))
                .andExpect(jsonPath("$[0].cardStatus").value("ACTIVE"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/admin/cards/{cardID}/operation — должен вернуть статус 200 OK")
    void blockCardByCardID_shouldReturnNoContent() throws Exception {
        CardDto dto = new CardDto();
        dto.setCardID(1L);
        dto.setNumber("**** **** **** 1234");
        dto.setCardStatus(BLOCKED);

        Mockito.when(adminCardService.performOperation(1L)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/admin/cards/1/operation"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardID").value(1L))
                .andExpect(jsonPath("$.cardStatus").value("BLOCKED"));

        Mockito.verify(adminCardService).performOperation(eq(1L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/admin/cards/{cardID}/operation — должен вернуть статус 200 OK")
    void activateCardByCardID_shouldReturnOk() throws Exception {
        CardDto dto = new CardDto();
        dto.setCardID(2L);
        dto.setNumber("**** **** **** 5678");
        dto.setCardStatus(ACTIVE);

        Mockito.when(adminCardService.performOperation(2L)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/admin/cards/2/operation"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardID").value(2L))
                .andExpect(jsonPath("$.cardStatus").value("ACTIVE"));

        Mockito.verify(adminCardService).performOperation(eq(2L));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PATCH /api/v1/admin/cards/{cardID}/operation — должен вернуть статус 200 OK")
    void deleteCardByCardID_shouldReturnOk() throws Exception {
        CardDto dto = new CardDto();
        dto.setCardID(3L);
        dto.setNumber("**** **** **** 9876");
        dto.setCardStatus(DELETED);

        Mockito.when(adminCardService.performOperation(3L)).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/admin/cards/3/operation"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.cardID").value(3L))
                .andExpect(jsonPath("$.cardStatus").value("DELETED"));

        Mockito.verify(adminCardService).performOperation(eq(3L));
    }

}
