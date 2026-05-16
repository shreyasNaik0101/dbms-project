package com.suraksha.setu.models;

import java.sql.Timestamp;

/**
 * Immutable audit record of a trust score change event.
 * All fields are final — demonstrates 'final' keyword (Unit I).
 */
public class TrustScoreAudit {
    private final int       auditId;
    private final int       workerId;
    private final double    oldScore;
    private final double    newScore;
    private final String    reason;
    private final Timestamp calculatedAt;

    public TrustScoreAudit(int auditId, int workerId,
                           double oldScore, double newScore,
                           String reason, Timestamp calculatedAt) {
        this.auditId      = auditId;
        this.workerId     = workerId;
        this.oldScore     = oldScore;
        this.newScore     = newScore;
        this.reason       = reason;
        this.calculatedAt = calculatedAt;
    }

    // Read-only access — no setters (immutable)
    public int       getAuditId()      { return auditId; }
    public int       getWorkerId()     { return workerId; }
    public double    getOldScore()     { return oldScore; }
    public double    getNewScore()     { return newScore; }
    public String    getReason()       { return reason; }
    public Timestamp getCalculatedAt() { return calculatedAt; }

    public double getDelta() { return newScore - oldScore; }

    @Override
    public String toString() {
        return String.format("Audit{worker=%d, %.1f -> %.1f, reason='%s'}",
                workerId, oldScore, newScore, reason);
    }
}
