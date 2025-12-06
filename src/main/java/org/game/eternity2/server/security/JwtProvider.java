package org.game.eternity2.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

/**
 * JWT token provider for authentication.
 * Handles token generation and validation.
 */
public class JwtProvider {

    private static final Logger LOGGER = LogManager.getLogger(JwtProvider.class);
    private static final String DEFAULT_SECRET = "eternity2-secret-key-must-be-at-least-256-bits-long-for-hs256";

    private final SecretKey secretKey;
    private final long tokenValidityHours;

    public JwtProvider() {
        this(System.getenv().getOrDefault("JWT_SECRET", DEFAULT_SECRET),
                Long.parseLong(System.getenv().getOrDefault("JWT_VALIDITY_HOURS", "24")));
    }

    public JwtProvider(String secret, long tokenValidityHours) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.tokenValidityHours = tokenValidityHours;
    }

    /**
     * Generate a JWT token for a user.
     * 
     * @param userId   User ID
     * @param username Username
     * @param role     User role (e.g., "USER", "ADMIN")
     * @return JWT token string
     */
    public String generateToken(int userId, String username, String role) {
        Instant now = Instant.now();
        Instant expiry = now.plus(tokenValidityHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Validate a JWT token and extract claims.
     * 
     * @param token JWT token
     * @return Claims if valid, null if invalid
     */
    public Claims validateToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            LOGGER.debug("Invalid JWT token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Extract user ID from token.
     */
    public Integer getUserId(String token) {
        Claims claims = validateToken(token);
        if (claims == null)
            return null;
        return Integer.parseInt(claims.getSubject());
    }

    /**
     * Extract username from token.
     */
    public String getUsername(String token) {
        Claims claims = validateToken(token);
        if (claims == null)
            return null;
        return claims.get("username", String.class);
    }

    /**
     * Extract role from token.
     */
    public String getRole(String token) {
        Claims claims = validateToken(token);
        if (claims == null)
            return null;
        return claims.get("role", String.class);
    }

    /**
     * Check if token is expired.
     */
    public boolean isTokenExpired(String token) {
        Claims claims = validateToken(token);
        if (claims == null)
            return true;
        return claims.getExpiration().before(new Date());
    }
}
