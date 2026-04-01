package com.medicology.auth.service;

import com.medicology.auth.dto.response.UserOAuthAccountResponseDTO;
import com.medicology.auth.entity.User;
import com.medicology.auth.entity.UserOAuthAccount;
import com.medicology.auth.exception.ApiException;
import com.medicology.auth.repository.UserOAuthAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class OAuthAccountService {
    private final CurrentUserService currentUserService;
    private final UserOAuthAccountRepository userOAuthAccountRepository;

    public List<UserOAuthAccountResponseDTO> getCurrentLinkedAccounts(Authentication authentication) {
        return getLinkedAccounts(currentUserService.getCurrentUser(authentication));
    }

    public List<UserOAuthAccountResponseDTO> getLinkedAccounts(User user) {
        return userOAuthAccountRepository.findAllByUser(user).stream()
                .flatMap(account -> mapProviders(account).stream())
                .toList();
    }

    @Transactional
    public void unlinkCurrentProvider(Authentication authentication, String provider) {
        User user = currentUserService.getCurrentUser(authentication);
        UserOAuthAccount account = userOAuthAccountRepository.findByUser(user)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Không tìm thấy tài khoản OAuth liên kết."));

        String normalizedProvider = provider.toLowerCase(Locale.ROOT);
        switch (normalizedProvider) {
            case "google" -> account.setGoogleUserId(null);
            case "facebook" -> account.setFacebookUserId(null);
            default -> throw new ApiException(HttpStatus.BAD_REQUEST, "Provider không được hỗ trợ.");
        }

        boolean hasGoogle = account.getGoogleUserId() != null && !account.getGoogleUserId().isBlank();
        boolean hasFacebook = account.getFacebookUserId() != null && !account.getFacebookUserId().isBlank();
        boolean hasPassword = user.getPasswordHash() != null && !user.getPasswordHash().isBlank();

        if (!hasGoogle && !hasFacebook && !hasPassword) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Không thể gỡ liên kết phương thức đăng nhập cuối cùng của tài khoản.");
        }

        if (!hasGoogle && !hasFacebook) {
            userOAuthAccountRepository.delete(account);
            return;
        }

        userOAuthAccountRepository.save(account);
    }

    private List<UserOAuthAccountResponseDTO> mapProviders(UserOAuthAccount account) {
        List<UserOAuthAccountResponseDTO> providers = new ArrayList<>();

        if (account.getGoogleUserId() != null && !account.getGoogleUserId().isBlank()) {
            providers.add(new UserOAuthAccountResponseDTO("google", account.getGoogleUserId(), account.getProviderEmail()));
        }
        if (account.getFacebookUserId() != null && !account.getFacebookUserId().isBlank()) {
            providers.add(new UserOAuthAccountResponseDTO("facebook", account.getFacebookUserId(), account.getProviderEmail()));
        }

        return providers;
    }
}
