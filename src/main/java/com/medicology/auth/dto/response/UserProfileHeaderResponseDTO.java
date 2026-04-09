package com.medicology.auth.dto.response;


import com.medicology.auth.entity.UserProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class UserProfileHeaderResponseDTO {
    private String displayName;
    private String bio;

    static public UserProfileHeaderResponseDTO entityToDTO(UserProfile userProfile){
        if(userProfile == null) return null;
        return UserProfileHeaderResponseDTO.builder()
                .displayName(buildDisplayName(userProfile))
                .bio(userProfile.getBio())
                .build();
    }

    private static String buildDisplayName(UserProfile userProfile) {
        StringBuilder builder = new StringBuilder();
        if (userProfile.getLastName() != null && !userProfile.getLastName().isBlank()) {
            builder.append(userProfile.getLastName().trim());
        }
        if (userProfile.getFirstName() != null && !userProfile.getFirstName().isBlank()) {
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(userProfile.getFirstName().trim());
        }
        return builder.length() == 0 ? null : builder.toString();
    }
}
