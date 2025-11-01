package com.example.bankcards.entity.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record TransferInfoDto(
        LocalDateTime transferDate,
        String numberCardFrom,
        BigDecimal cardFromBalance,
        BigDecimal amount,
        String numberCardTo,
        BigDecimal cardBalanceTo
) {
}
