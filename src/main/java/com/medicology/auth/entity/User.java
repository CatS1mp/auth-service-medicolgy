package com.medicology.auth.entity;

import jakarta.persistence.*; // Cho @Entity, @Table, @Id, @Column, @OneToOne...
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.CreationTimestamp; // Cho @CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp; // Cho @UpdateTimestamp
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "[user]") // Dùng ngoặc vuông vì 'user' là từ khóa hệ thống
@Getter // Dùng Getter/Setter riêng thay vì @Data để kiểm soát tốt hơn
@Setter
@ToString(exclude = { "profile", "settings", "verificationToken" })
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

    private Boolean isActive = true;

    private Boolean isVerified = false;

    private Boolean isAdmin = false;

    private LocalDateTime lastLoginAt;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // Mapping tới các bảng liên quan
    @OneToOne(mappedBy = "user")
    private UserProfile profile;

    @OneToOne(mappedBy = "user")
    private UserSetting settings;

    @OneToOne(mappedBy = "user") // Khai báo rằng bảng Token mới là chủ thể giữ khóa ngoại
    private VerificationToken verificationToken;

}