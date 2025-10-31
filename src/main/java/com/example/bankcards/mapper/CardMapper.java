package com.example.bankcards.mapper;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.dto.CardDto;
import com.example.bankcards.util.MaskCardNumber;
import org.mapstruct.*;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false))
public interface CardMapper {

    @Mapping(target = "ownerID", source = "owner.id")
    @Mapping(target = "cardID", source = "id")
    @Mapping(target = "number", source = "cardEntity", qualifiedByName = "defaultMaskCardNumber")
    CardDto toDto(CardEntity cardEntity);

    @Named("defaultMaskCardNumber")
    default String getMaskedCardNumber(CardEntity entity) {
        return MaskCardNumber.mask(entity.getNumber());
    }
}
