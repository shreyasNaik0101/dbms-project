package com.suraksha.setu.services;

import com.suraksha.setu.dao.WorkerDAO;
import com.suraksha.setu.models.Worker;

import java.sql.SQLException;
import java.util.List;

/**
 * Background thread that periodically recalculates trust scores for all workers.
 *
 * Demonstrates Unit II Multithreading:
 *   - extends Thread
 *   - run() method
 *   - join(), isAlive()
 *   - synchronized block for shared resource access
 *   - Thread.sleep()
 */
public class TrustScoreUpdater extends Thread {

    private static final long INTERVAL_MS = 3_600_000L; // 1 hour
    private final TrustScoreService trustScoreService;
    private final WorkerDAO         workerDAO;
    private volatile boolean        running = true;
    private int                     updateCount = 0;

    public TrustScoreUpdater() {
        super("TrustScoreUpdater");
        this.trustScoreService = new TrustScoreService();
        this.workerDAO         = new WorkerDAO();
        setDaemon(true);  // exits when main app closes
    }

    @Override
    public void run() {
        System.out.println("[TrustScoreUpdater] Background thread started.");
        while (running && !isInterrupted()) {
            try {
                recalculateAll();
                Thread.sleep(INTERVAL_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("[TrustScoreUpdater] Interrupted — stopping.");
                break;
            } catch (Exception e) {
                System.err.println("[TrustScoreUpdater] Error: " + e.getMessage());
            }
        }
        System.out.println("[TrustScoreUpdater] Stopped. Total updates: " + updateCount);
    }

    /** Synchronized method — protects shared DB access from concurrent writes. */
    private synchronized void recalculateAll() {
        try {
            List<Worker> workers = workerDAO.findAll();
            System.out.println("[TrustScoreUpdater] Recalculating scores for " + workers.size() + " workers...");
            for (Worker w : workers) {
                try {
                    trustScoreService.calculateAndUpdate(w.getWorkerId());
                    updateCount++;  // autoboxing: int -> Integer in any collection context
                } catch (Exception e) {
                    System.err.println("[TrustScoreUpdater] Skipped worker "
                        + w.getWorkerId() + ": " + e.getMessage());
                }
            }
            System.out.println("[TrustScoreUpdater] Batch complete.");
        } catch (SQLException e) {
            System.err.println("[TrustScoreUpdater] DB error: " + e.getMessage());
        }
    }

    /** Request graceful stop. */
    public void stopUpdater() {
        running = false;
        interrupt();
    }

    public int getUpdateCount() { return updateCount; }
}
