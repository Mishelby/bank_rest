package com.example.bankcards.handler;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.CardStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.example.bankcards.entity.enums.CardOperation.BLOCK;
import static com.example.bankcards.entity.enums.CardStatus.BLOCKED;

@Slf4j
@Component
public class BlockCardHandler implements CardOperationHandler {
    @Override
    public CardOperation getOperationType() {
        return BLOCK;
    }

    /**
     * Блокирует активную карту.
     *
     * @param card сущность карты
     * @throws IllegalArgumentException если карта не активна
     */
    @Override
    public void handle(CardEntity card) {
        log.info("[INFO] Запрос на блокировку карты с ID: [{}]", card.getId());

        if (CardStatus.ACTIVE != card.getCardStatus()) {
            throw new IllegalArgumentException("Нельзя заблокировать карту с ID %s!".formatted(card.getId()));
        }

        card.setCardStatus(BLOCKED);
        log.info("[INFO] Карта с ID {} была заблокирована!", card.getId());
    }
}
