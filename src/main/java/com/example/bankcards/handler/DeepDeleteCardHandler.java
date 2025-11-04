package com.example.bankcards.handler;

import com.example.bankcards.entity.CardEntity;
import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.exception.CardStatusException;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import static com.example.bankcards.entity.enums.CardOperation.DEEP_DELETE;
import static com.example.bankcards.entity.enums.CardStatus.BLOCKED;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeepDeleteCardHandler implements CardOperationHandler {
    private final CardRepository cardRepository;

    @Override
    public CardOperation getOperationType() {
        return DEEP_DELETE;
    }

    /**
     * Полностью удаляет карту из базы данных.
     *
     * @param card сущность карты
     * @throws IllegalArgumentException если карта заблокирована
     */
    @Override
    public void handle(CardEntity card) {
        log.info("[INFO] Запрос на глубокое удаление карты с ID: [{}]", card.getId());

        if (BLOCKED == card.getCardStatus()) {
            throw new CardStatusException(
                    "Нельзя удалить карту с ID %s!".formatted(card.getId()),
                    "CARD_STATUS", HttpStatus.BAD_REQUEST.value()
            );
        }

        cardRepository.deleteById(card.getId());
        log.info("[INFO] Карта с ID [{}] была удалена полностью!", card.getId());
    }
}
