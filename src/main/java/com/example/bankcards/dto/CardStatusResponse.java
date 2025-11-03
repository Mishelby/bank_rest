package com.example.bankcards.dto;

import lombok.Builder;

@Builder
public record CardStatusResponse(
        Long cardID,
        Long ownerID,
        String message
) {
}
