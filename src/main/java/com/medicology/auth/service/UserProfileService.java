package com.medicology.auth.service;

import com.medicology.auth.dto.request.UpdateProfileRequestDTO;
import com.medicology.auth.dto.response.UserProfileResponseDTO;
import com.medicology.auth.entity.User;
import com.medicology.auth.entity.UserProfile;
import com.medicology.auth.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final CurrentUserService currentUserService;
    private final UserProfileRepository userProfileRepository;

    public UserProfileResponseDTO getCurrentProfile(Authentication authentication) {
        return getProfileByUser(currentUserService.getCurrentUser(authentication));
    }

    public UserProfileResponseDTO getProfileByUser(User user) {
        return UserProfileResponseDTO.fromEntity(getOrCreateProfile(user));
    }

    @Transactional
    public UserProfileResponseDTO updateCurrentProfile(Authentication authentication, UpdateProfileRequestDTO request) {
        User user = currentUserService.getCurrentUser(authentication);
        UserProfile profile = getOrCreateProfile(user);

        if (request.displayName() != null) {
            profile.setDisplayName(request.displayName());
        }
        if (request.avatarUrl() != null) {
            profile.setAvatarUrl(request.avatarUrl());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio());
        }

        return UserProfileResponseDTO.fromEntity(userProfileRepository.save(profile));
    }

    private UserProfile getOrCreateProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setUser(user);
                    profile.setDisplayName(user.getUsername());
                    return userProfileRepository.save(profile);
                });
    }
}
