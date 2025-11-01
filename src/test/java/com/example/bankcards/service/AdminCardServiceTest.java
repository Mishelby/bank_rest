package com.example.bankcards.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.RepositoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class AdminCardServiceTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private RepositoryHelper repositoryHelper;

    @Mock
    private CardMapper cardMapper;

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
    void testGetAllCards() {
        // Arrange
        when(repositoryHelper.getSpecificationWithParams(null, null, null))
                .thenReturn(Specification.unrestricted());
        List<CardEntity> cards = new ArrayList<>();
        cards.add(new CardEntity());
        PageRequest pageable = PageRequest.of(0, 10);
        when(cardRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(new PageImpl<>(cards));
        when(cardMapper.toDto(cards.get(0))).thenReturn(new CardDto());

        // Act
        List<CardDto> result = adminCardService.getAllCards(0, 10, null, null, null);

        // Assert
        assertEquals(1, result.size());
    }

    @Test
    void testCreateCard() {
        // Arrange
        long ownerId = 1L;
        var userEntity = mock(UserEntity.class);
        when(repositoryHelper.findUserEntityByID(ownerId)).thenReturn(userEntity);

        var cardEntity = new CardEntity();
        cardEntity.setId(1L);
        when(cardRepository.save(any())).thenReturn(cardEntity);
        when(cardMapper.toDto(cardEntity)).thenReturn(new CardDto());

        // Act
        CardDto createdCard = adminCardService.createCard(ownerId);

        // Assert
        verify(cardRepository).save(any());
        assertEquals(CardStatus.ACTIVE, cardEntity.getCardStatus());
    }
}