package com.medicology.auth.repository;

import com.medicology.auth.entity.RefreshToken; // Import Class User bạn đã tạo ở bước trước
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByToken(String token);
    List<RefreshToken> findAllByUserIdAndRevokedFalseAndExpiresAtAfter(UUID userId, LocalDateTime now);
    Optional<RefreshToken> findByIdAndUserId(UUID id, UUID userId);
}
