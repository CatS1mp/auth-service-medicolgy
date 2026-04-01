package com.medicology.auth.dto.response;

import com.medicology.auth.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        String username,
        LocalDate dateOfBirth,
        String location,
        Boolean active,
        Boolean verified,
        Boolean admin,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static UserResponseDTO fromEntity(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.getDateOfBirth(),
                user.getLocation(),
                user.getIsActive(),
                user.getIsVerified(),
                user.getIsAdmin(),
                user.getLastLoginAt(),
                user.getCreatedAt(),
                user.getUpdatedAt());
    }
}
