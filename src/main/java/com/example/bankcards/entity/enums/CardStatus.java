package com.example.bankcards.entity.enums;

import lombok.Getter;

@Getter
public enum CardStatus {
    ACTIVE,
    BLOCKED,
    DELETED,
    EXPIRED;

    public static CardStatus fromString(String value) {
        for (CardStatus cardStatus : CardStatus.values()) {
            if(cardStatus.name().equalsIgnoreCase(value)) {
                return cardStatus;
            }
        }

        throw new IllegalArgumentException("Incorrect CardStatus value: " + value);
    }
}
