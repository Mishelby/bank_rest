package com.example.bankcards.entity.dto;

import com.example.bankcards.entity.enums.CardStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CardDto {
    private Long cardID;
    private Long ownerID;
    private String number;
    private CardStatus cardStatus;
    private LocalDate expirationDate;
    private BigDecimal balance;
}
