package com.example.bankcards.dto;

import lombok.Builder;

@Builder
public record SignupResponseDto(
        Long userId,
        String username
) {
}
