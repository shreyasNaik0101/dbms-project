package com.suraksha.setu.exceptions;

/**
 * Thrown when a worker's earnings are below the required threshold
 * for a financial operation (e.g., loan application).
 */
public class InsufficientEarningsException extends Exception {
    private static final long serialVersionUID = 1L;
    private final double requiredAmount;
    private final double actualAmount;

    public InsufficientEarningsException(double required, double actual) {
        super(String.format("Insufficient earnings. Required: %.2f, Actual: %.2f", required, actual));
        this.requiredAmount = required;
        this.actualAmount   = actual;
    }

    public InsufficientEarningsException(String message) {
        super(message);
        this.requiredAmount = 0;
        this.actualAmount   = 0;
    }

    public InsufficientEarningsException(String message, Throwable cause) {
        super(message, cause);
        this.requiredAmount = 0;
        this.actualAmount   = 0;
    }

    public double getRequiredAmount() { return requiredAmount; }
    public double getActualAmount()   { return actualAmount; }
}
