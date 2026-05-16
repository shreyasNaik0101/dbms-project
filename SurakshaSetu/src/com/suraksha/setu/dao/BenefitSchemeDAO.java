package com.suraksha.setu.dao;

import com.suraksha.setu.models.BenefitScheme;
import com.suraksha.setu.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for benefit_schemes and scheme_enrollments tables. */
public class BenefitSchemeDAO extends GenericDAO<BenefitScheme> {

    @Override
    public void save(BenefitScheme bs) throws SQLException {
        String sql = "INSERT INTO benefit_schemes (scheme_name, coverage_amount, premium_cost) VALUES (?,?,?)";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, bs.getSchemeName());
            ps.setDouble(2, bs.getCoverageAmount());
            ps.setDouble(3, bs.getPremiumCost());
            ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) bs.setSchemeId(keys.getInt(1));
            DatabaseConnection.closeQuietly(keys);
        } finally { DatabaseConnection.closeQuietly(ps, conn); }
    }

    @Override
    public BenefitScheme findById(int id) throws SQLException {
        String sql = "SELECT * FROM benefit_schemes WHERE scheme_id=?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, id); rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }

    @Override
    public List<BenefitScheme> findAll() throws SQLException {
        String sql = "SELECT * FROM benefit_schemes ORDER BY premium_cost";
        List<BenefitScheme> list = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }

    @Override
    public void update(BenefitScheme bs) throws SQLException {
        String sql = "UPDATE benefit_schemes SET scheme_name=?, coverage_amount=?, premium_cost=? WHERE scheme_id=?";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setString(1, bs.getSchemeName()); ps.setDouble(2, bs.getCoverageAmount());
            ps.setDouble(3, bs.getPremiumCost()); ps.setInt(4, bs.getSchemeId());
            ps.executeUpdate();
        } finally { DatabaseConnection.closeQuietly(ps, conn); }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM benefit_schemes WHERE scheme_id=?";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); ps.setInt(1, id); ps.executeUpdate();
        } finally { DatabaseConnection.closeQuietly(ps, conn); }
    }

    /** Enroll a worker in a scheme. */
    public void enrollWorker(int workerId, int schemeId, java.sql.Date startDate) throws SQLException {
        String sql = "INSERT INTO scheme_enrollments (worker_id, scheme_id, start_date, status) VALUES (?,?,?,'Active')";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql);
            ps.setInt(1, workerId); ps.setInt(2, schemeId); ps.setDate(3, startDate);
            ps.executeUpdate();
        } finally { DatabaseConnection.closeQuietly(ps, conn); }
    }

    /** Get schemes a worker is enrolled in. */
    public List<BenefitScheme> findEnrolledSchemes(int workerId) throws SQLException {
        String sql = "SELECT bs.* FROM benefit_schemes bs "
                   + "JOIN scheme_enrollments se ON bs.scheme_id = se.scheme_id "
                   + "WHERE se.worker_id = ? AND se.status = 'Active'";
        List<BenefitScheme> list = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); ps.setInt(1, workerId);
            rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }

    private BenefitScheme mapRow(ResultSet rs) throws SQLException {
        return new BenefitScheme(rs.getInt("scheme_id"), rs.getString("scheme_name"),
                rs.getDouble("coverage_amount"), rs.getDouble("premium_cost"));
    }
}
