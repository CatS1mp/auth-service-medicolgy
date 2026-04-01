package com.medicology.auth.dto.response;

import com.medicology.auth.entity.RefreshToken;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserSessionResponseDTO(
        UUID id,
        LocalDateTime createdAt,
        LocalDateTime expiresAt,
        Boolean revoked,
        String tokenPreview) {

    public static UserSessionResponseDTO fromEntity(RefreshToken token) {
        String rawToken = token.getToken();
        String preview = rawToken == null || rawToken.length() < 12
                ? rawToken
                : rawToken.substring(0, 12) + "...";

        return new UserSessionResponseDTO(
                token.getId(),
                token.getCreatedAt(),
                token.getExpiresAt(),
                token.getRevoked(),
                preview);
    }
}
