package com.suraksha.setu.services;

import com.suraksha.setu.dao.LoanApplicationDAO;
import com.suraksha.setu.exceptions.LoanApplicationException;
import com.suraksha.setu.exceptions.InsufficientEarningsException;
import com.suraksha.setu.models.LoanApplication;
import com.suraksha.setu.models.Worker;
import com.suraksha.setu.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for loan eligibility checking and application.
 */
public class LoanEligibilityService {

    private final LoanApplicationDAO loanDAO = new LoanApplicationDAO();

    /**
     * Result object for eligibility check.
     */
    public static class EligibilityResult {
        public final boolean eligible;
        public final double  maxLoanAmount;
        public final String  reason;

        EligibilityResult(boolean eligible, double maxLoanAmount, String reason) {
            this.eligible      = eligible;
            this.maxLoanAmount = maxLoanAmount;
            this.reason        = reason;
        }
    }

    /**
     * Loan provider lookup row.
     */
    public static class LoanProvider {
        public final int    providerId;
        public final String providerName;
        public final double minTrustScore;

        LoanProvider(int id, String name, double minScore) {
            this.providerId    = id;
            this.providerName  = name;
            this.minTrustScore = minScore;
        }

        @Override public String toString() {
            return providerName + " (Min Score: " + (int) minTrustScore + ")";
        }
    }

    /**
     * Check if a worker is eligible for loans.
     *
     * Logic:
     *   - Trust score >= provider minimum
     *   - At least 3 months of work history
     *   - Average monthly income >= 5000
     *
     * @throws InsufficientEarningsException if income too low (demonstrates exception chaining)
     */
    public EligibilityResult checkEligibility(Worker worker)
            throws InsufficientEarningsException, SQLException {

        double trustScore    = worker.getCurrentTrustScore();
        double[] earningStats = getEarningStats(worker.getWorkerId());
        double avgMonthly    = earningStats[0];
        int    monthsActive  = (int) earningStats[1];

        final double MIN_INCOME  = 5000.0;
        final int    MIN_MONTHS  = 3;
        final double MAX_LOAN_MULTIPLIER = 2.0;
        final double ABSOLUTE_MAX = 50000.0;

        if (avgMonthly < MIN_INCOME) {
            throw new InsufficientEarningsException(MIN_INCOME, avgMonthly);
        }

        if (monthsActive < MIN_MONTHS) {
            return new EligibilityResult(false, 0,
                "Requires at least " + MIN_MONTHS + " months of work history. Currently: " + monthsActive);
        }

        if (trustScore < 400) {
            return new EligibilityResult(false, 0,
                "Trust score " + (int)trustScore + " is below minimum threshold of 400.");
        }

        double maxLoan = Math.min(avgMonthly * MAX_LOAN_MULTIPLIER, ABSOLUTE_MAX);
        return new EligibilityResult(true, maxLoan,
            String.format("Eligible! Max loan: ₹%.0f based on avg income ₹%.0f", maxLoan, avgMonthly));
    }

    /** Get all loan providers from DB. */
    public List<LoanProvider> getAllProviders() throws SQLException {
        String sql = "SELECT * FROM loan_providers ORDER BY min_trust_score_required";
        List<LoanProvider> list = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new LoanProvider(rs.getInt("provider_id"),
                        rs.getString("provider_name"),
                        rs.getDouble("min_trust_score")));
            }
            return list;
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }

    /** Get providers for which the worker is eligible based on trust score. */
    public List<LoanProvider> getEligibleProviders(double trustScore) throws SQLException {
        List<LoanProvider> all = getAllProviders();
        List<LoanProvider> eligible = new ArrayList<>();
        // Lambda-equivalent using for-each
        for (LoanProvider p : all) {
            if (trustScore >= p.minTrustScore) eligible.add(p);
        }
        return eligible;
    }

    /**
     * Submit a loan application.
     * @throws LoanApplicationException if worker already has a pending loan with same provider
     */
    public void applyForLoan(int workerId, int providerId, double amount)
            throws LoanApplicationException, SQLException {
        // Check for duplicate pending application
        List<LoanApplication> existing = loanDAO.findByWorkerId(workerId);
        for (LoanApplication la : existing) {
            if (la.getProviderId() == providerId && "Applied".equals(la.getStatus())) {
                throw new LoanApplicationException(
                    "A pending application already exists with this provider.",
                    new IllegalStateException("Duplicate loan application"));
            }
        }
        LoanApplication la = new LoanApplication();
        la.setWorkerId(workerId);
        la.setProviderId(providerId);
        la.setLoanAmount(amount);
        la.setStatus("Applied");
        loanDAO.save(la);
    }

    private double[] getEarningStats(int workerId) throws SQLException {
        String sql = "SELECT AVG(total_gross) as avg_g, COUNT(*) as months "
                   + "FROM monthly_earnings_summary WHERE worker_id=?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); ps.setInt(1, workerId);
            rs   = ps.executeQuery();
            if (rs.next()) return new double[]{ rs.getDouble("avg_g"), rs.getDouble("months") };
            return new double[]{0.0, 0.0};
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }
}
