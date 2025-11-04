package com.example.bankcards.service;

import com.example.bankcards.dto.CardStatusRequestDto;
import com.example.bankcards.dto.SpecificationData;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatusRequestEntity;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.handler.CardOperationHandler;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.mapper.CardStatusMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRequestRepository;
import com.example.bankcards.util.GenerateCardNumber;
import com.example.bankcards.util.RepositoryHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.example.bankcards.entity.enums.CardStatus.*;
import static com.example.bankcards.util.RepositoryHelper.getCardDtos;
import static com.example.bankcards.util.RepositoryHelper.getPageableSortingByAscID;
import static java.util.Objects.isNull;

/**
 * Сервис для управления банковскими картами администратором.
 * Предоставляет операции для создания, получения, изменения статуса и удаления карт.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminCardService {
    private final CardRepository cardRepository;
    private final CardStatusRequestRepository statusRequestRepository;
    private final RepositoryHelper repositoryHelper;
    private final CardMapper cardMapper;

    /**
     * Срок действия карты по умолчанию — 10 лет.
     */
    @Value("${data.expired.value}")
    public int expiredValue;

    private final Map<CardOperation, CardOperationHandler> cardOperationsHandler;
    private final CardStatusMapper cardStatusMapper;

    /**
     * Возвращает список всех карт с возможностью фильтрации по статусу, владельцу и дате истечения срока действия.
     *
     * @param page           номер страницы (начиная с 0)
     * @param size           количество элементов на странице
     * @param status         статус карты (может быть {@code null})
     * @param ownerID        идентификатор владельца карты (может быть {@code null})
     * @param expirationDate дата истечения срока действия карты (может быть {@code null})
     * @return список {@link CardDto}, соответствующих фильтрам
     */
    @Transactional(readOnly = true)
    public List<CardDto> getAllCards(int page,
                                     int size,
                                     CardStatus status,
                                     Long ownerID,
                                     LocalDate expirationDate) {
        var pageable = getPageableSortingByAscID(page, size);
        var specificationData = SpecificationData.builder()
                .status(status)
                .ownerID(ownerID)
                .expirationDate(expirationDate)
                .build();

        Specification<CardEntity> spec = repositoryHelper.getSpecificationWithParams(specificationData);

        return getCardDtos(pageable, spec, repositoryHelper, cardMapper);
    }

    @Transactional(readOnly = true)
    public List<CardStatusRequestDto> getAllCardsRequests(int page,
                                                          int size,
                                                          CardOperation statusRequest,
                                                          Long ownerID,
                                                          Long cardID,
                                                          LocalDateTime requestedAt) {
        var pageable = getPageableSortingByAscID(page, size);
        var specificationData = SpecificationData.builder()
                .statusRequest(statusRequest)
                .ownerID(ownerID)
                .cardID(cardID)
                .requestedAt(requestedAt)
                .build();

        Specification<CardStatusRequestEntity> specificationWithParams =
                repositoryHelper.getSpecificationWithParams(specificationData);

        Page<CardStatusRequestEntity> pageStatus = statusRequestRepository.findAll(specificationWithParams, pageable);

        return pageStatus.stream().map(entity -> {
            var userEntityByID = repositoryHelper.findUserEntityByID(entity.getOwnerID());
            var cardEntityByID = repositoryHelper.findCardEntityByID(entity.getCardID());

            return cardStatusMapper.toDto(entity, cardEntityByID, userEntityByID);
        }).toList();
    }

    /**
     * Возвращает информацию о карте по её идентификатору.
     *
     * @param cardID идентификатор карты
     * @return {@link CardDto} с информацией о карте
     * @throws EntityNotFoundException если карта с указанным ID не найдена
     */
    @Transactional(readOnly = true)
    public CardDto getCardById(Long cardID) throws EntityNotFoundException {
        log.info("[INFO] Запрос на получение карты по ID: [{}]", cardID);
        return cardMapper.toDto(repositoryHelper.findCardEntityByID(cardID));
    }

    /**
     * Создаёт новую карту для указанного пользователя.
     *
     * @param ownerId идентификатор владельца карты
     * @return {@link CardDto} созданной карты
     * @throws EntityNotFoundException если пользователь с указанным ID не найден
     * @throws RuntimeException        если возникает ошибка при сохранении карты
     */
    @Transactional
    public CardDto createCard(Long ownerId) throws EntityNotFoundException {
        log.info("[INFO] Создаём карту для пользователя с ID: [{}]", ownerId);
        var userEntity = repositoryHelper.findUserEntityByID(ownerId);

        String cardNumber = GenerateCardNumber.generateCardNumber();

        var cardEntity = new CardEntity();
        cardEntity.setBalance(BigDecimal.ZERO);
        cardEntity.setOwner(userEntity);
        cardEntity.setNumber(cardNumber);
        cardEntity.setCardStatus(ACTIVE);
        cardEntity.setExpirationDate(LocalDate.now().plusYears(expiredValue));

        CardEntity savedCard = cardRepository.save(cardEntity);
        log.info("[INFO] Сохранённая сущность карты пользователя: [{}]", cardEntity);

        var cardDto = cardMapper.toDto(savedCard);
        log.info("[INFO] Карта для пользователя с ID: [{}], была успешно создана! [{}]", ownerId, cardDto);
        return cardDto;
    }

    /**
     * Выполняет указанную операцию над картой в зависимости от типа {@link CardOperation}.
     *
     * @param cardID идентификатор карты
     * @throws EntityNotFoundException  если карта с указанным ID не найдена
     * @throws IllegalArgumentException если операция некорректна или недопустима
     */
    //TODO переделать
    @Transactional
    public CardDto performOperation(Long cardID, CardOperation cardOperation) throws EntityNotFoundException {
        var cardOperationHandler = cardOperationsHandler.get(cardOperation);

        if (isNull(cardOperationHandler)) {
            throw new IllegalArgumentException("Некорректная операция %s".formatted(cardOperation));
        }

        var cardEntity = repositoryHelper.findCardEntityByID(cardID);
        cardOperationHandler.handle(cardEntity);

        return cardMapper.toDto(cardEntity);
    }


}
