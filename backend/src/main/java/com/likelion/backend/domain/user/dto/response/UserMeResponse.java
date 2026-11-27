package com.likelion.backend.domain.user.dto.response;

public record UserMeResponse(
        Long userId,
        String username
) {
}