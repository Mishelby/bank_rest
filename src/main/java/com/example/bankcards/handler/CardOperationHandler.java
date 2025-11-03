package com.example.bankcards.handler;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardOperation;

public interface CardOperationHandler {
    CardOperation getOperationType();
    void handle(CardEntity card);
}
