package com.example.bankcards.util;

import com.example.bankcards.dto.SpecificationData;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;

/**
 * Утилитный Spring-компонент, содержащий вспомогательные методы для работы с репозиториями
 * {@link CardRepository} и {@link UserRepository}.
 * <p>
 * Класс предназначен для:
 * <ul>
 *     <li>поиска сущностей {@link CardEntity} и {@link UserEntity} с обработкой ошибок;</li>
 *     <li>формирования динамических {@link Specification} для фильтрации данных;</li>
 *     <li>построения {@link Pageable} и пагинации;</li>
 *     <li>преобразования страниц сущностей в DTO-объекты.</li>
 * </ul>
 *
 * <p>Класс аннотирован {@link Component}, поэтому может быть внедрён через Spring DI.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RepositoryHelper {
    /**
     * Репозиторий для работы с картами.
     */
    private final CardRepository cardRepository;

    /**
     * Репозиторий для работы с пользователями.
     */
    private final UserRepository userRepository;

    /**
     * Находит пользователя по его идентификатору.
     *
     * @param userID идентификатор пользователя
     * @return сущность {@link UserEntity}, если найдена
     * @throws EntityNotFoundException если пользователь не найден
     */
    public UserEntity findUserEntityByID(Long userID) throws EntityNotFoundException {
        return userRepository.findById(userID).orElseThrow(
                () -> {
                    log.error("[ERROR] Пользователь с ID: [{}] не найден!", userID);
                    return new EntityNotFoundException("User not found with id %s: ".formatted(userID));
                }
        );
    }

    /**
     * Находит карту по её идентификатору.
     *
     * @param cardID идентификатор карты
     * @return сущность {@link CardEntity}, если найдена
     * @throws EntityNotFoundException если карта не найдена
     */
    public CardEntity findCardEntityByID(Long cardID) throws EntityNotFoundException {
        return cardRepository.findById(cardID).orElseThrow(
                () -> {
                    log.error("[ERROR] Карта с ID: [{}] не найден!", cardID);
                    return new EntityNotFoundException("Card not found with ID: " + cardID);
                }
        );
    }

    /**
     * Находит карту по ID с блокировкой для обновления (использует {@code FOR UPDATE}).
     *
     * @param cardID идентификатор карты
     * @return сущность {@link CardEntity}
     * @throws EntityNotFoundException если карта не найдена
     */
    public CardEntity findCardEntityByIDAndLockModeType(Long cardID) throws EntityNotFoundException {
        return cardRepository.findCardForUpdate(cardID).orElseThrow(
                () -> {
                    log.error("[ERROR] Карта с ID: [{}] не найден!", cardID);
                    return new EntityNotFoundException("Card not found with ID: " + cardID);
                }
        );
    }

    /**
     * Находит карту по ID и статусу.
     *
     * @param cardID идентификатор карты
     * @param status статус карты (например, {@link CardStatus#ACTIVE})
     * @return сущность {@link CardEntity}
     * @throws EntityNotFoundException если карта с указанным ID и статусом не найдена
     */
    public CardEntity findCardEntityByIDAndStatus(Long cardID, CardStatus status) {
        return cardRepository.findCardEntityByIDAndStatus(cardID, status).orElseThrow(
                () -> {
                    log.error("[ERROR] Карта с ID: [{}] и статусом [{}] не найдена!", cardID, status.name());
                    return new EntityNotFoundException("Card with ID %s and status %s not found!: "
                            .formatted(cardID, status.name())
                    );
                }
        );
    }

    /**
     * Проверяет существование пользователя по его ID.
     *
     * @param userID идентификатор пользователя
     * @throws EntityNotFoundException если пользователь не существует
     */
    public void isUserExists(Long userID) throws EntityNotFoundException {
        if (!userRepository.existsById(userID)) {
            throw new EntityNotFoundException("Пользователь с ID: [%s] не найден!".formatted(userID));
        }
    }

    /**
     * Преобразует страницу сущностей {@link CardEntity} в список {@link CardDto}.
     *
     * @param pageable         параметры пагинации
     * @param spec             спецификация для фильтрации данных
     * @param repositoryHelper экземпляр {@link RepositoryHelper}
     * @param cardMapper       маппер для преобразования {@link CardEntity} → {@link CardDto}
     * @return список DTO-объектов карт
     */
    public static List<CardDto> getCardDtos(Pageable pageable,
                                            Specification<CardEntity> spec,
                                            RepositoryHelper repositoryHelper,
                                            CardMapper cardMapper) {
        Page<CardEntity> allCards = repositoryHelper.getPageCardEntity(spec, pageable);
        if (allCards.hasContent()) {
            List<CardEntity> list = allCards.getContent();
            return list.stream().map(cardMapper::toDto).toList();
        }

        return Collections.emptyList();
    }

    /**
     * Возвращает страницу сущностей {@link CardEntity} согласно спецификации и пагинации.
     *
     * @param spec     спецификация фильтрации
     * @param pageable параметры пагинации
     * @return объект {@link Page} с картами
     */
    public Page<CardEntity> getPageCardEntity(Specification<CardEntity> spec, Pageable pageable) {
        return cardRepository.findAll(spec, pageable);
    }

    /**
     * Создаёт {@link Pageable}, сортируя результаты по возрастанию поля {@code id}.
     *
     * @param page номер страницы (начиная с 0)
     * @param size количество элементов на странице
     * @return объект {@link Pageable} с сортировкой по возрастанию ID
     */
    public static Pageable getPageableSortingByAscID(int page, int size) {
        return PageRequest.of(page, size, Sort.by("id").ascending());
    }

    /**
     * Формирует {@link Specification} для {@link CardEntity} на основе переданных параметров фильтрации.
     * <p>
     * Если какое-то из значений в {@link SpecificationData} равно {@code null}, оно просто пропускается.
     *
     * @param data параметры фильтрации (статус карты, владелец, дата истечения и т.д.)
     * @return объект {@link Specification}, который можно передать в {@link JpaSpecificationExecutor}
     */
    public <T> Specification<T> getSpecificationWithParams(SpecificationData data) {
        Specification<T> spec = Specification.unrestricted();

        if (nonNull(data.status())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("cardStatus"), data.status()));
        }

        if (nonNull(data.enabled())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("enabled"), data.enabled()));
        }

        if (nonNull(data.createdDate())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("createdDate"), data.createdDate()));
        }

        if (nonNull(data.ownerID())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("owner").get("id"), data.ownerID()));
        }

        if (nonNull(data.expirationDate())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("expirationDate"), data.expirationDate()));
        }

        if (nonNull(data.cardID())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("cardID"), data.cardID()));
        }

        if (nonNull(data.statusRequest())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("status"), data.statusRequest()));
        }

        if (nonNull(data.requestedAt())) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("requestedAt"), data.requestedAt()));
        }

        return spec;
    }
}
