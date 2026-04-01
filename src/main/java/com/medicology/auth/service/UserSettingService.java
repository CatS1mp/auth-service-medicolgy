package com.medicology.auth.service;

import com.medicology.auth.dto.request.UpdateUserSettingsRequestDTO;
import com.medicology.auth.dto.response.UserSettingResponseDTO;
import com.medicology.auth.entity.User;
import com.medicology.auth.entity.UserSetting;
import com.medicology.auth.repository.UserSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserSettingService {
    private final CurrentUserService currentUserService;
    private final UserSettingRepository userSettingRepository;

    public UserSettingResponseDTO getCurrentSettings(Authentication authentication) {
        return getSettingsByUser(currentUserService.getCurrentUser(authentication));
    }

    public UserSettingResponseDTO getSettingsByUser(User user) {
        return UserSettingResponseDTO.fromEntity(getOrCreateSetting(user));
    }

    @Transactional
    public UserSettingResponseDTO updateCurrentSettings(Authentication authentication, UpdateUserSettingsRequestDTO request) {
        User user = currentUserService.getCurrentUser(authentication);
        UserSetting setting = getOrCreateSetting(user);

        if (request.notificationEnabled() != null) {
            setting.setNotificationEnabled(request.notificationEnabled());
        }
        if (request.dailyReminderTime() != null) {
            setting.setDailyReminderTime(request.dailyReminderTime());
        }
        if (request.emailNotifications() != null) {
            setting.setEmailNotifications(request.emailNotifications());
        }
        if (request.pushNotifications() != null) {
            setting.setPushNotifications(request.pushNotifications());
        }
        if (request.themePreference() != null) {
            setting.setThemePreference(request.themePreference());
        }
        if (request.dailyGoalCourses() != null) {
            setting.setDailyGoalCourses(request.dailyGoalCourses());
        }

        return UserSettingResponseDTO.fromEntity(userSettingRepository.save(setting));
    }

    private UserSetting getOrCreateSetting(User user) {
        return userSettingRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    UserSetting setting = new UserSetting();
                    setting.setUser(user);
                    return userSettingRepository.save(setting);
                });
    }
}
