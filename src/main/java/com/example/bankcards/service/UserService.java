package com.example.bankcards.service;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatusRequestEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.dto.TransferInfoDto;
import com.example.bankcards.entity.dto.TransferRequestDto;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.CardStatusRequestRepository;
import com.example.bankcards.entity.dto.CardDto;
import com.example.bankcards.entity.dto.CardStatusResponse;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.util.RepositoryHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.enums.CardOperation.BLOCK;
import static com.example.bankcards.entity.enums.CardStatus.ACTIVE;
import static com.example.bankcards.util.RepositoryHelper.getCardDtos;
import static com.example.bankcards.util.RepositoryHelper.getPageableSortingByAscID;
import static java.util.Objects.isNull;

/**
 * Сервисный слой, предоставляющий операции для пользователей,
 * связанные с управлением их банковскими картами.
 * <p>
 * Пользователи могут получать список своих карт, запрашивать блокировку,
 * просматривать баланс и информацию по конкретной карте.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final RepositoryHelper repositoryHelper;
    private final CardStatusRequestRepository cardStatusRequestRepository;
    private final CardMapper cardMapper;

    /**
     * Возвращает список всех карт пользователя с возможностью фильтрации по статусу и дате истечения.
     *
     * @param page           номер страницы (начиная с 0)
     * @param size           количество элементов на странице
     * @param userID         идентификатор пользователя
     * @param status         статус карты (может быть {@code null})
     * @param expirationDate дата истечения срока действия карты (может быть {@code null})
     * @return список DTO карт пользователя
     * @throws EntityNotFoundException если пользователь с указанным {@code userID} не найден
     */
    @Transactional(readOnly = true)
    public List<CardDto> findAllUserCards(int page,
                                          int size,
                                          Long userID,
                                          CardStatus status,
                                          LocalDate expirationDate) throws EntityNotFoundException {
        repositoryHelper.isUserExists(userID);

        var pageable = getPageableSortingByAscID(page, size);
        Specification<CardEntity> spec = repositoryHelper.getSpecificationWithParams(
                status,
                userID,
                expirationDate
        );

        return getCardDtos(pageable, spec, repositoryHelper, cardMapper);
    }

    /**
     * Возвращает информацию о конкретной карте пользователя по её идентификатору.
     *
     * @param userID идентификатор пользователя
     * @param cardID идентификатор карты
     * @return DTO найденной карты
     * @throws EntityNotFoundException если пользователь или карта не найдены
     */
    @Transactional(readOnly = true)
    public CardDto findCardByID(
            Long userID,
            Long cardID) throws EntityNotFoundException {
        repositoryHelper.isUserExists(userID);
        return cardMapper.toDto(repositoryHelper.findCardEntityByID(cardID));
    }

    /**
     * Отправляет запрос на блокировку карты пользователя.
     * <p>
     * Проверяет, принадлежит ли карта пользователю, и имеет ли статус {@link CardStatus#ACTIVE}.
     * Если всё корректно — создаёт новую запись {@link CardStatusRequestEntity}.
     * </p>
     *
     * @param userID идентификатор пользователя
     * @param cardID идентификатор карты
     * @return объект {@link CardStatusResponse} с информацией о заявке
     * @throws IllegalArgumentException     если карта не принадлежит пользователю или имеет недопустимый статус
     * @throws ConstraintViolationException если нарушено ограничение уникальности при сохранении заявки
     * @throws EntityNotFoundException      если пользователь или карта не найдены
     */
    @Transactional
    public CardStatusResponse requestToBlockCard(
            Long userID,
            Long cardID) throws ConstraintViolationException {
        repositoryHelper.isUserExists(userID);
        var cardEntity = repositoryHelper.findCardEntityByID(cardID);

        if (!cardEntity.getOwner().getId().equals(userID)) {
            throw new IllegalArgumentException("Карта не принадлежит данному пользователю!");
        }

        if (ACTIVE != cardEntity.getCardStatus()) {
            throw new IllegalArgumentException("Нельзя заблокировать данную карту!");
        }

        var statusRequest = new CardStatusRequestEntity();
        statusRequest.setStatus(BLOCK);
        statusRequest.setOwnerID(userID);
        statusRequest.setCardID(cardID);
        CardStatusRequestEntity saved = cardStatusRequestRepository.save(statusRequest);

        return CardStatusResponse.builder()
                .ownerID(saved.getOwnerID())
                .cardID(saved.getCardID())
                .message("Заявка на блокировку отправлена!")
                .build();
    }

    /**
     * Возвращает текущий баланс активной карты пользователя.
     *
     * @param userID идентификатор пользователя
     * @param cardID идентификатор карты
     * @return текущий баланс карты
     * @throws EntityNotFoundException если пользователь не найден или карта отсутствует/не активна
     */
    @Transactional(readOnly = true)
    public BigDecimal findUserCardBalance(
            Long userID,
            Long cardID) throws EntityNotFoundException {
        repositoryHelper.isUserExists(userID);
        var cardEntity = repositoryHelper.findCardEntityByIDAndStatus(cardID, CardStatus.ACTIVE);

        return cardEntity.getBalance();
    }

    /**
     * Выполняет перевод средств между картами пользователя.
     *
     * @param userID             идентификатор пользователя
     * @param transferRequestDto DTO с данными перевода (ID карт и сумма)
     * @return {@link TransferInfoDto} — информация о совершённом переводе
     * @throws IllegalArgumentException если:
     *                                  <ul>
     *                                      <li>одна из карт неактивна</li>
     *                                      <li>карты не принадлежат пользователю</li>
     *                                      <li>недостаточно средств</li>
     *                                      <li>сумма перевода некорректна</li>
     *                                  </ul>
     */
    @Transactional
    public TransferInfoDto transferMoney(
            Long userID,
            TransferRequestDto transferRequestDto) {

        var amount = transferRequestDto.amount();

        if (isNull(amount) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[WARN] Некорректная сумма перевода: {}", amount);
            throw new IllegalArgumentException("Сумма перевода должна быть больше 0");
        }

        var cardFromByID = repositoryHelper.findCardEntityByID(transferRequestDto.fromCardId());
        var cardTobyID = repositoryHelper.findCardEntityByID(transferRequestDto.toCardId());

        if (ACTIVE != cardFromByID.getCardStatus() || ACTIVE != cardTobyID.getCardStatus()) {
            log.error("[ERROR] Одна из карт не активна: from={}, to={}",
                    cardFromByID.getCardStatus(), cardTobyID.getCardStatus());
            throw new IllegalArgumentException("Нельзя перевести средства! Одна из карт не активна");
        }
        var userEntityByID = repositoryHelper.findUserEntityByID(userID);

        if (!userEntityByID.getCards().contains(cardFromByID) || !userEntityByID.getCards().contains(cardTobyID)) {
            log.error("[ERROR] Ошибка! Одна из карт не принадлежит пользователю с ID {}, from={}, to={}",
                    userID,
                    cardFromByID.getId(),
                    cardTobyID.getId()
            );
            throw new IllegalArgumentException("Ошибка! Одна из карт не принадлежит пользователю!");
        }

        if (cardFromByID.getBalance().compareTo(amount) < 0) {
            log.warn("[WARN] Недостаточно средств: баланс={}, требуется={}",
                    cardFromByID.getBalance(), amount);
            throw new IllegalArgumentException("Недостаточно средств для перевода");
        }

        cardFromByID.setBalance(cardFromByID.getBalance().subtract(transferRequestDto.amount()));
        cardTobyID.setBalance(cardTobyID.getBalance().add(transferRequestDto.amount()));

        log.info("""
                        [INFO] Перевод выполнен успешно. userId={}, fromCard={}, toCard={}, amount={},
                        newBalances: {} -> {}, {} -> {}
                        """,
                userID,
                cardFromByID.getId(), cardTobyID.getId(),
                amount,
                cardFromByID.getId(), cardFromByID.getBalance(),
                cardTobyID.getId(), cardTobyID.getBalance()
        );

        return cardMapper.toTransferInfoDto(cardFromByID, cardTobyID, transferRequestDto.amount());
    }
}
