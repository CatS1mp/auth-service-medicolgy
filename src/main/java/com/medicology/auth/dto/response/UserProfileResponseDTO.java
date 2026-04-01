package com.medicology.auth.dto.response;

import com.medicology.auth.entity.UserProfile;

import java.util.UUID;

public record UserProfileResponseDTO(
        UUID userId,
        String displayName,
        String avatarUrl,
        String bio) {

    public static UserProfileResponseDTO fromEntity(UserProfile profile) {
        return new UserProfileResponseDTO(
                profile.getUserId(),
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                profile.getBio());
    }
}
