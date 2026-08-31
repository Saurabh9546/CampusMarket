package com.campusmarket.backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    // Dev-only secret. Move this to an environment variable before production —
    // never commit a real signing key to source control.
    private static final String SECRET = "change-this-to-a-long-random-secret-before-production-use-only";
    private static final long ACCESS_TOKEN_EXPIRY_MS = 15 * 60 * 1000; // 15 minutes

    private final SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateAccessToken(String userId, boolean verified) {
        return Jwts.builder()
                .subject(userId)
                .claim("verified", verified)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRY_MS))
                .signWith(key)
                .compact();
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isVerified(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("verified", Boolean.class);
    }

    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false; // malformed, tampered, or otherwise unparseable token
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}