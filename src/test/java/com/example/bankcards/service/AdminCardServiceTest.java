package com.example.bankcards.service;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.entity.enums.converter.CardEncryptorConverter;
import com.example.bankcards.util.MaskCardNumber;
import com.example.bankcards.util.RepositoryHelper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static com.example.bankcards.entity.enums.CardOperation.ACTIVATE;
import static com.example.bankcards.entity.enums.CardStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@Disabled
class AdminCardServiceTest {
    @InjectMocks
    private AdminCardService cardService;

    @Mock
    private RepositoryHelper repositoryHelper;

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardEncryptorConverter cardEncryptorConverter;

    @Mock
    private CardMapper cardMapper;

    private CardEntity cardEntity;

    private CardDto cardDto;

    private final String cardNumber = "1234 5678 9012 3456";
    private String maskNumber;
    private String encryptedCardNumber;
    private String decryptedCardNumber;

    private static final LocalDate expiredDate = LocalDate.now().plusYears(1L);

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        encryptedCardNumber = cardEncryptorConverter.convertToDatabaseColumn(cardNumber);
        decryptedCardNumber = cardEncryptorConverter.convertToEntityAttribute(cardNumber);

        cardEncryptorConverter = mock(CardEncryptorConverter.class);

        cardEntity = new CardEntity();
        cardEntity.setId(1L);
        cardEntity.setExpirationDate(expiredDate);
        cardEntity.setCardStatus(CardStatus.BLOCKED);
        cardEntity.setNumber(decryptedCardNumber);
        cardEntity.setBalance(BigDecimal.ZERO);

        maskNumber = MaskCardNumber.mask(cardNumber);

        cardDto = new CardDto(
                1L,
                1L,
                maskNumber,
                CardStatus.BLOCKED,
                cardEntity.getExpirationDate(),
                cardEntity.getBalance()
        );

    }

    @Test
    void testEncryptAndDecrypt() throws Exception {
        Long ownerId = 1L;

        UserEntity user = new UserEntity();
        user.setId(ownerId);

        when(repositoryHelper.findUserEntityByID(ownerId)).thenReturn(user);

        when(cardRepository.save(any(CardEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(cardRepository.findById(1L)).thenReturn(Optional.of(cardEntity));

        assertNotEquals(cardNumber, cardEntity.getNumber(), "Зашифрованное значение не должно совпадать с оригиналом");
        assertEquals(maskNumber, cardDto.getNumber(), "После расшифровки номер должен быть замаскирован");
    }

    @Test
    void testCreateCard_EncryptorIsUsed() {
        Long ownerId = 1L;

        UserEntity user = new UserEntity();
        user.setId(ownerId);

        when(repositoryHelper.findUserEntityByID(ownerId)).thenReturn(user);

        when(cardService.createCard(ownerId)).thenReturn(cardDto);

        when(cardEncryptorConverter.convertToDatabaseColumn(decryptedCardNumber))
                .thenReturn(encryptedCardNumber);

        when(cardRepository.save(any(CardEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ArgumentCaptor<CardEntity> captor = ArgumentCaptor.forClass(CardEntity.class);
        verify(cardRepository).save(captor.capture());

        assertEquals(ACTIVE, cardDto.getCardStatus());
        assertEquals(BigDecimal.ZERO, cardDto.getBalance());
        assertEquals(ownerId, cardDto.getOwnerID());

        verify(cardEncryptorConverter).convertToDatabaseColumn(anyString());
    }


    @Test
    void testActivateCard_Success() {
        when(repositoryHelper.findCardEntityByID(1L)).thenReturn(cardEntity);

        when(cardMapper.toDto(cardEntity)).thenReturn(cardDto);

        cardService.performOperation(1L, ACTIVATE);

        assertEquals(ACTIVE, cardEntity.getCardStatus());

        verify(repositoryHelper).findCardEntityByID(1L);
        verify(cardMapper).toDto(cardEntity);
    }

    @Test
    void testActivateCard_AlreadyActive_ThrowsException() {
        cardEntity.setCardStatus(ACTIVE);

        when(repositoryHelper.findCardEntityByID(1L)).thenReturn(cardEntity);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> cardService.performOperation(1L, ACTIVATE));

        assertEquals("Нельзя активировать карту с ID 1!", ex.getMessage());
        verify(repositoryHelper).findCardEntityByID(1L);
        verifyNoInteractions(cardMapper);
    }

    @Test
    void testActivateCard_AlreadyActive_DoesNotChangeStatus() {
        cardEntity.setCardStatus(ACTIVE);

        when(repositoryHelper.findCardEntityByID(1L)).thenReturn(cardEntity);

        CardStatus oldStatus = cardEntity.getCardStatus();

        assertThrows(IllegalArgumentException.class, () -> cardService.performOperation(1L, ACTIVATE));

        assertEquals(oldStatus, cardEntity.getCardStatus(), "Статус карты не должен измениться");

        verify(repositoryHelper).findCardEntityByID(1L);
        verifyNoInteractions(cardMapper);
    }

    @Test
    void testCreateCardWithRealEncryption() throws Exception {

        String originalNumber = "1234 5678 1234 5678";
        CardEntity card = new CardEntity();
        card.setNumber(originalNumber);

        String encrypted = cardEncryptorConverter.convertToDatabaseColumn(card.getNumber());
        card.setNumber(encrypted);

        String decrypted = cardEncryptorConverter.convertToEntityAttribute(card.getNumber());

        assertNotEquals(encrypted, decrypted, "Зашифрованный и дешифрованный номера должны отличаться");
        assertEquals(originalNumber, decrypted, "Дешифрованный номер должен совпадать с исходным");
    }


    @Test
    void testCreateCard_UserNotFound() {
        Long ownerId = 99L;
        when(repositoryHelper.findUserEntityByID(ownerId))
                .thenThrow(new EntityNotFoundException("User not found with id " + ownerId));

        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class,
                () -> cardService.createCard(ownerId));

        assertTrue(exception.getMessage().contains("User not found with id"));
    }
}
