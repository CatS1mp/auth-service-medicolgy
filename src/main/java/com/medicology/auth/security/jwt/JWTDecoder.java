package com.medicology.auth.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.JwtException;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JWTDecoder {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.issuer:medicology-auth}")
    private String expectedIssuer;

    @Value("${jwt.audience:medicology-api}")
    private String expectedAudience;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractUserIdClaim(String token) {
        try {
            return extractAllClaims(token).get("id", String.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

    // 3. Hàm kiểm tra Token hợp lệ hay không
    public boolean isTokenValid(String token, String expectedType) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Kiểm tra đúng loại token (Access vs Refresh)
            String tokenType = claims.get("type", String.class);
            if (!expectedType.equals(tokenType)) {
                return false;
            }
            String iss = claims.getIssuer();
            if (iss == null || !expectedIssuer.equals(iss)) {
                return false;
            }
            Object aud = claims.get("aud");
            if (aud == null) {
                return false;
            }
            if (aud instanceof String s) {
                return expectedAudience.equals(s);
            }
            if (aud instanceof java.util.Collection<?> col) {
                return col.stream().anyMatch(expectedAudience::equals);
            }
            return false;

        } catch (ExpiredJwtException e) {
            // Token hết hạn - Log ra để debug nếu cần
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            // Chữ ký sai, format sai, token rỗng...
            return false;
        }
    }
}
