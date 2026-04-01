package com.medicology.auth.service;

import com.medicology.auth.dto.request.SelfChangePasswordRequestDTO;
import com.medicology.auth.dto.request.UpdateUserRequestDTO;
import com.medicology.auth.dto.request.UpdateUserStatusRequestDTO;
import com.medicology.auth.dto.response.AdminUserDetailResponseDTO;
import com.medicology.auth.dto.response.UserOAuthAccountResponseDTO;
import com.medicology.auth.dto.response.UserProfileResponseDTO;
import com.medicology.auth.dto.response.UserResponseDTO;
import com.medicology.auth.dto.response.UserSessionResponseDTO;
import com.medicology.auth.dto.response.UserSettingResponseDTO;
import com.medicology.auth.entity.User;
import com.medicology.auth.exception.ApiException;
import com.medicology.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final PasswordEncoder passwordEncoder;
    private final UserProfileService userProfileService;
    private final UserSettingService userSettingService;
    private final OAuthAccountService oauthAccountService;
    private final SessionManagementService sessionManagementService;

    public UserResponseDTO getCurrentUser(Authentication authentication) {
        return UserResponseDTO.fromEntity(currentUserService.getCurrentUser(authentication));
    }

    @Transactional
    public UserResponseDTO updateCurrentUser(Authentication authentication, UpdateUserRequestDTO request) {
        User user = currentUserService.getCurrentUser(authentication);

        if (request.username() != null && !request.username().equals(user.getUsername())) {
            if (userRepository.existsByUsernameAndIdNot(request.username(), user.getId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Username đã được sử dụng.");
            }
            user.setUsername(request.username());
        }

        if (request.dateOfBirth() != null) {
            user.setDateOfBirth(request.dateOfBirth());
        }

        if (request.location() != null) {
            user.setLocation(request.location());
        }

        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    @Transactional
    public void changeCurrentUserPassword(Authentication authentication, SelfChangePasswordRequestDTO request) {
        User user = currentUserService.getCurrentUser(authentication);

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tài khoản OAuth chưa có mật khẩu để thay đổi.");
        }
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu mới và xác nhận mật khẩu mới không khớp.");
        }
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Mật khẩu hiện tại không đúng.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public List<UserResponseDTO> getAdminUsers(Boolean active) {
        List<User> users = active == null ? userRepository.findAll() : userRepository.findAllByIsActive(active);
        return users.stream()
                .map(UserResponseDTO::fromEntity)
                .toList();
    }

    public AdminUserDetailResponseDTO getAdminUserDetail(UUID userId) {
        User user = findUserById(userId);
        UserProfileResponseDTO profile = userProfileService.getProfileByUser(user);
        UserSettingResponseDTO settings = userSettingService.getSettingsByUser(user);
        List<UserOAuthAccountResponseDTO> linkedAccounts = oauthAccountService.getLinkedAccounts(user);
        List<UserSessionResponseDTO> sessions = sessionManagementService.getActiveSessions(user);

        return new AdminUserDetailResponseDTO(
                UserResponseDTO.fromEntity(user),
                profile,
                settings,
                linkedAccounts,
                sessions);
    }

    @Transactional
    public UserResponseDTO updateUserStatus(UUID userId, UpdateUserStatusRequestDTO request) {
        User user = findUserById(userId);
        user.setIsActive(request.active());
        return UserResponseDTO.fromEntity(userRepository.save(user));
    }

    public User findUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng."));
    }
}
