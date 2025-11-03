package com.example.bankcards.mapper;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.TransferInfoDto;
import com.example.bankcards.util.MaskCardNumber;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface CardMapper {

    @Mapping(target = "ownerID", source = "owner.id")
    @Mapping(target = "cardID", source = "id")
    @Mapping(target = "number", source = "cardEntity", qualifiedByName = "defaultMaskCardNumber")
    CardDto toDto(CardEntity cardEntity);

    @Mapping(target = "numberCardFrom", source = "cardFrom", qualifiedByName = "defaultMaskCardNumber")
    @Mapping(target = "numberCardTo", source = "cardTo", qualifiedByName = "defaultMaskCardNumber")
    @Mapping(target = "cardFromBalance", source = "cardFrom.balance")
    @Mapping(target = "cardBalanceTo", source = "cardTo.balance")
    @Mapping(target = "transferDate",  expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "amount", source = "amount")
    TransferInfoDto toTransferInfoDto(CardEntity cardFrom, CardEntity cardTo, BigDecimal amount);


    @Named("defaultMaskCardNumber")
    default String getMaskedCardNumber(CardEntity entity) {
        return MaskCardNumber.mask(entity.getNumber());
    }
}
