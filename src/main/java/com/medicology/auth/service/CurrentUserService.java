package com.medicology.auth.service;

import com.medicology.auth.entity.User;
import com.medicology.auth.exception.ApiException;
import com.medicology.auth.repository.UserRepository;
import com.medicology.auth.wrapper.CustomUserDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurrentUserService {
    private final UserRepository userRepository;

    public void assertSelfOrAdmin(Authentication authentication, UUID targetUserId) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetail detail)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Người dùng chưa được xác thực.");
        }
        if (detail.getId().equals(targetUserId)) {
            return;
        }
        if (Boolean.TRUE.equals(detail.getIsAdmin())) {
            return;
        }
        throw new ApiException(HttpStatus.FORBIDDEN, "Không có quyền truy cập profile này.");
    }

    public User getCurrentUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetail userDetail)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Người dùng chưa được xác thực.");
        }

        return userRepository.findById(userDetail.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy người dùng hiện tại."));
    }
}
