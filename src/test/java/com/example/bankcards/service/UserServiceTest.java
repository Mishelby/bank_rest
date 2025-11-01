package com.example.bankcards.service;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatusRequestEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.dto.CardDto;
import com.example.bankcards.entity.dto.CardStatusResponse;
import com.example.bankcards.entity.dto.TransferRequestDto;
import com.example.bankcards.entity.dto.TransferInfoDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardStatusRequestRepository;
import com.example.bankcards.util.RepositoryHelper;
import jakarta.persistence.EntityNotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private RepositoryHelper repositoryHelper;

    @Mock
    private CardStatusRequestRepository cardStatusRequestRepository;

    @Mock
    private CardMapper cardMapper;

    @InjectMocks
    private UserService userService;

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
    void findCardByID_shouldReturnCardDto_whenCardExists() throws EntityNotFoundException {
        CardDto expectedDto = new CardDto();
        expectedDto.setCardID(CARD_ID);

        Mockito.doNothing().when(repositoryHelper).isUserExists(USER_ID);
        Mockito.when(repositoryHelper.findCardEntityByID(CARD_ID)).thenReturn(activeCard);
        Mockito.when(cardMapper.toDto(activeCard)).thenReturn(expectedDto);

        CardDto actual = userService.findCardByID(USER_ID, CARD_ID);

        assertEquals(expectedDto, actual, "CardDto должны соответствовать друг другу!");
        Mockito.verify(repositoryHelper).isUserExists(USER_ID);
        Mockito.verify(cardMapper).toDto(activeCard);
    }

    @Test
    void requestToBlockCard_shouldThrowException_whenCardNotOwnedByUser() {
        activeCard.getOwner().setId(2L);

        Mockito.doNothing().when(repositoryHelper).isUserExists(USER_ID);
        Mockito.when(repositoryHelper.findCardEntityByID(CARD_ID)).thenReturn(activeCard);

        assertThrows(IllegalArgumentException.class,
                () -> userService.requestToBlockCard(USER_ID, CARD_ID),
                "Карта должна принадлежать пользователю!");
    }

    @Test
    void requestToBlockCard_shouldReturnResponse_whenCardActive() throws ConstraintViolationException {
        CardStatusRequestEntity savedRequest = new CardStatusRequestEntity();
        savedRequest.setCardID(CARD_ID);
        savedRequest.setOwnerID(USER_ID);

        Mockito.doNothing().when(repositoryHelper).isUserExists(USER_ID);
        Mockito.when(repositoryHelper.findCardEntityByID(CARD_ID)).thenReturn(activeCard);
        Mockito.when(cardStatusRequestRepository.save(Mockito.any())).thenReturn(savedRequest);

        CardStatusResponse response = userService.requestToBlockCard(USER_ID, CARD_ID);

        assertEquals(CARD_ID, response.cardID(), "ID карт должны совпасть!");
        assertEquals(USER_ID, response.ownerID(), "ID пользователя должны совпасть!");
        assertEquals("Заявка на блокировку отправлена!", response.message(),
                "Должны получить сообщение об отправленной заявки на блокировку карты!");
    }

    @Test
    void findUserCardBalance_shouldReturnBalance_whenCardActive() throws EntityNotFoundException {
        Mockito.doNothing().when(repositoryHelper).isUserExists(USER_ID);
        Mockito.when(repositoryHelper.findCardEntityByIDAndStatus(CARD_ID, CardStatus.ACTIVE))
                .thenReturn(activeCard);

        BigDecimal balance = userService.findUserCardBalance(USER_ID, CARD_ID);

        assertEquals(BigDecimal.valueOf(1000), balance, "Баланс карты должен совпасть с реальным");
    }


    @Test
    void transferMoney_shouldSucceed_whenValid() {
        TransferRequestDto request = new TransferRequestDto(CARD_ID, activeCardTo.getId(), BigDecimal.valueOf(200));

        Mockito.when(repositoryHelper.findCardEntityByID(CARD_ID)).thenReturn(activeCard);
        Mockito.when(repositoryHelper.findCardEntityByID(activeCardTo.getId())).thenReturn(activeCardTo);
        Mockito.when(repositoryHelper.findUserEntityByID(USER_ID)).thenReturn(user);

        TransferInfoDto transferInfoDto = TransferInfoDto.builder()
                .transferDate(LocalDateTime.now())
                .numberCardFrom("1111")
                .numberCardTo("2222")
                .amount(BigDecimal.valueOf(200))
                .cardFromBalance(BigDecimal.valueOf(800))
                .cardBalanceTo(BigDecimal.valueOf(700))
                .build();

        Mockito.when(cardMapper.toTransferInfoDto(activeCard, activeCardTo, BigDecimal.valueOf(200)))
                .thenReturn(transferInfoDto);

        TransferInfoDto result = userService.transferMoney(USER_ID, request);

        assertEquals(transferInfoDto, result, "TransferInfoDto должен соответствовать результату");
        assertEquals(BigDecimal.valueOf(800), activeCard.getBalance(), "Баланс должен быть равен ожидаемому");
        assertEquals(BigDecimal.valueOf(700), activeCardTo.getBalance(), "Баланс должен быть равен ожидаемому");
    }

    @Test
    void transferMoney_shouldThrowException_whenAmountInvalid() {
        TransferRequestDto request = new TransferRequestDto(CARD_ID, activeCardTo.getId(), BigDecimal.valueOf(0));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.transferMoney(USER_ID, request));

        assertEquals("Сумма перевода должна быть больше 0",
                exception.getMessage(), "Сумма перевода должна быть больше 0");
    }

    @Test
    void transferMoney_shouldThrowException_whenCardNotActive() {
        TransferRequestDto request = new TransferRequestDto(CARD_ID, blockedCard.getId(), BigDecimal.valueOf(100));

        Mockito.when(repositoryHelper.findCardEntityByID(CARD_ID)).thenReturn(activeCard);
        Mockito.when(repositoryHelper.findCardEntityByID(blockedCard.getId())).thenReturn(blockedCard);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.transferMoney(USER_ID, request));

        assertEquals("Нельзя перевести средства! Одна из карт не активна",
                exception.getMessage(), "Для перевода средств обе карты должны быть активны");
    }

    @Test
    void transferMoney_shouldThrowException_whenCardsNotBelongToUser() {
        TransferRequestDto request = new TransferRequestDto(CARD_ID, activeCardTo.getId(), BigDecimal.valueOf(100));

        UserEntity anotherUser = new UserEntity();
        anotherUser.setId(2L);
        anotherUser.setCards(List.of());

        Mockito.when(repositoryHelper.findCardEntityByID(CARD_ID)).thenReturn(activeCard);
        Mockito.when(repositoryHelper.findCardEntityByID(activeCardTo.getId())).thenReturn(activeCardTo);
        Mockito.when(repositoryHelper.findUserEntityByID(USER_ID)).thenReturn(anotherUser);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.transferMoney(USER_ID, request));

        assertEquals("Ошибка! Одна из карт не принадлежит пользователю!", exception.getMessage(),
                "Для перевода средств обе карты должны принадлежать пользователю");
    }

    @Test
    void transferMoney_shouldThrowException_whenInsufficientFunds() {
        TransferRequestDto request = new TransferRequestDto(CARD_ID, activeCardTo.getId(), BigDecimal.valueOf(2000));

        Mockito.when(repositoryHelper.findCardEntityByID(CARD_ID)).thenReturn(activeCard);
        Mockito.when(repositoryHelper.findCardEntityByID(activeCardTo.getId())).thenReturn(activeCardTo);
        Mockito.when(repositoryHelper.findUserEntityByID(USER_ID)).thenReturn(user);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.transferMoney(USER_ID, request));

        assertEquals("Недостаточно средств для перевода", exception.getMessage(),
                "Для перевода средств баланс ");
    }
}
