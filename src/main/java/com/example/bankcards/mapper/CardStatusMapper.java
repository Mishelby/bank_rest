package com.example.bankcards.mapper;

import com.example.bankcards.dto.CardStatusRequestDto;
import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.CardStatusRequestEntity;
import com.example.bankcards.entity.UserEntity;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface CardStatusMapper {

    @Mapping(target = "userID", source = "status.ownerID")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "cardNumber", source = "card.number")
    CardStatusRequestDto toDto(CardStatusRequestEntity status, CardEntity card, UserEntity user);
}
