package com.medicology.auth.entity;

import jakarta.persistence.*; // Cho @Entity, @Table, @Id, @Column, @OneToOne...
import lombok.Data; // Cho @Data
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "user_setting")
@Data
public class UserSetting {
    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private Boolean notificationEnabled = true;
    private LocalTime dailyReminderTime;
    private Boolean emailNotifications = true;
    private Boolean pushNotifications = true;
    private String themePreference = "light";
    private Integer dailyGoalCourses = 1;
}
