package com.example.bankcards.entity.dto;

import lombok.Builder;

@Builder
public record LoginRequestDto(
        String username,
        String password
) {
}
