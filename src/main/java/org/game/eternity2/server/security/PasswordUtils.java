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

import at.favre.lib.crypto.bcrypt.BCrypt;

/**
 * Password hashing utility using bcrypt.
  * @author Silvere Martin-Michiellot
  * @author Antigravity
  * @since 1.0
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
