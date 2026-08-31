package com.campusmarket.backend.security;

import com.campusmarket.backend.exception.UnauthorizedException;
import org.springframework.stereotype.Service;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repository;

    public RefreshTokenService(RefreshTokenRepository repository) {
        this.repository = repository;
    }

    public String issueToken(Long userId) {
        String rawToken = UUID.randomUUID().toString();
        RefreshToken entity = new RefreshToken();
        entity.setTokenHash(hash(rawToken));
        entity.setUserId(userId);
        entity.setExpiresAt(LocalDateTime.now().plusDays(30));
        repository.save(entity);
        return rawToken;
    }

    public Long validateAndGetUserId(String rawToken) {
        RefreshToken entity = repository.findByTokenHash(hash(rawToken))
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired", "REFRESH_TOKEN_EXPIRED");
        }
        return entity.getUserId();
    }

    public void revokeAllForUser(Long userId) {
        repository.deleteByUserId(userId);
    }

    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(digest.digest(raw.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException(e); // MessageDigest.getInstance("SHA-256") never actually fails at runtime — this is unreachable in practice
        }
    }
}