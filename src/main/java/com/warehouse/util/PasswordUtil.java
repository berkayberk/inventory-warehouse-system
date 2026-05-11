package com.warehouse.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility methods for secure password handling using BCrypt.
 */
public final class PasswordUtil {

    private static final int WORK_FACTOR = 12;

    // Prevent instantiation
    private PasswordUtil() {
    }

    /**
     * Hashes a plain-text password using BCrypt.
     *
     * @param plainText the password to hash
     * @return the BCrypt hash string ready to store in the database
     */
    public static String hash(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        return BCrypt.hashpw(plainText, BCrypt.gensalt(WORK_FACTOR));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainText      the password the user entered
     * @param hashedPassword the hash from the database
     * @return {@code true} if the password matches
     */
    public static boolean verify(String plainText, String hashedPassword) {
        if (plainText == null || hashedPassword == null)
            return false;
        try {
            return BCrypt.checkpw(plainText, hashedPassword);
        } catch (Exception ex) {
            return false;
        }
    }
}
