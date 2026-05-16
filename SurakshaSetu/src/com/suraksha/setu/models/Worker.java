package com.suraksha.setu.models;

import com.suraksha.setu.util.DigitalIDGenerator;
import java.sql.Timestamp;

/**
 * Represents a gig economy worker — the core domain entity.
 *
 * Demonstrates (Unit I):
 *  - Inheritance (extends User)
 *  - Comparable<Worker> interface (sort by trust score)
 *  - final field (digitalWorkId — immutable after construction)
 *  - Constructor overloading (3 constructors)
 *  - Static utility method (generateDigitalID)
 *  - Method overriding (getDisplayName, compareTo, toString)
 *  - super() constructor chaining
 */
public class Worker extends User implements Comparable<Worker> {
    private static final long serialVersionUID = 1L;

    private int       workerId;
    private final String digitalWorkId;   // immutable — final
    private String    fullName;
    private String    phone;
    private String    aadhaarHash;
    private double    currentTrustScore;
    private Timestamp joiningDate;
    private boolean   kycVerified;

    // ── Constructors (overloaded) ────────────────────────────────────────────

    /** Default constructor — generates a new Digital ID */
    public Worker() {
        super();
        this.digitalWorkId = DigitalIDGenerator.generateID();
        this.currentTrustScore = 0.0;
        this.kycVerified       = false;
    }

    /** Parameterized constructor without aadhaar — overloading */
    public Worker(int workerId, String digitalWorkId, String fullName,
                  String phone, double trustScore) {
        super();
        this.workerId          = workerId;
        this.digitalWorkId     = digitalWorkId;
        this.fullName          = fullName;
        this.phone             = phone;
        this.currentTrustScore = trustScore;
        this.kycVerified       = false;
    }

    /** Full constructor with aadhaar — overloading, uses super() */
    public Worker(int workerId, String digitalWorkId, String fullName,
                  String phone, String aadhaarHash,
                  double trustScore, Timestamp joiningDate) {
        super(0, "", "Worker");  // super() constructor chaining
        this.workerId          = workerId;
        this.digitalWorkId     = digitalWorkId;
        this.fullName          = fullName;
        this.phone             = phone;
        this.aadhaarHash       = aadhaarHash;
        this.currentTrustScore = trustScore;
        this.joiningDate       = joiningDate;
        this.kycVerified       = false;
    }

    // ── Static utility method ────────────────────────────────────────────────

    /** Generates a new Digital Work ID — static factory method */
    public static String generateDigitalID() {
        return DigitalIDGenerator.generateID();
    }

    public static String generateDigitalID(String aadhaar) {
        return DigitalIDGenerator.generateID(aadhaar);
    }

    // ── Abstract method implementations ─────────────────────────────────────

    @Override
    public String getDisplayName() {
        return fullName + " (" + digitalWorkId + ")";
    }

    @Override
    public boolean isAdmin() { return false; }

    // ── Comparable implementation — sort by trust score (descending) ─────────

    @Override
    public int compareTo(Worker other) {
        // Descending order: higher score first
        return Double.compare(other.currentTrustScore, this.currentTrustScore);
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public int       getWorkerId()                         { return workerId; }
    public void      setWorkerId(int workerId)             { this.workerId = workerId; }
    public String    getDigitalWorkId()                    { return digitalWorkId; }
    public String    getFullName()                         { return fullName; }
    public void      setFullName(String fullName)          { this.fullName = fullName; }
    public String    getPhone()                            { return phone; }
    public void      setPhone(String phone)                { this.phone = phone; }
    public String    getAadhaarHash()                      { return aadhaarHash; }
    public void      setAadhaarHash(String aadhaarHash)    { this.aadhaarHash = aadhaarHash; }
    public double    getCurrentTrustScore()                { return currentTrustScore; }
    public void      setCurrentTrustScore(double score)    { this.currentTrustScore = score; }
    public Timestamp getJoiningDate()                      { return joiningDate; }
    public void      setJoiningDate(Timestamp joiningDate) { this.joiningDate = joiningDate; }
    public boolean   isKycVerified()                       { return kycVerified; }
    public void      setKycVerified(boolean kycVerified)   { this.kycVerified = kycVerified; }

    @Override
    public String toString() {
        return String.format("Worker{id=%d, name='%s', score=%.1f}", workerId, fullName, currentTrustScore);
    }
}
