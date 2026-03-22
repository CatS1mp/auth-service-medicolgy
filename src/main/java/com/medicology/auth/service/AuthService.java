package com.medicology.auth.service;

import com.medicology.auth.repository.ResetTokenRepository;
import com.medicology.auth.repository.UserRepository;
import com.medicology.auth.repository.VerificationTokenRepository;
import com.medicology.auth.repository.UserOAuthAccountRepository;
import com.medicology.auth.repository.UserProfileRepository;
import com.medicology.auth.repository.RefreshTokenRepository;

import com.medicology.auth.entity.UserOAuthAccount;
import com.medicology.auth.entity.UserProfile;
import com.medicology.auth.entity.VerificationToken;
import com.medicology.auth.entity.ResetToken;
import com.medicology.auth.entity.User;
import com.medicology.auth.entity.RefreshToken;

import com.medicology.auth.dto.request.*;
import com.medicology.auth.dto.response.*;

import com.medicology.auth.security.jwt.JWTTokenProvider;

import java.util.UUID;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    @Value("${verify.token.expiration}")
    private Long verifyTokenExpiration;
    @Value("${reset.token.expiration}")
    private Long resetTokenExpiration;
    @Value("${access.token.expiration}")
    private Long accessTokenExpiration;

    private final UserRepository userRepository;
    private final UserOAuthAccountRepository userOAuthAccountRepository;
    private final UserProfileRepository userProfileRepository;
    private final VerificationTokenRepository tokenRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JWTTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public LoginResponseDTO login(LoginRequestDTO request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Email hoặc mật khẩu không đúng");
        }
        User user = userRepository.findByEmail(request.email()).get();
        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin..."));
        return LoginResponseDTO.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user))
                .expiresIn(accessTokenExpiration / 1000)
                // Gọi hàm static để convert cực kỳ gọn gàng
                .userProfile(UserProfileHeaderResponseDTO.entityToDTO(profile))
                .build();
    }

    @Transactional
    public LoginResponseDTO processOAuthPostLogin(OAuthRequestDTO request) {
        User user = userRepository.findByEmail(request.email())
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(request.email());
                    newUser.setUsername(request.name());
                    newUser.setIsVerified(true);
                    newUser.setPasswordHash(null);

                    return userRepository.save(newUser);
                });

        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUserId(user.getId());
                    newProfile.setDisplayName(request.name());
                    newProfile.setUser(user);
                    return userProfileRepository.save(newProfile);
                });

        UserOAuthAccount oauthAccount = userOAuthAccountRepository.findByUser(user)
                .orElseGet(() -> {
                    UserOAuthAccount newAccount = new UserOAuthAccount();
                    newAccount.setUser(user);
                    return newAccount;
                });

        if (request.googleId() != null) {
            oauthAccount.setGoogleUserId(request.googleId());
        }
        if (request.facebookId() != null) {
            oauthAccount.setFacebookUserId(request.facebookId());
        }

        userOAuthAccountRepository.save(oauthAccount);

        // 2. Trả về Response
        return LoginResponseDTO.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user))
                .expiresIn(accessTokenExpiration / 1000)
                // Gọi hàm static để convert cực kỳ gọn gàng
                .userProfile(UserProfileHeaderResponseDTO.entityToDTO(profile))
                .build();
    }

    @Transactional
    public RegisterResponseDTO registerNewUser(RegisterRequestDTO request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu không khớp");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Người dùng với email " + request.email() + " đã tồn tại");
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Người dùng với tên đăng nhập " + request.username() + " đã tồn tại");
        }
        // Tạo user
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);

        // Tạo token
        UUID token = UUID.randomUUID();
        VerificationToken verificationToken = new VerificationToken(token, user);
        tokenRepository.save(verificationToken);

        // Gửi mail
        emailService.sendVerificationEmail(user.getEmail(), token, "verify");
        return RegisterResponseDTO.builder()
                .email(user.getEmail())
                .build();
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tìm thấy với email: " + email));

        if (user.getIsVerified()) {
            throw new IllegalArgumentException("Email đã được xác minh");
        }
        // Tạo token mới
        UUID token = UUID.randomUUID();
        VerificationToken verificationToken = tokenRepository.findByUserId(user.getId())
                .map(existingToken -> {
                    existingToken.setToken(token);
                    existingToken.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(verifyTokenExpiration));
                    return existingToken;
                })
                .orElseGet(() -> new VerificationToken(token, user));
        tokenRepository.save(verificationToken);

        // Gửi mail
        emailService.sendVerificationEmail(user.getEmail(), token, "verify");
    }

    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tìm thấy với email: " + email));
        if (!user.getIsVerified()) {
            throw new IllegalArgumentException("Email chưa được xác minh");
        }

        // Tạo token mới
        UUID token = UUID.randomUUID();
        ResetToken resetToken = resetTokenRepository.findByUserId(user.getId())
                .map(existingToken -> {
                    existingToken.setToken(token);
                    existingToken.setExpiryDate(java.time.LocalDateTime.now().plusMinutes(resetTokenExpiration));
                    return existingToken;
                })
                .orElseGet(() -> new ResetToken(token, user));
        resetTokenRepository.save(resetToken);

        // Gửi mail
        emailService.sendVerificationEmail(user.getEmail(), token, "reset");
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        ResetToken resetToken = resetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (resetToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset token has expired");
        }
        if (!request.newPassword().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu mới không khớp");
        }

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetTokenRepository.delete(resetToken); // Xóa token sau khi reset password thành công
    }

    @Transactional
    public void changePassword(ChangePasswordRequestDTO request) {
        if (!request.newPassword().equals(request.confirmNewPassword())) {
            throw new IllegalArgumentException("Mật khẩu mới và xác nhận mật khẩu mới không khớp");
        }
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(
                        () -> new IllegalArgumentException("Người dùng không tìm thấy với email: " + request.email()));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không đúng");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    public void verifyEmail(UUID token) {

        VerificationToken verificationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));

        if (verificationToken.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Token đã hết hạn");
        }
        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        if (!userProfileRepository.existsById(user.getId())) {
            UserProfile userProfile = new UserProfile();
            userProfile.setDisplayName(user.getUsername());
            userProfile.setUser(user);
            userProfileRepository.save(userProfile);
        }

        tokenRepository.delete(verificationToken); // Xóa token sau khi xác thực thành công

    }

    public boolean checkVerificationStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Người dùng không tìm thấy với email: " + email));
        return user.getIsVerified();
    }

    public boolean checkResetTokenValidity(UUID token) { // Khi ấn vào token gọi api này để check token còn hạn không,
                                                         // nếu còn hạn thì cho reset password, hết hạn thì bắt tạo lại
                                                         // token
        ResetToken resetToken = resetTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token không hợp lệ"));
        return resetToken.getExpiryDate().isAfter(java.time.LocalDateTime.now());
    }

    @Transactional
    public void logout(String authHeader, LogoutRequestDTO request) {
        if (request != null && request.refreshToken() != null) {
            // Tìm RefreshToken trong DB và vô hiệu hóa nó (revoke)
            refreshTokenRepository.findByToken(request.refreshToken()).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        }
    }

    @Transactional
    public LoginResponseDTO refreshToken(String requestRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(requestRefreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Refresh token không tồn tại."));
        
        if (refreshToken.getRevoked() != null && refreshToken.getRevoked()) {
            throw new IllegalArgumentException("Refresh token đã bị thu hồi.");
        }
        
        if (refreshToken.getExpiresAt().isBefore(java.time.LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token đã hết hạn. Vui lòng đăng nhập lại.");
        }
        
        User user = refreshToken.getUser();
        UserProfile profile = userProfileRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin user."));
                
        // Vô hiệu hóa refresh token cũ (Refresh Token Rotation)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);
        
        return LoginResponseDTO.builder()
                .accessToken(jwtTokenProvider.generateAccessToken(user))
                .refreshToken(jwtTokenProvider.generateRefreshToken(user))
                .expiresIn(accessTokenExpiration / 1000)
                .userProfile(UserProfileHeaderResponseDTO.entityToDTO(profile))
                .build();
    }

}