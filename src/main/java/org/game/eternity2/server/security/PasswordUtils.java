package org.game.eternity2.server.security;

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Password hashing utility using bcrypt.
 */
public class PasswordUtils {

    private static final int BCRYPT_COST = 12;

    /**
     * Hash a password using bcrypt.
     * 
     * @param plainPassword Plain text password
     * @return Hashed password
     */
    public static String hash(String plainPassword) {
        return BCrypt.withDefaults().hashToString(BCRYPT_COST, plainPassword.toCharArray());
    }

    /**
     * Verify a password against a hash.
     * 
     * @param plainPassword  Plain text password
     * @param hashedPassword Bcrypt hash
     * @return true if password matches
     */
    public static boolean verify(String plainPassword, String hashedPassword) {
        BCrypt.Result result = BCrypt.verifyer().verify(plainPassword.toCharArray(), hashedPassword);
        return result.verified;
    }
}
