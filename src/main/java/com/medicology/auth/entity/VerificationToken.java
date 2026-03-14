package com.medicology.auth.entity;

import jakarta.persistence.*; // Cho @Entity, @Table, @Id, @Column, @OneToOne...
import lombok.Data; // Cho @Data
import java.time.LocalDateTime;
import java.util.UUID;
@Entity
@Table(name = "verification_tokens") // Tên bảng nên để số nhiều hoặc bình thường
@Data
public class VerificationToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String token;

    // Không dùng @UpdateTimestamp ở đây vì đây là logic hết hạn
    private LocalDateTime expiryDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id") // Tạo cột user_id làm khóa ngoại
    private User user;

    // Constructor để tạo token nhanh trong Service
    public VerificationToken(String token, User user) {
        this.token = token;
        this.user = user;
        this.expiryDate = LocalDateTime.now().plusHours(24); // Tự cộng 24 tiếng
    }
    
    // Đừng quên No-Args Constructor cho JPA
    public VerificationToken() {}
}