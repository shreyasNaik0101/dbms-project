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
 *   Score = (0.4 × Consistency) + (0.3 × RatingAvg) + (0.3 × IncomeStability)
 *
 *   Consistency     = (workDaysLast30 / 30.0) × 100
 *   RatingAvg       = avgRating × 20          [maps 0-5 → 0-100]
 *   IncomeStability = (minMonthly / avgMonthly) × 100
 *
 *   Final = weightedSum × 10                  [scales 0-100 → 0-1000]
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

        // 1. Consistency component
        int    workDays    = trustScoreDAO.getWorkDaysLast30(workerId);
        double consistency = (workDays / 30.0) * 100.0;

        // 2. Rating component
        double avgRating   = trustScoreDAO.getAvgRating(workerId);
        double ratingComp  = avgRating * 20.0;   // 0–5 mapped to 0–100

        // 3. Income stability component
        double[] stats          = trustScoreDAO.getMonthlyEarningsStats(workerId);
        double   minMonthly     = stats[0];
        double   avgMonthly     = stats[1];
        double   incomeStability = (avgMonthly > 0) ? (minMonthly / avgMonthly) * 100.0 : 0.0;

        // Weighted sum (0–100), then scale to 0–1000
        double weighted  = (0.4 * consistency) + (0.3 * ratingComp) + (0.3 * incomeStability);
        double newScore  = Math.min(1000.0, Math.max(0.0, weighted * 10.0));

        // Validate (demonstrates custom exception)
        if (newScore < 0 || newScore > 1000) {
            throw new InvalidTrustScoreException(newScore);
        }

        // Persist atomically (transaction inside DAO)
        StringBuffer reason = new StringBuffer("Recalculated: ");
        reason.append(String.format("Consistency=%.1f%%, Rating=%.2f, Stability=%.1f%%",
                consistency, avgRating, incomeStability));

        trustScoreDAO.updateScoreWithAudit(workerId, oldScore, newScore, reason.toString());
        return newScore;
    }

    /**
     * Calculate score without persisting — for preview only.
     */
    public double previewScore(int workerId) throws SQLException, WorkerNotFoundException {
        Worker worker = workerDAO.findById(workerId);
        if (worker == null) throw new WorkerNotFoundException(workerId);

        int    days    = trustScoreDAO.getWorkDaysLast30(workerId);
        double rating  = trustScoreDAO.getAvgRating(workerId);
        double[] stats = trustScoreDAO.getMonthlyEarningsStats(workerId);
        double stability = (stats[1] > 0) ? (stats[0] / stats[1]) * 100.0 : 0.0;
        double weighted  = (0.4 * (days / 30.0) * 100.0)
                         + (0.3 * rating * 20.0)
                         + (0.3 * stability);
        return Math.min(1000.0, Math.max(0.0, weighted * 10.0));
    }
}
