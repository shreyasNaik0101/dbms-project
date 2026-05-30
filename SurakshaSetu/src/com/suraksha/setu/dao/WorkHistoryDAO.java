package com.suraksha.setu.dao;

import com.suraksha.setu.models.WorkHistory;
import com.suraksha.setu.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.StreamSupport;

/**
 * DAO for work_history table.
 * Demonstrates Iterator and Spliterator (Unit III).
 */
public class WorkHistoryDAO extends GenericDAO<WorkHistory> {

    @Override
    public void save(WorkHistory wh) throws SQLException {
        String insertSql = "INSERT INTO work_history (worker_id, platform_id, work_date, hours_logged, earnings, completion_date) "
                   + "VALUES (?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            conn.setAutoCommit(false);

            // 1. Insert into work_history
            ps = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, wh.getWorkerId());
            ps.setInt(2, wh.getPlatformId());
            ps.setDate(3, wh.getWorkDate());
            ps.setDouble(4, wh.getHoursLogged());
            ps.setDouble(5, wh.getEarnings());
            ps.setTimestamp(6, wh.getCompletionDate() != null
                ? new java.sql.Timestamp(wh.getCompletionDate().getTime()) : null);
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) wh.setEntryId(keys.getInt(1));
            DatabaseConnection.closeQuietly(keys);
            ps.close();

            // 2. Upsert monthly_earnings_summary for this month
            // Get the month_year string (YYYY-MM) from the work date
            String monthYear = wh.getWorkDate().toLocalDate().getYear()
                + "-" + String.format("%02d", wh.getWorkDate().toLocalDate().getMonthValue());

            // Check if a row exists for this worker+month
            ps = conn.prepareStatement(
                "SELECT summary_id, total_gross FROM monthly_earnings_summary "
                + "WHERE worker_id = ? AND month_year = ?");
            ps.setInt(1, wh.getWorkerId());
            ps.setString(2, monthYear);
            ResultSet existing = ps.executeQuery();
            if (existing.next()) {
                // Update existing row
                int summaryId = existing.getInt("summary_id");
                double newGross = existing.getDouble("total_gross") + wh.getEarnings();
                existing.close();
                ps.close();
                ps = conn.prepareStatement(
                    "UPDATE monthly_earnings_summary SET total_gross = ?, net_savings = total_gross - total_expenses "
                    + "WHERE summary_id = ?");
                ps.setDouble(1, newGross);
                ps.setInt(2, summaryId);
                ps.executeUpdate();
                ps.close();
            } else {
                // Insert new row for this month
                existing.close();
                ps.close();
                ps = conn.prepareStatement(
                    "INSERT INTO monthly_earnings_summary (worker_id, month_year, total_gross, total_expenses, net_savings) "
                    + "VALUES (?, ?, ?, 0, ?)");
                ps.setInt(1, wh.getWorkerId());
                ps.setString(2, monthYear);
                ps.setDouble(3, wh.getEarnings());
                ps.setDouble(4, wh.getEarnings());
                ps.executeUpdate();
                ps.close();
            }

            // 3. Ensure a performance_rating row exists for this worker+platform (default 4.0 rating)
            ps = conn.prepareStatement(
                "SELECT rating_id FROM performance_ratings WHERE worker_id = ? AND platform_id = ?");
            ps.setInt(1, wh.getWorkerId());
            ps.setInt(2, wh.getPlatformId());
            ResultSet ratingCheck = ps.executeQuery();
            boolean hasRating = ratingCheck.next();
            ratingCheck.close();
            ps.close();
            if (!hasRating) {
                ps = conn.prepareStatement(
                    "INSERT INTO performance_ratings (worker_id, platform_id, avg_rating, total_reviews) "
                    + "VALUES (?, ?, 4.0, 1)");
                ps.setInt(1, wh.getWorkerId());
                ps.setInt(2, wh.getPlatformId());
                ps.executeUpdate();
                ps.close();
            }

            conn.commit();
        } catch (SQLException e) {
            if (conn != null) { try { conn.rollback(); } catch (SQLException ignored) {} }
            throw e;
        } finally {
            if (conn != null) { try { conn.setAutoCommit(true); } catch (SQLException ignored) {} }
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    @Override
    public WorkHistory findById(int entryId) throws SQLException {
        String sql = "SELECT wh.*, gp.platform_name FROM work_history wh "
                   + "JOIN gig_platforms gp ON wh.platform_id = gp.platform_id "
                   + "WHERE wh.work_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, entryId);
            rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    @Override
    public List<WorkHistory> findAll() throws SQLException {
        return findByWorkerId(-1);
    }

    /**
     * Fetch all work history for a given worker.
     * Pass workerId = -1 to fetch all workers.
     */
    public List<WorkHistory> findByWorkerId(int workerId) throws SQLException {
        String sql;
        if (workerId == -1) {
            sql = "SELECT wh.*, gp.platform_name FROM work_history wh "
                + "JOIN gig_platforms gp ON wh.platform_id = gp.platform_id "
                + "ORDER BY wh.work_date DESC";
        } else {
            sql = "SELECT wh.*, gp.platform_name FROM work_history wh "
                + "JOIN gig_platforms gp ON wh.platform_id = gp.platform_id "
                + "WHERE wh.worker_id = ? ORDER BY wh.work_date DESC";
        }
        List<WorkHistory> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            if (workerId != -1) ps.setInt(1, workerId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /** Fetch work history for the last N days for a worker. */
    public List<WorkHistory> findRecentDays(int workerId, int days) throws SQLException {
        String sql = "SELECT wh.*, gp.platform_name FROM work_history wh "
                   + "JOIN gig_platforms gp ON wh.platform_id = gp.platform_id "
                   + "WHERE wh.worker_id = ? AND wh.work_date >= CURRENT_DATE - (? * INTERVAL '1 day') "
                   + "ORDER BY wh.work_date DESC";
        List<WorkHistory> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            ps.setInt(2, days);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /**
     * Iterate over logs using explicit Iterator (Unit III requirement).
     * Sums total earnings using Iterator pattern.
     */
    public double sumEarningsWithIterator(List<WorkHistory> logs) {
        double total = 0.0;
        Iterator<WorkHistory> it = logs.iterator();
        while (it.hasNext()) {
            total += it.next().getEarnings();
        }
        return total;
    }

    /**
     * Sum earnings using Spliterator (Unit III requirement).
     */
    public double sumEarningsWithSpliterator(List<WorkHistory> logs) {
        Spliterator<WorkHistory> split = logs.spliterator();
        double[] sum = {0.0};
        split.forEachRemaining(wh -> sum[0] += wh.getEarnings());
        return sum[0];
    }

    @Override
    public void update(WorkHistory wh) throws SQLException {
        String sql = "UPDATE work_history SET hours_logged=?, earnings=? WHERE work_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setDouble(1, wh.getHoursLogged());
            ps.setDouble(2, wh.getEarnings());
            ps.setInt(3, wh.getEntryId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    @Override
    public void delete(int entryId) throws SQLException {
        String sql = "DELETE FROM work_history WHERE work_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, entryId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    private WorkHistory mapRow(ResultSet rs) throws SQLException {
        WorkHistory wh = new WorkHistory(
            rs.getInt("work_id"),   // PK column is work_id in PostgreSQL schema
            rs.getInt("worker_id"),
            rs.getInt("platform_id"),
            rs.getDate("work_date"),
            rs.getDouble("hours_logged"),
            rs.getDouble("earnings"),
            rs.getTimestamp("completion_date") != null
                ? new java.sql.Date(rs.getTimestamp("completion_date").getTime()) : null
        );
        try { wh.setPlatformName(rs.getString("platform_name")); } catch (SQLException ignored) {}
        return wh;
    }
}
