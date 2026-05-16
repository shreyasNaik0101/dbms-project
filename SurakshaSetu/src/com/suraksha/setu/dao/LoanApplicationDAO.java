package com.suraksha.setu.dao;

import com.suraksha.setu.models.LoanApplication;
import com.suraksha.setu.util.DatabaseConnection;

import java.sql.*;
import java.util.*;

/**
 * DAO for loan_applications table.
 * Demonstrates PriorityQueue (Unit III) for processing loans by trust score.
 */
public class LoanApplicationDAO extends GenericDAO<LoanApplication> {

    @Override
    public void save(LoanApplication la) throws SQLException {
        String sql = "INSERT INTO loan_applications (worker_id, provider_id, loan_amount, status) VALUES (?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, la.getWorkerId());
            ps.setInt(2, la.getProviderId());
            ps.setDouble(3, la.getLoanAmount());
            ps.setString(4, la.getStatus() != null ? la.getStatus() : "Applied");
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) la.setLoanId(keys.getInt(1));
            DatabaseConnection.closeQuietly(keys);
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    @Override
    public LoanApplication findById(int loanId) throws SQLException {
        String sql = "SELECT la.*, w.full_name, w.current_trust_score, lp.provider_name "
                   + "FROM loan_applications la "
                   + "JOIN workers w ON la.worker_id = w.worker_id "
                   + "JOIN loan_providers lp ON la.provider_id = lp.provider_id "
                   + "WHERE la.loan_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, loanId);
            rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    @Override
    public List<LoanApplication> findAll() throws SQLException {
        String sql = "SELECT la.*, w.full_name, w.current_trust_score, lp.provider_name "
                   + "FROM loan_applications la "
                   + "JOIN workers w ON la.worker_id = w.worker_id "
                   + "JOIN loan_providers lp ON la.provider_id = lp.provider_id "
                   + "ORDER BY la.applied_date DESC";
        List<LoanApplication> list = new ArrayList<>();
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

    public List<LoanApplication> findByWorkerId(int workerId) throws SQLException {
        String sql = "SELECT la.*, w.full_name, w.current_trust_score, lp.provider_name "
                   + "FROM loan_applications la "
                   + "JOIN workers w ON la.worker_id = w.worker_id "
                   + "JOIN loan_providers lp ON la.provider_id = lp.provider_id "
                   + "WHERE la.worker_id = ? ORDER BY la.applied_date DESC";
        List<LoanApplication> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs   = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    @Override
    public void update(LoanApplication la) throws SQLException {
        String sql = "UPDATE loan_applications SET status=? WHERE loan_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setString(1, la.getStatus());
            ps.setInt(2, la.getLoanId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    @Override
    public void delete(int loanId) throws SQLException {
        String sql = "DELETE FROM loan_applications WHERE loan_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, loanId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    /**
     * Load all pending applications into a PriorityQueue ordered by trust score.
     * Demonstrates Unit III PriorityQueue + LoanApplication.compareTo().
     */
    public PriorityQueue<LoanApplication> getPendingQueue() throws SQLException {
        List<LoanApplication> all = findAll();
        PriorityQueue<LoanApplication> queue = new PriorityQueue<>();
        for (LoanApplication la : all) {
            if ("Applied".equals(la.getStatus())) {
                queue.offer(la);
            }
        }
        return queue;
    }

    private LoanApplication mapRow(ResultSet rs) throws SQLException {
        LoanApplication la = new LoanApplication(
            rs.getInt("loan_id"),
            rs.getInt("worker_id"),
            rs.getInt("provider_id"),
            rs.getDouble("loan_amount"),
            rs.getString("status"),
            rs.getTimestamp("applied_date")
        );
        try { la.setWorkerName(rs.getString("full_name")); } catch (SQLException ignored) {}
        try { la.setProviderName(rs.getString("provider_name")); } catch (SQLException ignored) {}
        try { la.setWorkerTrustScore(rs.getDouble("current_trust_score")); } catch (SQLException ignored) {}
        return la;
    }
}
