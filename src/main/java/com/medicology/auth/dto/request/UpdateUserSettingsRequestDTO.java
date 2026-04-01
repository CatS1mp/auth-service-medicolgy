package com.medicology.auth.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import java.time.LocalTime;

public record UpdateUserSettingsRequestDTO(
        Boolean notificationEnabled,
        LocalTime dailyReminderTime,
        Boolean emailNotifications,
        Boolean pushNotifications,
        @Pattern(regexp = "light|dark|system", message = "Theme preference chỉ hỗ trợ light, dark hoặc system.")
        String themePreference,
        @Min(value = 1, message = "Daily goal courses phải lớn hơn hoặc bằng 1.")
        @Max(value = 100, message = "Daily goal courses phải nhỏ hơn hoặc bằng 100.")
        Integer dailyGoalCourses) {
}
