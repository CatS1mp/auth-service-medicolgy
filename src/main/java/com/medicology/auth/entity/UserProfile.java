package com.medicology.auth.entity;

import jakarta.persistence.*; // Cho @Entity, @Table, @Id, @Column, @OneToOne...
import lombok.Data; // Cho @Data
import java.util.UUID;

@Table(name = "user_profile")
@Data
public class UserProfile {
    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    private String displayName;
    private String avatarUrl;
    private String bio;
    private String occupation;
    private Boolean medicalBackground = false;
}