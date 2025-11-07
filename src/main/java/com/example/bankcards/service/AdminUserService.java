package com.example.bankcards.service;

import com.example.bankcards.dto.SpecificationData;
import com.example.bankcards.dto.UserDto;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.mapper.UserMapper;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.util.RepositoryHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static com.example.bankcards.entity.enums.CardStatus.ACTIVE;
import static com.example.bankcards.util.RepositoryHelper.getPageableSortingByAscID;

/**
 * Сервис для работы с административными операциями над пользователями.
 * <p>
 * Предоставляет методы для получения списка пользователей с фильтрацией и пагинацией,
 * а также для получения информации о пользователе по его идентификатору.
 * Использует {@link UserRepository} для доступа к данным, {@link RepositoryHelper} для вспомогательных операций с репозиторием
 * и {@link UserMapper} для преобразования сущностей {@link UserEntity} в DTO {@link UserDto}.
 * </p>
 *
 * <p><b>Примечание:</b> Все методы помечены как read-only транзакции,
 * так как они не изменяют состояние базы данных.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminUserService {
    private final UserRepository userRepository;
    private final RepositoryHelper repositoryHelper;
    private final UserMapper userMapper;

    /**
     * Возвращает список пользователей с поддержкой пагинации, фильтрации и сортировки по возрастанию ID.
     *
     * @param page        номер страницы (начиная с 0)
     * @param size        количество элементов на странице
     * @param enabled     фильтр по доступности аккаунта пользователя (true/false);
     * @param createdDate фильтр по дате создания пользователя; может быть {@code null}, если фильтр не применяется
     * @return список DTO пользователей {@link UserDto}, удовлетворяющих условиям поиска;
     * пустой список, если пользователей не найдено
     */
    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers(int page,
                                     int size,
                                     Boolean enabled,
                                     LocalDateTime createdDate) {
        var pageable = getPageableSortingByAscID(page, size);
        var specificationData = SpecificationData.builder()
                .enabled(enabled)
                .createdDate(createdDate)
                .build();

        Specification<UserEntity> specificationWithParams
                = repositoryHelper.getSpecificationWithParams(specificationData);

        Page<UserEntity> allUsers = userRepository.findAll(specificationWithParams, pageable);

        if (allUsers.isEmpty()) {
            return Collections.emptyList();
        }

        return allUsers.getContent()
                .stream()
                .map(userMapper::toUserDto)
                .toList();
    }

    /**
     * Возвращает информацию о пользователе по его идентификатору.
     *
     * @param userID уникальный идентификатор пользователя
     * @return DTO пользователя {@link UserDto}
     * @throws EntityNotFoundException если пользователь с указанным ID не найден
     */
    @Transactional(readOnly = true)
    public UserDto getUserByID(Long userID) {
        return userMapper.toUserDto(
                repositoryHelper.findUserEntityByID(userID)
        );
    }

    /**
     * Удаляет пользователя по его идентификатору, если у него нет активных карт.
     * Перед удалением выполняется бизнес-проверка — наличие активных карт у пользователя.
     * Если хотя бы одна из карт имеет статус {@code ACTIVE},
     * операция удаления будет отклонена с выбрасыванием исключения {@link CardStatusException}.
     *
     * @param userID идентификатор пользователя, которого требуется удалить
     * @throws CardStatusException если у пользователя есть хотя бы одна активная карта
     * @throws EntityNotFoundException если пользователь с указанным ID не найден
     *
     * @see jakarta.transaction.Transactional
     * @see CardStatusException
     * @see org.springframework.http.HttpStatus#CONFLICT
     */
    @Transactional
    public void deleteUserByID(Long userID) {
        log.info("[INFO] Запрос на удаление пользователя по ID: {}", userID);
        var userEntityByID = repositoryHelper.findUserEntityByID(userID);
        if (userEntityByID.getCards().stream()
                .anyMatch(card -> ACTIVE == card.getCardStatus())) {
            throw new CardStatusException(
                    "Невозможно удалить пользователя с активными картами",
                    "USER_HAS_CARDS", HttpStatus.CONFLICT.value()
            );
        }
        userRepository.delete(userEntityByID);
        log.info("[INFO] Пользователь был удалён");
    }

}
