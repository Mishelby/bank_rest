package com.example.bankcards.entity.dto;

import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.CardStatus;

public record CardOperationRequest(
        Long cardID,
        CardOperation operation
) {
}
