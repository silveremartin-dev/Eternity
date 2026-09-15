/*
 * MIT License
 *
 * Copyright (c) 2026 Silvere Martin-Michiellot, Antigravity
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
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

/**
 * JWT token provider for authentication.
 * Handles token generation and validation.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
 */
public class JwtProvider {

    private static final Logger LOGGER = LogManager.getLogger(JwtProvider.class);

    private final SecretKey secretKey;
    private final long tokenValidityHours;

    public JwtProvider() {
        String secret = System.getenv("JWT_SECRET");
        this.tokenValidityHours = Long.parseLong(System.getenv().getOrDefault("JWT_VALIDITY_HOURS", "24"));
        if (secret != null && !secret.isBlank()) {
            this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } else {
            LOGGER.warn("JWT_SECRET not set in environment. Generating an in-memory random 256-bit key for this session.");
            this.secretKey = Jwts.SIG.HS256.key().build();
        }
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
