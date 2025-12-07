package org.game.eternity2.server.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PasswordUtils.
 * 
 * @author Gemini AI Assistant
 * @author Silvère
 */
class PasswordUtilsTest {

    @Test
    @DisplayName("Hash password")
    void testHash() {
        String password = "SecurePassword123!";
        String hash = PasswordUtils.hash(password);
        
        assertNotNull(hash);
        assertFalse(hash.isEmpty());
        assertNotEquals(password, hash);
        assertTrue(hash.startsWith("$2a$") || hash.startsWith("$2b$"));
    }

    @Test
    @DisplayName("Verify correct password")
    void testVerifyCorrect() {
        String password = "MyPassword";
        String hash = PasswordUtils.hash(password);
        
        assertTrue(PasswordUtils.verify(password, hash));
    }

    @Test
    @DisplayName("Verify wrong password")
    void testVerifyWrong() {
        String password = "MyPassword";
        String hash = PasswordUtils.hash(password);
        
        assertFalse(PasswordUtils.verify("WrongPassword", hash));
    }

    @Test
    @DisplayName("Same password produces different hashes")
    void testDifferentHashes() {
        String password = "TestPassword";
        String hash1 = PasswordUtils.hash(password);
        String hash2 = PasswordUtils.hash(password);
        
        assertNotEquals(hash1, hash2);
        assertTrue(PasswordUtils.verify(password, hash1));
        assertTrue(PasswordUtils.verify(password, hash2));
    }
}
