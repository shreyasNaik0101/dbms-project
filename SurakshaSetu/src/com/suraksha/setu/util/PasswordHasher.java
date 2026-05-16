package com.suraksha.setu.util;

/**
 * Custom password hashing utility using StringBuffer arithmetic.
 * No external libraries — demonstrates Unit II String/StringBuffer usage.
 *
 * NOTE: This is a teaching implementation. Use BCrypt in production.
 */
public class PasswordHasher {

    private static final long SALT = 0xDEADBEEFCAFEL;

    private PasswordHasher() {}

    /**
     * Hash a password using polynomial rolling hash + XOR salt.
     * Returns a 16-character hexadecimal string.
     * Uses StringBuffer (Unit II).
     */
    public static String hash(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        StringBuffer sb = new StringBuffer(password);
        long hashVal = 0L;
        for (int i = 0; i < sb.length(); i++) {
            hashVal = hashVal * 31L + sb.charAt(i);
        }
        hashVal ^= SALT;
        hashVal = Math.abs(hashVal);

        // Convert to hex, pad to 16 chars with StringBuffer
        StringBuffer result = new StringBuffer(Long.toHexString(hashVal));
        while (result.length() < 16) {
            result.insert(0, '0');
        }
        if (result.length() > 16) {
            result = new StringBuffer(result.substring(0, 16));
        }
        return result.toString();
    }

    /**
     * Verify a plain-text password against a stored hash.
     */
    public static boolean verify(String password, String storedHash) {
        if (password == null || storedHash == null) return false;
        return hash(password).equals(storedHash);
    }

    /**
     * Validate Aadhaar number: must be exactly 12 digits.
     * Demonstrates String methods (Unit II).
     */
    public static boolean isValidAadhaar(String aadhaar) {
        if (aadhaar == null) return false;
        String cleaned = aadhaar.trim().replaceAll("\\s+", "");
        return cleaned.matches("\\d{12}");
    }

    /**
     * Validate phone number: must be 10 digits, starting with 6-9.
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) return false;
        return phone.trim().matches("[6-9]\\d{9}");
    }
}
