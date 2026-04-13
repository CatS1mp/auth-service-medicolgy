package com.medicology.auth.service;

import com.medicology.auth.dto.request.UpdateProfileRequestDTO;
import com.medicology.auth.dto.response.UserProfileResponseDTO;
import com.medicology.auth.entity.User;
import com.medicology.auth.entity.UserProfile;
import com.medicology.auth.exception.ApiException;
import com.medicology.auth.repository.UserProfileRepository;
import com.medicology.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileService {
    private final CurrentUserService currentUserService;
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;

    public UserProfileResponseDTO getCurrentProfile(Authentication authentication) {
        return getProfileByUser(currentUserService.getCurrentUser(authentication));
    }

    public UserProfileResponseDTO getProfileByUserId(Authentication authentication, UUID userId) {
        currentUserService.assertSelfOrAdmin(authentication, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng."));
        return getProfileByUser(user);
    }

    public UserProfileResponseDTO getProfileByUser(User user) {
        return UserProfileResponseDTO.fromEntities(user, getOrCreateProfile(user));
    }

    @Transactional
    public UserProfileResponseDTO updateCurrentProfile(Authentication authentication, UpdateProfileRequestDTO request) {
        User user = currentUserService.getCurrentUser(authentication);
        return applyProfileUpdate(user, request);
    }

    @Transactional
    public UserProfileResponseDTO updateProfileByUserId(
            Authentication authentication, UUID userId, UpdateProfileRequestDTO request) {
        currentUserService.assertSelfOrAdmin(authentication, userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng."));
        return applyProfileUpdate(user, request);
    }

    private UserProfileResponseDTO applyProfileUpdate(User user, UpdateProfileRequestDTO request) {
        UserProfile profile = getOrCreateProfile(user);

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(request.username(), user.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Username đã được sử dụng.");
            }
            user.setUsername(request.username());
        }

        if (request.dateOfBirth() != null) {
            profile.setDateOfBirth(request.dateOfBirth());
        }

        if (request.lastName() != null) {
            profile.setLastName(request.lastName());
        }
        if (request.firstName() != null) {
            profile.setFirstName(request.firstName());
        }
        if (request.gender() != null) {
            profile.setGender(request.gender());
        }
        if (request.address() != null) {
            profile.setAddress(request.address());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio());
        }

        userRepository.save(user);
        UserProfile savedProfile = userProfileRepository.save(profile);
        return UserProfileResponseDTO.fromEntities(user, savedProfile);
    }

    private UserProfile getOrCreateProfile(User user) {
        return userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserProfile profile = new UserProfile();
                    profile.setUser(user);
                    user.setProfile(profile);
                    return userProfileRepository.save(profile);
                });
    }
}
