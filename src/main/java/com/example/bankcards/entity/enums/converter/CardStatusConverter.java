package com.example.bankcards.entity.enums.converter;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static java.util.Objects.nonNull;

@Converter(autoApply = true)
public class CardStatusConverter implements AttributeConverter<CardStatus, String> {
    @Override
    public String convertToDatabaseColumn(CardStatus cardStatus) {
        if(nonNull(cardStatus)){
            return cardStatus.name();
        }
        return null;
    }

    @Override
    public CardStatus convertToEntityAttribute(String dbValue) {
        if(nonNull(dbValue)){
            return CardStatus.fromString(dbValue);
        }
        return null;
    }
}
