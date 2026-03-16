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
    private String avatarUrl;
    private String bio;

    static public UserProfileHeaderResponseDTO entityToDTO(UserProfile userProfile){
        if(userProfile == null) return null;
        return UserProfileHeaderResponseDTO.builder()
                .displayName(userProfile.getDisplayName())
                .avatarUrl(userProfile.getAvatarUrl())
                .bio(userProfile.getBio())
                .build();
    }
}
