package com.example.bankcards.entity.enums;

import lombok.Getter;

@Getter
public enum CardOperation {
    ACTIVATE,
    BLOCK,
    DELETE,
    DEEP_DELETE ;

    public static CardOperation fromString(String value) {
        for (CardOperation cardOperation : CardOperation.values()) {
            if (cardOperation.name().equalsIgnoreCase(value)) {
                return cardOperation;
            }
        }

        throw new IllegalArgumentException("Invalid card operation value: " + value);
    }
}
