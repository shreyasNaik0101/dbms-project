package com.suraksha.setu.util;

import com.suraksha.setu.models.Worker;

/**
 * Functional Interface for filtering Worker objects with lambda expressions.
 * Demonstrates Unit II: @FunctionalInterface, Lambda expressions.
 *
 * Usage:
 *   WorkerFilter highScore = w -> w.getCurrentTrustScore() > 600;
 *   WorkerFilter verified  = w -> w.isKycVerified();
 *   WorkerFilter combined  = highScore.and(verified);
 */
@FunctionalInterface
public interface WorkerFilter {

    /**
     * Test whether a worker matches the filter condition.
     */
    boolean test(Worker worker);

    /**
     * Compose two filters with logical AND.
     * Default method — allows filter chaining without breaking @FunctionalInterface.
     */
    default WorkerFilter and(WorkerFilter other) {
        return w -> this.test(w) && other.test(w);
    }

    // ── Pre-built static factory methods (lambda-based) ──────────────────────

    static WorkerFilter byMinTrustScore(double minScore) {
        return w -> w.getCurrentTrustScore() >= minScore;
    }

    static WorkerFilter byPhone(String phone) {
        return w -> phone != null && phone.equals(w.getPhone());
    }

    static WorkerFilter byNameContains(String keyword) {
        return w -> w.getFullName() != null
                 && w.getFullName().toLowerCase().contains(keyword.toLowerCase());
    }
}
