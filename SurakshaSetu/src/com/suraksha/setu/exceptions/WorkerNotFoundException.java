package com.suraksha.setu.exceptions;

/**
 * Thrown when a worker cannot be found by ID or digital work ID.
 * Demonstrates exception chaining (cause constructor).
 */
public class WorkerNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;
    private final int workerId;

    public WorkerNotFoundException(int workerId) {
        super("Worker not found with ID: " + workerId);
        this.workerId = workerId;
    }

    public WorkerNotFoundException(String message) {
        super(message);
        this.workerId = -1;
    }

    // Exception chaining constructor
    public WorkerNotFoundException(String message, Throwable cause) {
        super(message, cause);
        this.workerId = -1;
    }

    public int getWorkerId() { return workerId; }
}
