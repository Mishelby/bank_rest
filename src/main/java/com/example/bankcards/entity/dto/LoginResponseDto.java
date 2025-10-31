package com.example.bankcards.entity.dto;

import lombok.Builder;

@Builder
public record LoginResponseDto(
        String jwt,
        Long userId
) {
}
