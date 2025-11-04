package com.example.bankcards.service;



import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.handler.CardOperationHandler;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.RepositoryHelper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@Disabled
@ExtendWith(MockitoExtension.class)
class AdminCardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private RepositoryHelper repositoryHelper;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private Map<CardOperation, CardOperationHandler> cardOperationsHandler;

    @InjectMocks
    private AdminCardService adminCardService;

    private final Long USER_ID = 1L;
    private final Long CARD_ID = 10L;

    private CardEntity activeCard;
    private CardEntity blockedCard;
    private CardEntity activeCardTo;
    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = new UserEntity();
        user.setId(USER_ID);

        activeCard = new CardEntity();
        activeCard.setId(CARD_ID);
        activeCard.setOwner(user);
        activeCard.setCardStatus(CardStatus.ACTIVE);
        activeCard.setBalance(BigDecimal.valueOf(1000));

        blockedCard = new CardEntity();
        blockedCard.setId(20L);
        blockedCard.setOwner(user);
        blockedCard.setCardStatus(CardStatus.BLOCKED);
        blockedCard.setBalance(BigDecimal.valueOf(500));

        activeCardTo = new CardEntity();
        activeCardTo.setId(30L);
        activeCardTo.setOwner(user);
        activeCardTo.setCardStatus(CardStatus.ACTIVE);
        activeCardTo.setBalance(BigDecimal.valueOf(500));

        user.setCards(List.of(activeCard, blockedCard, activeCardTo));
    }

    @Test
    void getCardById_shouldReturnCardDto() {
        when(repositoryHelper.findCardEntityByID(10L)).thenReturn(activeCard);
        CardDto cardDto = new CardDto();
        when(cardMapper.toDto(activeCard)).thenReturn(cardDto);

        CardDto result = adminCardService.getCardById(10L);

        assertNotNull(result);
        assertEquals(cardDto, result);
    }

    @Test
    void getCardById_shouldThrowException_whenCardNotFound() {
        when(repositoryHelper.findCardEntityByID(10L)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> adminCardService.getCardById(10L));
    }

    @Test
    void createCard_shouldCreateCardSuccessfully() {
        when(repositoryHelper.findUserEntityByID(1L)).thenReturn(user);
        when(cardRepository.save(any(CardEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        CardDto cardDto = new CardDto();
        when(cardMapper.toDto(any(CardEntity.class))).thenReturn(cardDto);

        CardDto result = adminCardService.createCard(1L);

        assertNotNull(result);
        assertEquals(cardDto, result);
        verify(cardRepository, times(1)).save(any(CardEntity.class));
    }

    @Test
    void createCard_shouldThrowException_whenUserNotFound() {
        when(repositoryHelper.findUserEntityByID(1L)).thenThrow(EntityNotFoundException.class);

        assertThrows(EntityNotFoundException.class, () -> adminCardService.createCard(1L));
    }

    @Test
    void performOperation_shouldPerformCardOperationSuccessfully() {
        when(repositoryHelper.findCardEntityByID(10L)).thenReturn(activeCard);
        CardOperationHandler handler = mock(CardOperationHandler.class);
        when(cardOperationsHandler.get(CardOperation.BLOCK)).thenReturn(handler);
        CardDto cardDto = new CardDto();
        when(cardMapper.toDto(activeCard)).thenReturn(cardDto);

        CardDto result = adminCardService.performOperation(10L, CardOperation.BLOCK);

        assertNotNull(result);
        assertEquals(cardDto, result);
        verify(handler, times(1)).handle(activeCard);
    }

    @Test
    void performOperation_shouldThrowException_whenOperationInvalid() {
        when(cardOperationsHandler.get(CardOperation.BLOCK)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> adminCardService.performOperation(10L, CardOperation.BLOCK));

        assertTrue(ex.getMessage().contains("Некорректная операция"));
    }
}