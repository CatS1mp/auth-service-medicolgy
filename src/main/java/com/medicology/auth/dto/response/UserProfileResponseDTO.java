package com.medicology.auth.dto.response;

import com.medicology.auth.entity.User;
import com.medicology.auth.entity.UserProfile;

import java.time.LocalDate;
import java.util.UUID;

public record UserProfileResponseDTO(
        UUID userId,
        String email,
        String username,
        String lastName,
        String firstName,
        LocalDate dateOfBirth,
        String gender,
        String address,
        String displayName,
        String bio) {

    public static UserProfileResponseDTO fromEntities(User user, UserProfile profile) {
        return new UserProfileResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                profile != null ? profile.getLastName() : null,
                profile != null ? profile.getFirstName() : null,
                profile != null ? profile.getDateOfBirth() : null,
                profile != null ? profile.getGender() : null,
                profile != null ? profile.getAddress() : null,
                resolveDisplayName(profile),
                profile != null ? profile.getBio() : null);
    }

    private static String resolveDisplayName(UserProfile profile) {
        return buildFullName(profile);
    }

    private static String buildFullName(UserProfile profile) {
        if (profile == null) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        if (profile.getLastName() != null && !profile.getLastName().isBlank()) {
            builder.append(profile.getLastName().trim());
        }
        if (profile.getFirstName() != null && !profile.getFirstName().isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(profile.getFirstName().trim());
        }

        return builder.length() == 0 ? null : builder.toString();
    }
}
