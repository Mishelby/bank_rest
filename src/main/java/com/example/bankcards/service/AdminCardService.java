package com.example.bankcards.service;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.dto.CardDto;
import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.util.GenerateCardNumber;
import com.example.bankcards.util.RepositoryHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.example.bankcards.entity.enums.CardStatus.*;
import static com.example.bankcards.util.RepositoryHelper.getCardDtos;
import static com.example.bankcards.util.RepositoryHelper.getPageableSortingByAscID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCardService {
    private final CardRepository cardRepository;
    private final RepositoryHelper repositoryHelper;
    private final CardMapper cardMapper;

    public static final LocalDate EXPIRED_DATE = LocalDate.now().plusYears(10);

    @Transactional(readOnly = true)
    public List<CardDto> getAllCards(int page,
                                     int size,
                                     CardStatus status,
                                     Long ownerID,
                                     LocalDate expirationDate) {
        var pageable = getPageableSortingByAscID(page, size);
        Specification<CardEntity> spec = repositoryHelper.getSpecificationWithParams(
                status,
                ownerID,
                expirationDate
        );

        return getCardDtos(pageable, spec, repositoryHelper, cardMapper);
    }

    @Transactional(readOnly = true)
    public CardDto getCardById(Long cardID) {
        log.info("[INFO] Запрос на получение карты по ID: [{}]", cardID);
        return cardMapper.toDto(repositoryHelper.findCardEntityByID(cardID));
    }

    @Transactional
    public CardDto createCard(Long ownerId) {
        log.info("[INFO] Создаём карту для пользователя с ID: [{}]", ownerId);
        var userEntity = repositoryHelper.findUserEntityByID(ownerId);

        try {
            String cardNumber = GenerateCardNumber.generateCardNumber();

            var cardEntity = new CardEntity();
            cardEntity.setBalance(BigDecimal.ZERO);
            cardEntity.setOwner(userEntity);
            cardEntity.setNumber(cardNumber);
            cardEntity.setCardStatus(ACTIVE);
            cardEntity.setExpirationDate(EXPIRED_DATE);

            CardEntity savedCard = cardRepository.save(cardEntity);
            log.info("[INFO] Сохранённая сущность карты пользователя: [{}]", cardEntity);

            var cardDto = cardMapper.toDto(savedCard);
            log.info("[INFO] Карта для пользователя с ID: [{}], была успешно создана! [{}]", ownerId, cardDto);
            return cardDto;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public void performOperation(Long cardID, CardOperation operation) {
        var cardEntity = repositoryHelper.findCardEntityByID(cardID);

        switch (operation) {
            case ACTIVATE -> active(cardEntity);
            case BLOCK -> blockCard(cardEntity);
            case DELETE -> deleteCard(cardEntity);
            case DEEP_DELETE -> deepDeleteCard(cardEntity);
            default -> throw new IllegalArgumentException("Incorrect operation! " + operation);
        }
    }

    public void active(CardEntity cardEntity) {
        log.info("[INFO] Запрос на активацию карты с ID: [{}]", cardEntity.getId());

        if (BLOCKED != cardEntity.getCardStatus()) {
            throw new IllegalArgumentException("Нельзя активировать карту с ID %s!".formatted(cardEntity.getId()));
        }

        cardEntity.setCardStatus(CardStatus.ACTIVE);
        log.info("[INFO] Карта с ID {} была активирована!", cardEntity.getId());
    }

    public void blockCard(CardEntity cardEntity) {
        log.info("[INFO] Запрос на блокировку карты с ID: [{}]", cardEntity.getId());

        if (CardStatus.ACTIVE != cardEntity.getCardStatus()) {
            throw new IllegalArgumentException("Нельзя заблокировать карту с ID %s!".formatted(cardEntity.getId()));
        }

        cardEntity.setCardStatus(CardStatus.BLOCKED);
        log.info("[INFO] Карта с ID {} была заблокирована!", cardEntity.getId());
    }

    public void deleteCard(CardEntity cardEntity) {
        log.info("[INFO] Запрос на удаление карты с ID: [{}]", cardEntity.getId());

        if (DELETED == cardEntity.getCardStatus()) {
            throw new IllegalArgumentException("Нельзя удалить карту с ID %s!".formatted(cardEntity.getId()));
        }

        cardEntity.setCardStatus(DELETED);
        log.info("[INFO] Статус карты с ID {} был изменён на {}", cardEntity.getId(), DELETED);
    }

    public void deepDeleteCard(CardEntity cardEntity) {
        log.info("[INFO] Запрос на глубокое удаление карты с ID: [{}]", cardEntity.getId());

        if (BLOCKED == cardEntity.getCardStatus()) {
            throw new IllegalArgumentException("Нельзя удалить карту с ID %s!".formatted(cardEntity.getId()));
        }

        cardRepository.deleteById(cardEntity.getId());
        log.info("[INFO] Карта с ID [{}] была удалена полностью!", cardEntity.getId());
    }
}
