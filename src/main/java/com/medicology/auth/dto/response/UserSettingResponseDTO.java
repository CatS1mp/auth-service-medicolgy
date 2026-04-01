package com.medicology.auth.dto.response;

import com.medicology.auth.entity.UserSetting;

import java.time.LocalTime;
import java.util.UUID;

public record UserSettingResponseDTO(
        UUID userId,
        Boolean notificationEnabled,
        LocalTime dailyReminderTime,
        Boolean emailNotifications,
        Boolean pushNotifications,
        String themePreference,
        Integer dailyGoalCourses) {

    public static UserSettingResponseDTO fromEntity(UserSetting setting) {
        return new UserSettingResponseDTO(
                setting.getUserId(),
                setting.getNotificationEnabled(),
                setting.getDailyReminderTime(),
                setting.getEmailNotifications(),
                setting.getPushNotifications(),
                setting.getThemePreference(),
                setting.getDailyGoalCourses());
    }
}
