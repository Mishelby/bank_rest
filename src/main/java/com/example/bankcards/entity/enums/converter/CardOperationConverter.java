package com.example.bankcards.entity.enums.converter;

import com.example.bankcards.entity.enums.CardOperation;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import static java.util.Objects.nonNull;

@Converter(autoApply = true)
public class CardOperationConverter implements AttributeConverter<CardOperation, String> {
    @Override
    public String convertToDatabaseColumn(CardOperation cardOperation) {
        if (nonNull(cardOperation)) {
            return cardOperation.name();
        }

        return null;
    }

    @Override
    public CardOperation convertToEntityAttribute(String dbData) {
        if (nonNull(dbData)) {
            return CardOperation.fromString(dbData);
        }

        return null;
    }
}
