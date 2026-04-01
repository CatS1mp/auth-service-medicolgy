package com.medicology.auth.service;

import com.medicology.auth.dto.response.UserSessionResponseDTO;
import com.medicology.auth.entity.RefreshToken;
import com.medicology.auth.entity.User;
import com.medicology.auth.exception.ApiException;
import com.medicology.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionManagementService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final CurrentUserService currentUserService;

    public List<UserSessionResponseDTO> getCurrentSessions(Authentication authentication) {
        return getActiveSessions(currentUserService.getCurrentUser(authentication));
    }

    public List<UserSessionResponseDTO> getActiveSessions(User user) {
        return refreshTokenRepository.findAllByUserIdAndRevokedFalseAndExpiresAtAfter(user.getId(), LocalDateTime.now()).stream()
                .map(UserSessionResponseDTO::fromEntity)
                .toList();
    }

    @Transactional
    public void revokeCurrentSession(Authentication authentication, UUID sessionId) {
        User user = currentUserService.getCurrentUser(authentication);
        RefreshToken refreshToken = refreshTokenRepository.findByIdAndUserId(sessionId, user.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy session cần thu hồi."));

        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
    }
}
