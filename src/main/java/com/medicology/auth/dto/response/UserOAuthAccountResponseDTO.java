package com.medicology.auth.dto.response;

public record UserOAuthAccountResponseDTO(
        String provider,
        String providerUserId,
        String providerEmail) {
}
