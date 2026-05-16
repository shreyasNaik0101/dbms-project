package com.suraksha.setu.dao;

import com.suraksha.setu.models.Worker;
import com.suraksha.setu.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO for workers table — CRUD with PreparedStatement + ResultSet.
 * Demonstrates Unit IV JDBC requirements.
 */
public class WorkerDAO extends GenericDAO<Worker> {

    /** INSERT a new worker. */
    @Override
    public void save(Worker w) throws SQLException {
        String sql = "INSERT INTO workers (digital_work_id, full_name, phone, aadhaar_hash, current_trust_score) "
                   + "VALUES (?, ?, ?, ?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, w.getDigitalWorkId());
            ps.setString(2, w.getFullName());
            ps.setString(3, w.getPhone());
            ps.setString(4, w.getAadhaarHash());
            ps.setDouble(5, w.getCurrentTrustScore());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) {
                w.setWorkerId(keys.getInt(1));
            }
            DatabaseConnection.closeQuietly(keys);
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    /** SELECT worker by ID. Returns null if not found. */
    @Override
    public Worker findById(int workerId) throws SQLException {
        String sql = "SELECT * FROM workers WHERE worker_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /** SELECT worker by digital_work_id. */
    public Worker findByDigitalId(String digitalId) throws SQLException {
        String sql = "SELECT * FROM workers WHERE digital_work_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setString(1, digitalId);
            rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /** SELECT all workers — returns ArrayList (Unit III). */
    @Override
    public List<Worker> findAll() throws SQLException {
        String sql = "SELECT * FROM workers ORDER BY current_trust_score DESC";
        List<Worker> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            rs   = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } finally {
            DatabaseConnection.closeQuietly(rs, ps, conn);
        }
    }

    /**
     * Returns all workers as a HashMap<workerId, Worker> for O(1) lookup.
     * Demonstrates Unit III HashMap.
     */
    public Map<Integer, Worker> findAllAsMap() throws SQLException {
        List<Worker> all = findAll();
        Map<Integer, Worker> map = new HashMap<>();
        // Autoboxing: int -> Integer (Unit II)
        for (Worker w : all) {
            map.put(w.getWorkerId(), w);
        }
        return map;
    }

    /** UPDATE worker record. */
    @Override
    public void update(Worker w) throws SQLException {
        String sql = "UPDATE workers SET full_name=?, phone=?, current_trust_score=? WHERE worker_id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setString(1, w.getFullName());
            ps.setString(2, w.getPhone());
            ps.setDouble(3, w.getCurrentTrustScore());
            ps.setInt(4, w.getWorkerId());
            ps.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    /** DELETE worker by ID. */
    @Override
    public void delete(int workerId) throws SQLException {
        String sql = "DELETE FROM workers WHERE worker_id = ?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            ps.executeUpdate();
        } finally {
            DatabaseConnection.closeQuietly(ps, conn);
        }
    }

    /** Map a ResultSet row to a Worker object. */
    private Worker mapRow(ResultSet rs) throws SQLException {
        return new Worker(
            rs.getInt("worker_id"),
            rs.getString("digital_work_id"),
            rs.getString("full_name"),
            rs.getString("phone"),
            rs.getString("aadhaar_hash"),
            rs.getDouble("current_trust_score"),
            rs.getTimestamp("joining_date")
        );
    }
}
