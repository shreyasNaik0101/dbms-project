package com.suraksha.setu.services;

import com.suraksha.setu.dao.WorkHistoryDAO;
import com.suraksha.setu.models.WorkHistory;
import com.suraksha.setu.util.DatabaseConnection;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Generates monthly earnings reports using Streams & Lambda (Unit II).
 */
public class ReportService {

    private final WorkHistoryDAO workHistoryDAO = new WorkHistoryDAO();

    /**
     * Get all work logs for a worker as a sorted list.
     * Demonstrates Comparator (Unit III): sort by date (newest first).
     */
    public List<WorkHistory> getWorkLogsForWorker(int workerId) throws SQLException {
        List<WorkHistory> logs = workHistoryDAO.findByWorkerId(workerId);

        // Custom Comparator — newest date first (Unit III)
        logs.sort(Comparator.comparing(WorkHistory::getWorkDate).reversed());
        return logs;
    }

    /**
     * Calculate total earnings from work logs using Stream + Lambda (Unit II).
     */
    public double getTotalEarnings(List<WorkHistory> logs) {
        return logs.stream()
                   .mapToDouble(WorkHistory::getEarnings)
                   .sum();
    }

    /**
     * Calculate total hours from work logs.
     */
    public double getTotalHours(List<WorkHistory> logs) {
        return logs.stream()
                   .mapToDouble(WorkHistory::getHoursLogged)
                   .sum();
    }

    /**
     * Group work logs by platform name using Collectors.groupingBy (Lambda).
     * Returns Map<platformName, List<WorkHistory>>.
     */
    public Map<String, List<WorkHistory>> groupByPlatform(List<WorkHistory> logs) {
        return logs.stream()
                   .collect(Collectors.groupingBy(
                       wh -> (wh.getPlatformName() != null ? wh.getPlatformName() : "Unknown")
                   ));
    }

    /**
     * Get monthly summary data for JTable display.
     * Returns list of Object[] rows: [month_year, total_gross, total_expenses, net_savings]
     */
    public List<Object[]> getMonthlySummaryRows(int workerId) throws SQLException {
        String sql = "SELECT month_year, total_gross, total_expenses, net_savings "
                   + "FROM monthly_earnings_summary WHERE worker_id=? ORDER BY month_year DESC";
        List<Object[]> rows = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); ps.setInt(1, workerId);
            rs   = ps.executeQuery();
            while (rs.next()) {
                rows.add(new Object[]{
                    rs.getString("month_year"),
                    String.format("Rs. %.2f", rs.getDouble("total_gross")),
                    String.format("Rs. %.2f", rs.getDouble("total_expenses")),
                    String.format("Rs. %.2f", rs.getDouble("net_savings"))
                });
            }
            return rows;
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }

    /**
     * Net income = gross - expenses.
     * Uses Lambda / stream over work logs.
     */
    public double getNetIncome(int workerId) throws SQLException {
        List<WorkHistory> logs = workHistoryDAO.findByWorkerId(workerId);
        double gross = logs.stream().mapToDouble(WorkHistory::getEarnings).sum();
        double expenses = getExpenses(workerId);
        return gross - expenses;
    }

    private double getExpenses(int workerId) throws SQLException {
        String sql = "SELECT SUM(amount) as total FROM operational_expenses WHERE worker_id=?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); ps.setInt(1, workerId);
            rs   = ps.executeQuery();
            return rs.next() ? rs.getDouble("total") : 0.0;
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }
}
