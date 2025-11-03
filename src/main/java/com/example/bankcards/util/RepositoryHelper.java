package com.example.bankcards.util;

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
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.nonNull;


@Slf4j
@Component
@RequiredArgsConstructor
public class RepositoryHelper {
    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public UserEntity findUserEntityByID(Long userID) throws EntityNotFoundException {
        return userRepository.findById(userID).orElseThrow(
                () -> {
                    log.error("[ERROR] Пользователь с ID: [{}] не найден!", userID);
                    return new EntityNotFoundException("User not found with id %s: ".formatted(userID));
                }
        );
    }

    public CardEntity findCardEntityByID(Long cardID) throws EntityNotFoundException {
        return cardRepository.findById(cardID).orElseThrow(
                () -> {
                    log.error("[ERROR] Карта с ID: [{}] не найден!", cardID);
                    return new EntityNotFoundException("Card not found with ID: " + cardID);
                }
        );
    }

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

    public void isUserExists(Long userID) throws EntityNotFoundException {
        if (!userRepository.existsById(userID)) {
            throw new EntityNotFoundException("Пользователь с ID: [%s] не найден!".formatted(userID));
        }
    }

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

    public Page<CardEntity> getPageCardEntity(Specification<CardEntity> spec, Pageable pageable) {
        return cardRepository.findAll(spec, pageable);
    }

    public static Pageable getPageableSortingByAscID(int page, int size) {
        return PageRequest.of(page, size, Sort.by("id").ascending());
    }

    public Specification<CardEntity> getSpecificationWithParams(
            CardStatus status,
            Long ownerID,
            LocalDate expirationDate) {

        Specification<CardEntity> spec = Specification.unrestricted();
        if (nonNull(status)) {
            spec = spec.and((root, query, builder) ->
                    builder.and(builder.equal(root.get("cardStatus"), status)));
        }

        if (nonNull(ownerID)) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("owner").get("id"), ownerID));
        }

        if (nonNull(expirationDate)) {
            spec = spec.and((root, query, builder) ->
                    builder.equal(root.get("expirationDate"), expirationDate));
        }

        return spec;
    }
}
