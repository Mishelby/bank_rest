package com.example.bankcards.dto;

import lombok.Builder;

@Builder
public record LoginResponseDto(
        String jwt,
        Long userId
) {
}
