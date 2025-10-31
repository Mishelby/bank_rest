package com.example.bankcards.service;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatusRequestEntity;
import com.example.bankcards.repository.CardStatusRequestRepository;
import com.example.bankcards.entity.dto.CardDto;
import com.example.bankcards.entity.dto.CardStatusResponse;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.util.RepositoryHelper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static com.example.bankcards.entity.enums.CardOperation.BLOCK;
import static com.example.bankcards.entity.enums.CardStatus.ACTIVE;
import static com.example.bankcards.util.RepositoryHelper.getCardDtos;
import static com.example.bankcards.util.RepositoryHelper.getPageableSortingByAscID;


@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final RepositoryHelper repositoryHelper;
    private final CardStatusRequestRepository cardStatusRequestRepository;
    private final CardMapper cardMapper;

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


    @Transactional(readOnly = true)
    public CardDto findCardByID(Long userID, Long cardID) throws EntityNotFoundException {
        repositoryHelper.isUserExists(userID);
        return cardMapper.toDto(repositoryHelper.findCardEntityByID(cardID));
    }

    @Transactional
    public CardStatusResponse requestToBlockCard(Long userID, Long cardID){
        repositoryHelper.isUserExists(userID);
        var cardEntity = repositoryHelper.findCardEntityByID(cardID);
        CardStatusResponse.CardStatusResponseBuilder builder = CardStatusResponse.builder();

        if(!cardEntity.getOwner().getId().equals(userID)){
            throw new IllegalArgumentException("Карта не принадлежит данному пользователю!");
        }

        if(ACTIVE != cardEntity.getCardStatus()){
            throw new IllegalArgumentException("Нельзя заблокировать данную карту!");
        }

        CardStatusRequestEntity statusRequest = new CardStatusRequestEntity();
        statusRequest.setStatus(BLOCK);
        statusRequest.setOwnerID(userID);
        statusRequest.setCardID(cardID);
        CardStatusRequestEntity saved = cardStatusRequestRepository.save(statusRequest);

        return builder
                .ownerID(saved.getOwnerID())
                .cardID(saved.getCardID())
                .message("Заявка на блокировку отправлена!")
                .build();
    }

    @Transactional(readOnly = true)
    public BigDecimal findUserCardBalance(Long userID, Long cardID) throws EntityNotFoundException {
        repositoryHelper.isUserExists(userID);
        var cardEntity = repositoryHelper.findCardEntityByIDAndStatus(cardID, CardStatus.ACTIVE);

        return cardEntity.getBalance();
    }
}
