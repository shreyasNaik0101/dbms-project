package com.suraksha.setu.dao;

import com.suraksha.setu.models.User;
import com.suraksha.setu.models.Worker;
import com.suraksha.setu.models.Admin;
import com.suraksha.setu.util.DatabaseConnection;
import com.suraksha.setu.util.PasswordHasher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO for app_users table — authentication and access control.
 */
public class AppUserDAO extends GenericDAO<User> {

    /**
     * Authenticate a user by username + password.
     * Returns a Worker or Admin instance, or null on failure.
     */
    public User authenticate(String username, String password) throws SQLException {
        String sql = "SELECT au.*, w.worker_id as wid, w.full_name, w.digital_work_id, "
                   + "w.phone, w.current_trust_score, w.joining_date "
                   + "FROM app_users au "
                   + "LEFT JOIN workers w ON au.worker_id = w.worker_id "
                   + "WHERE au.username = ?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setString(1, username.trim());
            rs = ps.executeQuery();
            if (!rs.next()) return null;

            String storedHash = rs.getString("password_hash");
            String role       = rs.getString("role");

            // Verify password
            if (!PasswordHasher.verify(password, storedHash)) return null;

            int userId = rs.getInt("user_id");

            if ("Admin".equals(role)) {
                Admin admin = new Admin(userId, username, "SuperAdmin");
                admin.setRole("Admin");
                return admin;
            } else {
                // Build Worker object
                Worker w = new Worker(
                    rs.getInt("wid"),
                    rs.getString("digital_work_id"),
                    rs.getString("full_name"),
                    rs.getString("phone"),
                    null,
                    rs.getDouble("current_trust_score"),
                    rs.getTimestamp("joining_date")
                );
                w.setUserId(userId);
                w.setUsername(username);
                w.setRole("Worker");
                return w;
            }
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }

    /** Register a new app user account. */
    public void registerUser(int workerId, String username, String password, String role) throws SQLException {
        String sql = "INSERT INTO app_users (worker_id, username, password_hash, role) VALUES (?,?,?,?)";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId);
            ps.setString(2, username);
            ps.setString(3, PasswordHasher.hash(password));
            ps.setString(4, role);
            ps.executeUpdate();
        } finally { DatabaseConnection.closeQuietly(ps, conn); }
    }

    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM app_users WHERE username=?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setString(1, username); rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }

    // ── GenericDAO stubs ─────────────────────────────────────────────────────
    @Override public void save(User entity) throws SQLException { /* use registerUser */ }
    @Override public User findById(int id) throws SQLException { return null; }
    @Override public List<User> findAll() throws SQLException { return new ArrayList<>(); }
    @Override public void update(User entity) throws SQLException { }
    @Override public void delete(int id) throws SQLException {
        String sql = "DELETE FROM app_users WHERE user_id=?";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); ps.setInt(1, id); ps.executeUpdate();
        } finally { DatabaseConnection.closeQuietly(ps, conn); }
    }
}
