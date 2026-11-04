package com.likelion.backend.domain.lizard.dto.response;

public record LizardListResponse(
        Long lizardId,
        String name,
        String species
) {
}
