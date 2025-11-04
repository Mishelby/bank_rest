package com.example.bankcards.handler;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.CardStatusException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static com.example.bankcards.entity.enums.CardOperation.DELETE;
import static com.example.bankcards.entity.enums.CardStatus.DELETED;

@Slf4j
@Component
public class DeleteCardHandler implements CardOperationHandler {
    @Override
    public CardOperation getOperationType() {
        return DELETE;
    }

    /**
     * Помечает карту как удалённую (меняет статус на {@link CardStatus#DELETED}).
     *
     * @param card сущность карты
     * @throws IllegalArgumentException если карта уже удалена
     */
    @Override
    public void handle(CardEntity card) {
        log.info("[INFO] Запрос на удаление карты с ID: [{}]", card.getId());

        if (DELETED == card.getCardStatus()) {
            throw new CardStatusException(
                    "Нельзя удалить карту с ID %s!".formatted(card.getId()),
                    "CARD_STATUS", HttpStatus.BAD_REQUEST.value()
            );
        }

        card.setCardStatus(DELETED);
        log.info("[INFO] Статус карты с ID {} был изменён на {}", card.getId(), DELETED);
    }
}
