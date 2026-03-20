package com.medicology.auth.repository;

import com.medicology.auth.entity.UserOAuthAccount;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.medicology.auth.entity.User;

@Repository
public interface UserOAuthAccountRepository extends JpaRepository<UserOAuthAccount, UUID> {
    boolean existsByUser(User user);
    Optional<UserOAuthAccount> findByUser(User user);
}
