package com.suraksha.setu.util;

import java.time.LocalDate;
import java.util.Random;

/**
 * Generates Digital Work IDs in format: SS-YYYY-MMDD-XXXX
 * Demonstrates: StringBuffer, String manipulation, static methods, method overloading.
 */
public class DigitalIDGenerator {

    private static final String PREFIX = "SS";
    private static final Random RANDOM = new Random();

    private DigitalIDGenerator() {}

    /**
     * Generate a random Digital ID (no aadhaar seed).
     * Method overloading — no args version.
     */
    public static String generateID() {
        return buildID(RANDOM.nextInt(9000) + 1000);
    }

    /**
     * Generate a Digital ID seeded from aadhaar string.
     * Method overloading — with aadhaar version.
     * Uses String.hashCode() for deterministic suffix.
     */
    public static String generateID(String aadhaar) {
        if (aadhaar == null || aadhaar.trim().isEmpty()) {
            return generateID();
        }
        // Validate aadhaar: must be 12 digits
        String cleaned = aadhaar.replaceAll("\\s+", "");
        int suffix = Math.abs(cleaned.hashCode()) % 9000 + 1000;
        return buildID(suffix);
    }

    /** Internal builder — uses StringBuffer (Unit II requirement). */
    private static String buildID(int suffix) {
        LocalDate today = LocalDate.now();
        StringBuffer sb = new StringBuffer();
        sb.append(PREFIX).append("-");
        sb.append(today.getYear()).append("-");
        sb.append(String.format("%02d", today.getMonthValue()));
        sb.append(String.format("%02d", today.getDayOfMonth())).append("-");
        sb.append(suffix);
        return sb.toString();
    }

    /**
     * Validate format: SS-YYYY-MMDD-XXXX
     * Uses String.matches() with regex.
     */
    public static boolean isValidID(String id) {
        if (id == null) return false;
        return id.matches("SS-\\d{4}-\\d{4}-\\d{4}");
    }

    /**
     * Parse year from digital ID.
     * Demonstrates String.split() and Integer.parseInt() (Type Wrapper).
     */
    public static int parseYear(String digitalId) {
        try {
            String[] parts = digitalId.split("-");
            return Integer.parseInt(parts[1]);   // Type Wrapper autoboxing
        } catch (Exception e) {
            return -1;
        }
    }
}
