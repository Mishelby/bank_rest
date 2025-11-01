package com.example.bankcards.mapper;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.UserEntity;
import com.example.bankcards.entity.dto.CardDto;
import com.example.bankcards.entity.dto.TransferInfoDto;
import java.math.BigDecimal;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-11-01T12:07:04+0300",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 21.0.8 (Amazon.com Inc.)"
)
@Component
public class CardMapperImpl implements CardMapper {

    @Override
    public CardDto toDto(CardEntity cardEntity) {
        if ( cardEntity == null ) {
            return null;
        }

        CardDto cardDto = new CardDto();

        cardDto.setOwnerID( cardEntityOwnerId( cardEntity ) );
        cardDto.setCardID( cardEntity.getId() );
        cardDto.setNumber( getMaskedCardNumber( cardEntity ) );
        cardDto.setCardStatus( cardEntity.getCardStatus() );
        cardDto.setExpirationDate( cardEntity.getExpirationDate() );
        cardDto.setBalance( cardEntity.getBalance() );

        return cardDto;
    }

    @Override
    public TransferInfoDto toTransferInfoDto(CardEntity cardFrom, CardEntity cardTo, BigDecimal amount) {
        if ( cardFrom == null && cardTo == null && amount == null ) {
            return null;
        }

        TransferInfoDto.TransferInfoDtoBuilder transferInfoDto = TransferInfoDto.builder();

        if ( cardFrom != null ) {
            transferInfoDto.numberCardFrom( getMaskedCardNumber( cardFrom ) );
            transferInfoDto.cardFromBalance( cardFrom.getBalance() );
        }
        if ( cardTo != null ) {
            transferInfoDto.numberCardTo( getMaskedCardNumber( cardTo ) );
            transferInfoDto.cardBalanceTo( cardTo.getBalance() );
        }
        transferInfoDto.amount( amount );
        transferInfoDto.transferDate( java.time.LocalDateTime.now() );

        return transferInfoDto.build();
    }

    private Long cardEntityOwnerId(CardEntity cardEntity) {
        if ( cardEntity == null ) {
            return null;
        }
        UserEntity owner = cardEntity.getOwner();
        if ( owner == null ) {
            return null;
        }
        Long id = owner.getId();
        if ( id == null ) {
            return null;
        }
        return id;
    }
}
