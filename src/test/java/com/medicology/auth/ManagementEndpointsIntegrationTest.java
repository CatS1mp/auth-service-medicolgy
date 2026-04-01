package com.medicology.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.auth.entity.RefreshToken;
import com.medicology.auth.entity.User;
import com.medicology.auth.repository.RefreshTokenRepository;
import com.medicology.auth.repository.UserOAuthAccountRepository;
import com.medicology.auth.repository.UserProfileRepository;
import com.medicology.auth.repository.UserRepository;
import com.medicology.auth.repository.UserSettingRepository;
import com.medicology.auth.security.jwt.JWTTokenProvider;
import com.medicology.auth.service.EmailService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ManagementEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private UserSettingRepository userSettingRepository;

    @Autowired
    private UserOAuthAccountRepository userOAuthAccountRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTTokenProvider jwtTokenProvider;

    @MockBean
    private EmailService emailService;

    private User user;
    private User admin;
    private String userAccessToken;
    private String adminAccessToken;

    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userOAuthAccountRepository.deleteAll();
        userSettingRepository.deleteAll();
        userProfileRepository.deleteAll();
        userRepository.deleteAll();

        user = createUser("user@medicology.dev", "user-one", false);
        admin = createUser("admin@medicology.dev", "admin-one", true);

        userAccessToken = jwtTokenProvider.generateAccessToken(user);
        adminAccessToken = jwtTokenProvider.generateAccessToken(admin);
    }

    @Test
    void getCurrentUserReturnsAuthenticatedUser() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@medicology.dev"))
                .andExpect(jsonPath("$.admin").value(false));
    }

    @Test
    void adminEndpointsRejectNormalUser() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListUsers() throws Exception {
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void patchSettingsCreatesAndUpdatesCurrentUserSettings() throws Exception {
        String payload = objectMapper.writeValueAsString(new SettingsRequest(false, "dark", 3));

        mockMvc.perform(patch("/api/v1/settings/me")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notificationEnabled").value(false))
                .andExpect(jsonPath("$.themePreference").value("dark"))
                .andExpect(jsonPath("$.dailyGoalCourses").value(3));
    }

    @Test
    void revokeSessionMarksOwnedRefreshTokenAsRevoked() throws Exception {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken("sample-refresh-token");
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(1));
        refreshToken.setRevoked(false);
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

        mockMvc.perform(delete("/api/v1/sessions/" + savedToken.getId())
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isNoContent());

        RefreshToken reloaded = refreshTokenRepository.findById(savedToken.getId()).orElseThrow();
        Assertions.assertTrue(Boolean.TRUE.equals(reloaded.getRevoked()));
    }

    private User createUser(String email, String username, boolean isAdmin) {
        User entity = new User();
        entity.setEmail(email);
        entity.setUsername(username);
        entity.setPasswordHash(passwordEncoder.encode("Password123!"));
        entity.setIsVerified(true);
        entity.setIsActive(true);
        entity.setIsAdmin(isAdmin);
        return userRepository.save(entity);
    }

    private record SettingsRequest(
            Boolean notificationEnabled,
            String themePreference,
            Integer dailyGoalCourses) {
    }
}
