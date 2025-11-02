package com.example.bankcards.service;



import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.RepositoryHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

@Disabled
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


}