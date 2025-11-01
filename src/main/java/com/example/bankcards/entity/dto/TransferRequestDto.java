package com.example.bankcards.entity.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransferRequestDto(
        Long fromCardId,
        Long toCardId,
        BigDecimal amount
) {
}
