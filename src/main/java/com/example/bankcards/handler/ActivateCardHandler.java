package com.example.bankcards.handler;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.CardStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.example.bankcards.entity.enums.CardOperation.ACTIVATE;
import static com.example.bankcards.entity.enums.CardStatus.ACTIVE;
import static com.example.bankcards.entity.enums.CardStatus.DELETED;

@Slf4j
@Component
public class ActivateCardHandler implements CardOperationHandler {

    @Override
    public CardOperation getOperationType() {
        return ACTIVATE;
    }

    /**
     * Активирует карту, если она не удалена и неактивна.
     *
     * @param card сущность карты
     * @throws IllegalArgumentException если карта уже активна или удалена
     */
    @Override
    public void handle(CardEntity card) {
        log.info("[INFO] Запрос на активацию карты с ID: [{}]", card.getId());

        if (ACTIVE == card.getCardStatus() || DELETED == card.getCardStatus()) {
            throw new IllegalArgumentException("Нельзя активировать карту с ID %s!".formatted(card.getId()));
        }

        card.setCardStatus(ACTIVE);
        log.info("[INFO] Карта с ID {} была активирована!", card.getId());
    }
}
