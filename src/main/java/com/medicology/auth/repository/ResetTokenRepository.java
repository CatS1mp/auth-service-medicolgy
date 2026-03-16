package com.medicology.auth.repository;

import com.medicology.auth.entity.ResetToken; // Import Class User bạn đã tạo ở bước trước
import com.medicology.auth.entity.User;

import java.util.UUID;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, UUID> {
    Optional<User> findUserByToken(UUID token);
    Optional<ResetToken> findByToken(UUID token);
    Optional<ResetToken> findByUserId(UUID userId);
    
}