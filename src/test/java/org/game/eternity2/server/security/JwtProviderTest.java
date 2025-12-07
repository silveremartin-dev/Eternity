package org.game.eternity2.server.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import io.jsonwebtoken.Claims;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JwtProvider.
 * 
 * @author Gemini AI Assistant
 * @author Silvère
 */
class JwtProviderTest {

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider("test-secret-key-must-be-at-least-256-bits-long", 24);
    }

    @Test
    @DisplayName("Generate and validate token")
    void testGenerateAndValidate() {
        String token = jwtProvider.generateToken(1, "testuser", "USER");

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtProvider.validateToken(token);
        assertNotNull(claims);
        assertEquals("1", claims.getSubject());
        assertEquals("testuser", claims.get("username", String.class));
        assertEquals("USER", claims.get("role", String.class));
    }

    @Test
    @DisplayName("Extract user ID from token")
    void testGetUserId() {
        String token = jwtProvider.generateToken(42, "admin", "ADMIN");

        Integer userId = jwtProvider.getUserId(token);
        assertEquals(42, userId);
    }

    @Test
    @DisplayName("Extract username from token")
    void testGetUsername() {
        String token = jwtProvider.generateToken(1, "johndoe", "USER");

        String username = jwtProvider.getUsername(token);
        assertEquals("johndoe", username);
    }

    @Test
    @DisplayName("Extract role from token")
    void testGetRole() {
        String token = jwtProvider.generateToken(1, "admin", "ADMIN");

        String role = jwtProvider.getRole(token);
        assertEquals("ADMIN", role);
    }

    @Test
    @DisplayName("Invalid token returns null")
    void testInvalidToken() {
        Claims claims = jwtProvider.validateToken("invalid.token.here");
        assertNull(claims);

        Integer userId = jwtProvider.getUserId("bad-token");
        assertNull(userId);
    }

    @Test
    @DisplayName("Token not expired")
    void testTokenNotExpired() {
        String token = jwtProvider.generateToken(1, "user", "USER");
        assertFalse(jwtProvider.isTokenExpired(token));
    }
}
