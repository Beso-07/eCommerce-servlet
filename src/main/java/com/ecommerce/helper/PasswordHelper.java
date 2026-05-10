package com.ecommerce.helper;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;

public final class PasswordHelper {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int ITERATIONS = 65_536;
    private static final int KEY_LENGTH = 256;

    private PasswordHelper() {
    }

    public static String hashPassword(String plainTextPassword) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        byte[] hash = pbkdf2(plainTextPassword.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(salt) + ":" + Base64.getEncoder().encodeToString(hash);
    }

    public static boolean verifyPassword(String plainTextPassword, String storedHash) {
        if (storedHash == null || !storedHash.contains(":")) {
            return false;
        }
        String[] parts = storedHash.split(":");
        byte[] salt = Base64.getDecoder().decode(parts[0]);
        byte[] hash = pbkdf2(plainTextPassword.toCharArray(), salt);
        return Base64.getEncoder().encodeToString(hash).equals(parts[1]);
    }

    private static byte[] pbkdf2(char[] password, byte[] salt) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH);
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            throw new IllegalStateException("Could not hash password", ex);
        }
    }
}
