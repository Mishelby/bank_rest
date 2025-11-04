package com.example.bankcards.service;

import com.example.bankcards.dto.*;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatusRequestEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.exception.InsufficientFundsException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.CardStatusRequestRepository;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.RepositoryHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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
    private final UserMapper userMapper;
    private final UserRepository userRepository;

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

        var specificationData = SpecificationData.builder()
                .status(status)
                .expirationDate(expirationDate)
                .ownerID(userID).build();
        Specification<CardEntity> spec = repositoryHelper.getSpecificationWithParams(specificationData);

        return getCardDtos(pageable, spec, repositoryHelper, cardMapper);
    }

    /**
     * Находит пользователя по его уникальному идентификатору и преобразует его в {@link UserDto}.
     * <p>
     * Метод выполняется в режиме только для чтения.
     * Если пользователь с указанным ID не найден, будет выброшено исключение {@link EntityNotFoundException}.
     *
     * @param userID уникальный идентификатор пользователя
     * @return {@link UserDto}, представляющий пользователя с указанным ID
     * @throws EntityNotFoundException если пользователь с указанным ID не найден
     */
    @Transactional(readOnly = true)
    public UserDto findUserByID(Long userID) throws EntityNotFoundException {
        return userMapper.toUserDto(
                repositoryHelper.findUserEntityByID(userID)
        );
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
        var userEntityByID = repositoryHelper.findUserEntityByID(userID);
        var cardEntityByID = repositoryHelper.findCardEntityByID(cardID);

        if (!userEntityByID.getCards().contains(cardEntityByID)) {
            log.warn("Карта с ID: {} не принадлежит пользователю с ID {}", cardID, cardEntityByID);
            throw new CardStatusException(
                    "Карта не принадлежит данному пользователю!", "CARD_STATUS", HttpStatus.BAD_REQUEST.value()
            );
        }

        return cardMapper.toDto(cardEntityByID);
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
            throw new CardStatusException(
                    "Карта не принадлежит данному пользователю!", "CARD_STATUS", HttpStatus.BAD_REQUEST.value()
            );
        }

        if (ACTIVE != cardEntity.getCardStatus()) {
            throw new CardStatusException(
                    "Нельзя заблокировать данную карту!", "CARD_STATUS", HttpStatus.BAD_REQUEST.value()
            );
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
     * Выполняет перевод денежных средств между двумя картами пользователя в рамках одной транзакции.
     * Для предотвращения конкурентных изменений балансов используется
     * блокировка записей в базе данных с режимом {@code PESSIMISTIC_WRITE}
     * при извлечении сущностей карт (через {@code repositoryHelper.findCardEntityByIDAndLockModeType()}).
     * Это гарантирует, что одновременные транзакции не смогут изменить одни и те же карты до завершения текущей.
     *
     * <p><b>Особенности:</b></p>
     * <ul>
     *   <li>Метод потокобезопасен за счёт row-level locking в базе данных.</li>
     *   <li>При любой ошибке (например, недостаточно средств или невалидные карты)
     *       транзакция будет откатана.</li>
     * </ul>
     *
     * @param userID               идентификатор пользователя, выполняющего перевод
     * @param transferRequestDto   DTO с параметрами перевода (ID карт и сумма)
     * @return DTO с информацией об успешном переводе
     * @throws IllegalArgumentException если сумма некорректна
     * @throws EntityNotFoundException  если одна из карт или пользователь не найдены
     * @throws InsufficientFundsException если на карте отправителя недостаточно средств
     * @throws CardStatusException если одна из карт имеет неподходящий статус
     *
     * @see jakarta.transaction.Transactional
     * @see org.springframework.data.jpa.repository.Lock
     * @see jakarta.persistence.LockModeType#PESSIMISTIC_WRITE
     */
    @Transactional
    public TransferInfoDto transferMoney(
            Long userID,
            TransferRequestDto transferRequestDto) {

        isAmountValid(transferRequestDto.amount());
        var amount = transferRequestDto.amount();

        var cardFromByID = repositoryHelper.findCardEntityByIDAndLockModeType(transferRequestDto.fromCardId());
        var cardTobyID = repositoryHelper.findCardEntityByIDAndLockModeType(transferRequestDto.toCardId());

        isCardStatusValid(cardFromByID, cardTobyID);

        var userEntityByID = repositoryHelper.findUserEntityByID(userID);

        isUserContainsCards(userID, userEntityByID, cardFromByID, cardTobyID);
        isEnoughAmount(cardFromByID, amount);

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

    private static void isEnoughAmount(CardEntity cardFromByID, BigDecimal amount) {
        if (cardFromByID.getBalance().compareTo(amount) < 0) {
            log.warn("[WARN] Недостаточно средств: баланс={}, требуется={}",
                    cardFromByID.getBalance(), amount);
            throw new InsufficientFundsException(
                    "Недостаточно средств для перевода", "INSUFFICIENT_FUNDS", HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    private static void isUserContainsCards(Long userID,
                                            UserEntity userEntityByID,
                                            CardEntity cardFromByID,
                                            CardEntity cardTobyID) {
        if (!userEntityByID.getCards().contains(cardFromByID) || !userEntityByID.getCards().contains(cardTobyID)) {
            log.error("[ERROR] Ошибка! Одна из карт не принадлежит пользователю с ID {}, from={}, to={}",
                    userID,
                    cardFromByID.getId(),
                    cardTobyID.getId()
            );
            throw new CardStatusException(
                    "Ошибка! Одна из карт не принадлежит пользователю!", "CARD_STATUS", HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    private static void isCardStatusValid(CardEntity cardFromByID, CardEntity cardTobyID) {
        if (ACTIVE != cardFromByID.getCardStatus() || ACTIVE != cardTobyID.getCardStatus()) {
            log.error("[ERROR] Одна из карт не активна: from={}, to={}",
                    cardFromByID.getCardStatus(), cardTobyID.getCardStatus());
            throw new CardStatusException(
                    "Нельзя перевести средства! Одна из карт не активна", "CARD_STATUS", HttpStatus.BAD_REQUEST.value()
            );
        }
    }

    private static void isAmountValid(BigDecimal amount) {
        if (isNull(amount) || amount.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("[WARN] Некорректная сумма перевода: {}", amount);
            throw new IllegalArgumentException("Сумма перевода должна быть больше 0");
        }
    }
}
