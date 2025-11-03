package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardOperation;
import com.example.bankcards.entity.enums.CardStatus;
import lombok.Builder;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
public record SpecificationData(
        CardStatus status,
        Long ownerID,
        LocalDate expirationDate,
        Boolean enabled,
        LocalDateTime createdDate,
        CardOperation statusRequest,
        Long cardID,
        LocalDateTime requestedAt
) {
}
