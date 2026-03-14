package com.medicology.auth.entity;

import jakarta.persistence.*; // Cho @Entity, @Table, @Id, @Column, @OneToOne...
import jakarta.validation.constraints.Email;
import lombok.Data; // Cho @Data
import org.hibernate.annotations.CreationTimestamp; // Cho @CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp; // Cho @UpdateTimestamp
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "[user]") // Dùng ngoặc vuông vì 'user' là từ khóa hệ thống
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    @Email(message = "Email không hợp lệ")
    private String email;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash")
    private String passwordHash;

    private LocalDate dateOfBirth;

    private String location;

    private Boolean isActive = true;

    private Boolean isVerified = false;

    private Boolean isAdmin = false;

    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Mapping tới các bảng liên quan
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserProfile profile;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private UserSetting settings;

    @OneToOne(mappedBy = "user") // Khai báo rằng bảng Token mới là chủ thể giữ khóa ngoại
    private VerificationToken verificationToken;
}