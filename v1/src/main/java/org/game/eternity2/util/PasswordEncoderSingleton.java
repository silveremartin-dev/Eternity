package org.game.eternity2.util;


import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

// after https://www.baeldung.com/java-password-hashing
public final class PasswordEncoderSingleton {

    private PasswordEncoderSingleton() {
        super();
    }

    public static PasswordEncoderSingleton getInstance() {
        return PasswordEncoderSingleton.PasswordEncoderSingletonHolder.instance;
    }

    public final static byte[] hashPassword(String password) throws InvalidKeySpecException, NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] hash = factory.generateSecret(spec).getEncoded();
        return hash;
    }

    private static class PasswordEncoderSingletonHolder {
        private final static PasswordEncoderSingleton instance = new PasswordEncoderSingleton();

    }

}
