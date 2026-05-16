package com.suraksha.setu.models;

import java.sql.Timestamp;

/**
 * Represents a micro-loan application by a worker.
 * Implements Comparable<LoanApplication> — used in PriorityQueue (highest trust score first).
 * Demonstrates Unit III: Comparable, PriorityQueue ordering.
 */
public class LoanApplication implements Comparable<LoanApplication> {
    private int       loanId;
    private int       workerId;
    private String    workerName;   // joined
    private int       providerId;
    private String    providerName; // joined
    private double    loanAmount;
    private String    status;       // Applied / Approved / Rejected / Repaid
    private Timestamp appliedDate;
    private double    workerTrustScore; // for PriorityQueue ordering

    public LoanApplication() {}

    public LoanApplication(int loanId, int workerId, int providerId,
                           double loanAmount, String status, Timestamp appliedDate) {
        this.loanId      = loanId;
        this.workerId    = workerId;
        this.providerId  = providerId;
        this.loanAmount  = loanAmount;
        this.status      = status;
        this.appliedDate = appliedDate;
    }

    /** PriorityQueue ordering: highest trust score processed first. */
    @Override
    public int compareTo(LoanApplication other) {
        return Double.compare(other.workerTrustScore, this.workerTrustScore);
    }

    public int       getLoanId()                         { return loanId; }
    public void      setLoanId(int loanId)               { this.loanId = loanId; }
    public int       getWorkerId()                       { return workerId; }
    public void      setWorkerId(int workerId)           { this.workerId = workerId; }
    public String    getWorkerName()                     { return workerName; }
    public void      setWorkerName(String workerName)    { this.workerName = workerName; }
    public int       getProviderId()                     { return providerId; }
    public void      setProviderId(int providerId)       { this.providerId = providerId; }
    public String    getProviderName()                   { return providerName; }
    public void      setProviderName(String n)           { this.providerName = n; }
    public double    getLoanAmount()                     { return loanAmount; }
    public void      setLoanAmount(double loanAmount)    { this.loanAmount = loanAmount; }
    public String    getStatus()                         { return status; }
    public void      setStatus(String status)            { this.status = status; }
    public Timestamp getAppliedDate()                    { return appliedDate; }
    public void      setAppliedDate(Timestamp d)         { this.appliedDate = d; }
    public double    getWorkerTrustScore()               { return workerTrustScore; }
    public void      setWorkerTrustScore(double score)   { this.workerTrustScore = score; }

    @Override
    public String toString() {
        return String.format("LoanApplication{id=%d, worker=%s, amount=%.2f, status=%s}",
                loanId, workerName, loanAmount, status);
    }
}
