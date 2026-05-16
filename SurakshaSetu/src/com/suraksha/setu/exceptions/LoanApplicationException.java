package com.suraksha.setu.exceptions;

/**
 * Thrown when a loan application fails validation or processing.
 */
public class LoanApplicationException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String reason;

    public LoanApplicationException(String reason) {
        super("Loan application failed: " + reason);
        this.reason = reason;
    }

    public LoanApplicationException(String reason, Throwable cause) {
        super("Loan application failed: " + reason, cause);
        this.reason = reason;
    }

    public String getReason() { return reason; }
}
