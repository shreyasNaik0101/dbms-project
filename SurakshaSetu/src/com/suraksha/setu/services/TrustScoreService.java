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
 *   Score = (0.4 × Consistency) + (0.35 × RatingScore) + (0.25 × IncomeScore)
 *
 *   Consistency  = (workDaysLast90 / 90.0) × 100    [capped at 100]
 *   RatingScore  = avgRating × 20                   [maps 0–5 → 0–100]
 *   IncomeScore  = (stability × 0.5) + (incomeLevel × 0.5)
 *     stability  = (minMonthly / avgMonthly) × 100  [how steady income is]
 *     incomeLevel= min(avgMonthly / 10000.0, 1.0) × 100  [scales up to Rs.10k/month = 100]
 *
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

        // 1. Consistency — work days in last 90 days (fairer window for new workers)
        int    workDays    = trustScoreDAO.getWorkDaysLast90(workerId);
        double consistency = Math.min(100.0, (workDays / 90.0) * 100.0);

        // 2. Rating component — avg across all platforms, 0–5 mapped to 0–100
        double avgRating  = trustScoreDAO.getAvgRating(workerId);
        double ratingComp = avgRating * 20.0;

        // 3. Income component — blend of stability + absolute income level
        double[] stats       = trustScoreDAO.getMonthlyEarningsStats(workerId);
        double   minMonthly  = stats[0];
        double   avgMonthly  = stats[1];
        double   stability   = (avgMonthly > 0) ? Math.min(100.0, (minMonthly / avgMonthly) * 100.0) : 0.0;
        double   incomeLevel = Math.min(100.0, (avgMonthly / 10000.0) * 100.0); // Rs.10k/month = 100%
        double   incomeComp  = (stability * 0.5) + (incomeLevel * 0.5);

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
            "Consistency=%.1f%% (%dd/90d), Rating=%.2f/5 (%.0fpts), " +
            "IncomeStability=%.1f%%, IncomeLevel=%.1f%% (avg Rs.%.0f/mo) => Score=%.1f",
            consistency, workDays, avgRating, ratingComp,
            stability, incomeLevel, avgMonthly, newScore));

        trustScoreDAO.updateScoreWithAudit(workerId, oldScore, newScore, reason.toString());
        return newScore;
    }

    /**
     * Calculate score without persisting — for preview only.
     */
    public double previewScore(int workerId) throws SQLException, WorkerNotFoundException {
        Worker worker = workerDAO.findById(workerId);
        if (worker == null) throw new WorkerNotFoundException(workerId);

        int    days    = trustScoreDAO.getWorkDaysLast90(workerId);
        double rating  = trustScoreDAO.getAvgRating(workerId);
        double[] stats = trustScoreDAO.getMonthlyEarningsStats(workerId);
        double stability  = (stats[1] > 0) ? Math.min(100.0, (stats[0] / stats[1]) * 100.0) : 0.0;
        double incomeLevel = Math.min(100.0, (stats[1] / 10000.0) * 100.0);
        double incomeComp  = (stability * 0.5) + (incomeLevel * 0.5);
        double weighted = (0.40 * Math.min(100.0, (days / 90.0) * 100.0))
                        + (0.35 * rating * 20.0)
                        + (0.25 * incomeComp);
        return Math.min(1000.0, Math.max(0.0, weighted * 10.0));
    }
}

