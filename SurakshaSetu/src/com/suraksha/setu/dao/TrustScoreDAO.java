package com.suraksha.setu.dao;

import com.suraksha.setu.models.TrustScoreAudit;
import com.suraksha.setu.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for trust score updates and audit trail.
 * Demonstrates JDBC Transactions: BEGIN / COMMIT / ROLLBACK (Unit IV).
 */
public class TrustScoreDAO extends GenericDAO<TrustScoreAudit> {

    /**
     * Update worker trust score AND insert audit record — atomic transaction.
     * If either fails, rolls back both changes.
     */
    public void updateScoreWithAudit(int workerId, double oldScore,
                                     double newScore, String reason) throws SQLException {
        Connection conn = null;
        PreparedStatement updatePs = null;
        PreparedStatement auditPs  = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);  // BEGIN TRANSACTION

            // 1. Update workers table
            updatePs = conn.prepareStatement(
                "UPDATE workers SET current_trust_score = ? WHERE worker_id = ?");
            updatePs.setDouble(1, newScore);
            updatePs.setInt(2, workerId);
            updatePs.executeUpdate();

            // 2. Insert audit record
            auditPs = conn.prepareStatement(
                "INSERT INTO trust_score_audit (worker_id, old_score, new_score, reason) VALUES (?,?,?,?)");
            auditPs.setInt(1, workerId);
            auditPs.setDouble(2, oldScore);
            auditPs.setDouble(3, newScore);
            auditPs.setString(4, reason);
            auditPs.executeUpdate();

            conn.commit();  // COMMIT
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {
                    System.err.println("[ERROR] Rollback failed: " + ex.getMessage());
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException ignored) {}
            }
            DatabaseConnection.closeQuietly(updatePs, auditPs, conn);
        }
    }

    /** Fetch audit trail for a worker (newest first). */
    public List<TrustScoreAudit> findByWorkerId(int workerId) throws SQLException {
        String sql = "SELECT * FROM trust_score_audit WHERE worker_id = ? ORDER BY calculated_at DESC";
        List<TrustScoreAudit> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /** Fetch performance ratings for trust score calculation. */
    public double getAvgRating(int workerId) throws SQLException {
        String sql = "SELECT AVG(avg_rating) AS overall FROM performance_ratings WHERE worker_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getDouble("overall") : 0.0;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /** Count distinct work days in last 30 days. */
    public int getWorkDaysLast30(int workerId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT work_date) AS days FROM work_history "
                   + "WHERE worker_id = ? AND work_date >= CURRENT_DATE - INTERVAL '30 days'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt("days") : 0;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /** Count distinct work days in last 90 days — used for trust score consistency. */
    public int getWorkDaysLast90(int workerId) throws SQLException {
        String sql = "SELECT COUNT(DISTINCT work_date) AS days FROM work_history "
                   + "WHERE worker_id = ? AND work_date >= CURRENT_DATE - INTERVAL '90 days'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt("days") : 0;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /** Count total work logs in last 90 days. */
    public int getWorkLogCountLast90(int workerId) throws SQLException {
        String sql = "SELECT COUNT(*) AS total FROM work_history "
                   + "WHERE worker_id = ? AND work_date >= CURRENT_DATE - INTERVAL '90 days'";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt("total") : 0;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /** Count how many distinct months a worker has any earnings recorded. */
    public int getMonthCount(int workerId) throws SQLException {
        String sql = "SELECT COUNT(*) AS cnt FROM monthly_earnings_summary WHERE worker_id = ? AND total_gross > 0";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            if (rs.next() && rs.getInt("cnt") > 0) return rs.getInt("cnt");
            // Fallback: count distinct months in work_history
            DatabaseConnection.closeQuietly(rs, ps, null);
            sql = "SELECT COUNT(DISTINCT TO_CHAR(work_date, 'YYYY-MM')) AS cnt FROM work_history WHERE worker_id = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt("cnt") : 0;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /** Get monthly earnings stats for income stability calculation.
     *  Returns [minMonthly, avgMonthly] from monthly_earnings_summary.
     *  Falls back to computing directly from work_history if summary is empty.
     */
    public double[] getMonthlyEarningsStats(int workerId) throws SQLException {
        // First try monthly_earnings_summary
        String sql = "SELECT MIN(total_gross) AS min_g, AVG(total_gross) AS avg_g, COUNT(*) AS months "
                   + "FROM monthly_earnings_summary WHERE worker_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            if (rs.next() && rs.getInt("months") > 0) {
                double minG = rs.getDouble("min_g");
                double avgG = rs.getDouble("avg_g");
                if (avgG > 0) {
                    return new double[]{ minG, avgG };
                }
            }
            // Fallback: compute per-month totals directly from work_history
            DatabaseConnection.closeQuietly(rs, ps, null);
            sql = "SELECT MIN(monthly_total) AS min_g, AVG(monthly_total) AS avg_g "
                + "FROM (SELECT TO_CHAR(work_date, 'YYYY-MM') AS mo, SUM(earnings) AS monthly_total "
                +       "FROM work_history WHERE worker_id = ? GROUP BY TO_CHAR(work_date, 'YYYY-MM')) AS monthly";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                double minG = rs.getDouble("min_g");
                double avgG = rs.getDouble("avg_g");
                return new double[]{ minG, avgG };
            }
            return new double[]{0.0, 0.0};
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    // ── GenericDAO implementations ────────────────────────────────────────────

    @Override
    public void save(TrustScoreAudit entity) throws SQLException {
        String sql = "INSERT INTO trust_score_audit (worker_id, old_score, new_score, reason) VALUES (?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, entity.getWorkerId());
            ps.setDouble(2, entity.getOldScore());
            ps.setDouble(3, entity.getNewScore());
            ps.setString(4, entity.getReason());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    @Override
    public TrustScoreAudit findById(int auditId) throws SQLException {
        String sql = "SELECT * FROM trust_score_audit WHERE audit_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, auditId);
            rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    @Override
    public List<TrustScoreAudit> findAll() throws SQLException {
        String sql = "SELECT * FROM trust_score_audit ORDER BY calculated_at DESC";
        List<TrustScoreAudit> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            rs   = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    @Override
    public void update(TrustScoreAudit entity) throws SQLException {
        // Audit records are immutable — no update
        throw new UnsupportedOperationException("Audit records cannot be modified.");
    }

    @Override
    public void delete(int auditId) throws SQLException {
        String sql = "DELETE FROM trust_score_audit WHERE audit_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, auditId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    private TrustScoreAudit mapRow(ResultSet rs) throws SQLException {
        return new TrustScoreAudit(
            rs.getInt("audit_id"),
            rs.getInt("worker_id"),
            rs.getDouble("old_score"),
            rs.getDouble("new_score"),
            rs.getString("reason"),
            rs.getTimestamp("calculated_at")
        );
    }
}
