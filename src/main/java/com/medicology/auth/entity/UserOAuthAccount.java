package com.medicology.auth.entity;
import jakarta.persistence.*; // Cho @Entity, @Table, @Id, @Column, @OneToOne...
import lombok.Data; // Cho @Data
import java.util.UUID;

@Entity
@Table(name = "user_oauth_account")
@Data
public class UserOAuthAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String provider; // google, facebook, apple
    private String providerUserId;
    private String providerEmail;
}
