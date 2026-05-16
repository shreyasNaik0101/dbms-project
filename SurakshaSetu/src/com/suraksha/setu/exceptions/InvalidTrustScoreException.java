package com.suraksha.setu.exceptions;

/**
 * Thrown when a trust score value is outside the valid range (0–1000).
 * Extends RuntimeException — unchecked exception.
 */
public class InvalidTrustScoreException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private final double invalidScore;

    public InvalidTrustScoreException(double score) {
        super("Invalid trust score: " + score + ". Must be between 0.0 and 1000.0");
        this.invalidScore = score;
    }

    public InvalidTrustScoreException(String message) {
        super(message);
        this.invalidScore = -1;
    }

    public InvalidTrustScoreException(String message, Throwable cause) {
        super(message, cause);
        this.invalidScore = -1;
    }

    public double getInvalidScore() { return invalidScore; }
}
