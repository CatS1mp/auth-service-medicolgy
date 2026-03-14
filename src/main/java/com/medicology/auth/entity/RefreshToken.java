package com.medicology.auth.entity;
import jakarta.persistence.*; // Cho @Entity, @Table, @Id, @Column, @OneToOne...
import lombok.Data; // Cho @Data
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_token")
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String token;

    private LocalDateTime expiresAt;

    private Boolean revoked = false;
}
