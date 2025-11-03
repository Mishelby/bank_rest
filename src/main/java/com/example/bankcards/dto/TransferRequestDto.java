package com.example.bankcards.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TransferRequestDto(
        @NotNull(message = "From card ID cannot be null") Long fromCardId,
        @NotNull(message = "To card ID cannot be null") Long toCardId,
        @NotNull(message = "Amount cannot be null")
        @DecimalMin(value = "0.01", inclusive = true, message = "Amount must be greater than zero") BigDecimal amount
) {}
