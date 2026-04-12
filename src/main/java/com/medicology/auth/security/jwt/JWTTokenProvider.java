package com.medicology.auth.security.jwt;

import com.medicology.auth.entity.RefreshToken;
import com.medicology.auth.entity.User;
import com.medicology.auth.repository.RefreshTokenRepository;

import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JWTTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${access.token.expiration}")
    private long accessTokenExpiration;

    @Value("${refresh.token.expiration}")
    private long refreshTokenExpiration;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return generateToken(user, accessTokenExpiration, "access");
    }

    public String generateRefreshToken(User user) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000));
        refreshToken.setToken(generateToken(user, refreshTokenExpiration, "refresh"));
        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    // 1. Hàm tạo Token
    private String generateToken(User user, long expiration, String type) {
        return Jwts.builder()
                .subject(user.getEmail())
                .issuedAt(new Date())
                .claim("type", type)
                .claim("id", user.getId().toString())
                .claim("role", Boolean.TRUE.equals(user.getIsAdmin()) ? "ADMIN" : "USER")
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key)
                .compact();
    }

}
