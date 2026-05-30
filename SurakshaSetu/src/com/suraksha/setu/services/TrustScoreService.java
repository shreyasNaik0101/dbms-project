package com.suraksha.setu.services;

import com.suraksha.setu.dao.TrustScoreDAO;
import com.suraksha.setu.dao.WorkerDAO;
import com.suraksha.setu.exceptions.InvalidTrustScoreException;
import com.suraksha.setu.exceptions.WorkerNotFoundException;
import com.suraksha.setu.models.Worker;

import java.sql.SQLException;

/**
 * Business logic for trust score calculation.
 *
 * Formula (0–1000):
 *   Score = (0.40 × Consistency) + (0.35 × RatingScore) + (0.25 × IncomeScore)
 *
 *   Consistency  = (workDaysLast90 / 90.0) × 100    [capped at 100]
 *   RatingScore  = avgRating × 20                   [maps 0–5 → 0–100]
 *   IncomeScore  = incomeLevel + regularityBonus     [capped at 100]
 *     incomeLevel     = min(avgMonthly / 20000, 1.0) × 90  [Rs.20k/month = 90pts]
 *     regularityBonus = 10 if worker has earnings in 3+ months, else 0
 *
 *   Key property: logging more work always increases income → always increases score.
 *   Final = weightedSum × 10                        [scales 0–100 → 0–1000]
 */
public class TrustScoreService {

    private final WorkerDAO     workerDAO     = new WorkerDAO();
    private final TrustScoreDAO trustScoreDAO = new TrustScoreDAO();

    /**
     * Calculates and persists a new trust score for the given worker.
     * Uses DB transaction (inside TrustScoreDAO.updateScoreWithAudit).
     *
     * @return the newly calculated score (0–1000)
     * @throws WorkerNotFoundException   if workerId does not exist
     * @throws InvalidTrustScoreException if computed score is out of range
     * @throws SQLException              on DB error
     */
    public double calculateAndUpdate(int workerId)
            throws WorkerNotFoundException, InvalidTrustScoreException, SQLException {

        Worker worker = workerDAO.findById(workerId);
        if (worker == null) {
            throw new WorkerNotFoundException(workerId);
        }

        double oldScore = worker.getCurrentTrustScore();

        // 1. Consistency — blend of distinct work days + total gigs (highly responsive to new entries)
        int    workDays    = trustScoreDAO.getWorkDaysLast90(workerId);
        int    totalGigs   = trustScoreDAO.getWorkLogCountLast90(workerId);
        double consistency = Math.min(100.0, (workDays * 5.0) + (totalGigs * 2.0));

        // 2. Rating component — avg across all platforms, 0–5 mapped to 0–100
        double avgRating  = trustScoreDAO.getAvgRating(workerId);
        double ratingComp = avgRating * 20.0;

        // 3. Income component — MONOTONIC: more earnings = higher score, always
        //    incomeLevel: avg monthly income as % of Rs.20k target (90 pts max)
        //    regularityBonus: +10 pts for having 3+ months of recorded history
        double[] stats      = trustScoreDAO.getMonthlyEarningsStats(workerId);
        double   avgMonthly = stats[1];
        int      monthCount = trustScoreDAO.getMonthCount(workerId);
        double   incomeLevel  = Math.min(90.0, (avgMonthly / 20000.0) * 90.0);
        double   regularityBonus = (monthCount >= 3) ? 10.0 : (monthCount >= 1 ? 5.0 : 0.0);
        double   incomeComp  = Math.min(100.0, incomeLevel + regularityBonus);

        // Weighted sum (0–100), then scale to 0–1000
        double weighted = (0.40 * consistency) + (0.35 * ratingComp) + (0.25 * incomeComp);
        double newScore = Math.min(1000.0, Math.max(0.0, weighted * 10.0));

        // Validate (demonstrates custom exception)
        if (newScore < 0 || newScore > 1000) {
            throw new InvalidTrustScoreException(newScore);
        }

        // Persist atomically (transaction inside DAO)
        StringBuffer reason = new StringBuffer("Recalculated: ");
        reason.append(String.format(
            "Consistency=%.1f%% (%dd/90d), Rating=%.2f/5 (%.0fpts), "
            + "IncomeLevel=%.1f%% (avg Rs.%.0f/mo, %d months) => Score=%.1f",
            consistency, workDays, avgRating, ratingComp,
            incomeComp, avgMonthly, monthCount, newScore));

        trustScoreDAO.updateScoreWithAudit(workerId, oldScore, newScore, reason.toString());
        return newScore;
    }

    /**
     * Calculate score without persisting — for preview only.
     */
    public double previewScore(int workerId) throws SQLException, WorkerNotFoundException {
        Worker worker = workerDAO.findById(workerId);
        if (worker == null) throw new WorkerNotFoundException(workerId);

        int    days       = trustScoreDAO.getWorkDaysLast90(workerId);
        int    totalGigs  = trustScoreDAO.getWorkLogCountLast90(workerId);
        double consistency = Math.min(100.0, (days * 5.0) + (totalGigs * 2.0));
        
        double rating  = trustScoreDAO.getAvgRating(workerId);
        double[] stats = trustScoreDAO.getMonthlyEarningsStats(workerId);
        double avgMonthly  = stats[1];
        int    monthCount  = trustScoreDAO.getMonthCount(workerId);
        double incomeLevel = Math.min(90.0, (avgMonthly / 20000.0) * 90.0);
        double regularity  = (monthCount >= 3) ? 10.0 : (monthCount >= 1 ? 5.0 : 0.0);
        double incomeComp  = Math.min(100.0, incomeLevel + regularity);
        
        double weighted = (0.40 * consistency)
                        + (0.35 * rating * 20.0)
                        + (0.25 * incomeComp);
        return Math.min(1000.0, Math.max(0.0, weighted * 10.0));
    }
}

