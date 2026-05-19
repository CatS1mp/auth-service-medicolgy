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
        @Pattern(regexp = "light|dark|system", message = "Giao diện chỉ hỗ trợ: sáng (light), tối (dark) hoặc theo hệ thống (system).")
        String themePreference,
        @Min(value = 1, message = "Mục tiêu khóa học mỗi ngày phải từ 1 trở lên.")
        @Max(value = 100, message = "Mục tiêu khóa học mỗi ngày không được vượt quá 100.")
        Integer dailyGoalCourses) {
}
