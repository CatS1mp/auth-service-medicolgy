package com.medicology.auth.entity;

import jakarta.persistence.*; // Cho @Entity, @Table, @Id, @Column, @OneToOne...
import lombok.Data; // Cho @Data

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Data
public class UserProfile {
    @Id
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(length = 100)
    private String firstName;

    @Column(length = 100)
    private String lastName;

    private LocalDate dateOfBirth;

    @Column(length = 50)
    private String gender;

    @Column(length = 255)
    private String address;

    @Column(length = 500)
    private String bio;

}
