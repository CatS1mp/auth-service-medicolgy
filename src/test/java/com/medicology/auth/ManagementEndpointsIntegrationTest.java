package com.medicology.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medicology.auth.entity.RefreshToken;
import com.medicology.auth.entity.User;
import com.medicology.auth.entity.UserProfile;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

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
    void getCurrentProfileReturnsMergedUserAndProfileFields() throws Exception {
        jdbcTemplate.update(
                """
                        INSERT INTO user_profile (user_id, first_name, last_name, date_of_birth, gender, address, bio)
                        VALUES (?, ?, ?, ?, ?, ?, ?)
                        """,
                user.getId(),
                "A",
                "Tran Van",
                java.sql.Date.valueOf(LocalDate.of(2005, 11, 19)),
                "MALE",
                "Thanh pho Ho Chi Minh",
                "Gioi thieu ban than");

        mockMvc.perform(get("/api/v1/profiles/me")
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@medicology.dev"))
                .andExpect(jsonPath("$.username").value("user-one"))
                .andExpect(jsonPath("$.lastName").value("Tran Van"))
                .andExpect(jsonPath("$.firstName").value("A"))
                .andExpect(jsonPath("$.dateOfBirth").value("2005-11-19"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.address").value("Thanh pho Ho Chi Minh"))
                .andExpect(jsonPath("$.bio").value("Gioi thieu ban than"));
    }

    @Test
    void getProfileByIdAllowsSelf() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/" + user.getId())
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@medicology.dev"));
    }

    @Test
    void getProfileByIdForbiddenForOtherNonAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/" + admin.getId())
                        .header("Authorization", "Bearer " + userAccessToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getProfileByIdAllowedForAdmin() throws Exception {
        mockMvc.perform(get("/api/v1/profiles/" + user.getId())
                        .header("Authorization", "Bearer " + adminAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@medicology.dev"));
    }

    @Test
    void putProfileByIdUpdatesWhenSelf() throws Exception {
        String payload = objectMapper.writeValueAsString(new UpdateProfileRequest(
                "by-id-user",
                "Ho",
                "Ten",
                LocalDate.of(2000, 1, 2),
                "FEMALE",
                "Addr",
                "Bio text"));

        mockMvc.perform(put("/api/v1/profiles/" + user.getId())
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("by-id-user"))
                .andExpect(jsonPath("$.lastName").value("Ho"))
                .andExpect(jsonPath("$.firstName").value("Ten"));

        User reloaded = userRepository.findById(user.getId()).orElseThrow();
        Assertions.assertEquals("by-id-user", reloaded.getUsername());
    }

    @Test
    void putProfileMeUpdatesNewNullableFields() throws Exception {
        String payload = objectMapper.writeValueAsString(new UpdateProfileRequest(
                "user-profile-updated",
                "Tran Van",
                "A",
                LocalDate.of(2005, 11, 19),
                "MALE",
                "Thanh pho Ho Chi Minh",
                "Gioi thieu ban than"));

        mockMvc.perform(put("/api/v1/profiles/me")
                        .header("Authorization", "Bearer " + userAccessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user-profile-updated"))
                .andExpect(jsonPath("$.lastName").value("Tran Van"))
                .andExpect(jsonPath("$.firstName").value("A"))
                .andExpect(jsonPath("$.dateOfBirth").value("2005-11-19"))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.address").value("Thanh pho Ho Chi Minh"))
                .andExpect(jsonPath("$.bio").value("Gioi thieu ban than"))
                .andExpect(jsonPath("$.displayName").value("Tran Van A"));

        User reloadedUser = userRepository.findById(user.getId()).orElseThrow();
        Assertions.assertEquals("user-profile-updated", reloadedUser.getUsername());

        UserProfile reloadedProfile = userProfileRepository.findById(user.getId()).orElseThrow();
        Assertions.assertEquals(LocalDate.of(2005, 11, 19), reloadedProfile.getDateOfBirth());
        Assertions.assertEquals("Tran Van", reloadedProfile.getLastName());
        Assertions.assertEquals("A", reloadedProfile.getFirstName());
        Assertions.assertEquals("MALE", reloadedProfile.getGender());
        Assertions.assertEquals("Thanh pho Ho Chi Minh", reloadedProfile.getAddress());
        Assertions.assertEquals("Gioi thieu ban than", reloadedProfile.getBio());
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

    private record UpdateProfileRequest(
            String username,
            String lastName,
            String firstName,
            LocalDate dateOfBirth,
            String gender,
            String address,
            String bio) {
    }
}
