package com.example.bankcards.dto;

import com.example.bankcards.entity.enums.CardOperation;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CardStatusRequestDto(
        Long userID,
        String username,
        String cardNumber,
        CardOperation status,
        LocalDateTime requestedAt
) {
}
