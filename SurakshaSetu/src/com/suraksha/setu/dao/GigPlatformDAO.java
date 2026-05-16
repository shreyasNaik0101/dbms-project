package com.suraksha.setu.dao;

import com.suraksha.setu.models.GigPlatform;
import com.suraksha.setu.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * DAO for gig_platforms table.
 * Demonstrates HashSet<String> for unique skill/platform names (Unit III).
 */
public class GigPlatformDAO extends GenericDAO<GigPlatform> {

    @Override
    public void save(GigPlatform gp) throws SQLException {
        String sql = "INSERT INTO gig_platforms (platform_name) VALUES (?)";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, gp.getPlatformName()); ps.executeUpdate();
            ResultSet keys = ps.getGeneratedKeys();
            if (keys.next()) gp.setPlatformId(keys.getInt(1));
            DatabaseConnection.closeQuietly(keys);
        } finally { DatabaseConnection.closeQuietly(ps, conn); }
    }

    @Override
    public GigPlatform findById(int id) throws SQLException {
        String sql = "SELECT * FROM gig_platforms WHERE platform_id=?";
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); ps.setInt(1, id); rs = ps.executeQuery();
            return rs.next() ? mapRow(rs) : null;
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }

    @Override
    public List<GigPlatform> findAll() throws SQLException {
        String sql = "SELECT * FROM gig_platforms ORDER BY platform_name";
        List<GigPlatform> list = new ArrayList<>();
        Connection conn = null; PreparedStatement ps = null; ResultSet rs = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
            return list;
        } finally { DatabaseConnection.closeQuietly(rs, ps, conn); }
    }

    @Override public void update(GigPlatform gp) throws SQLException { /* lookup table — no update */ }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM gig_platforms WHERE platform_id=?";
        Connection conn = null; PreparedStatement ps = null;
        try {
            conn = DatabaseConnection.getConnection();
            ps   = conn.prepareStatement(sql); ps.setInt(1, id); ps.executeUpdate();
        } finally { DatabaseConnection.closeQuietly(ps, conn); }
    }

    /**
     * Returns unique platform names as a HashSet<String>.
     * Demonstrates Unit III: HashSet for unique elements.
     */
    public Set<String> getAllPlatformNamesAsSet() throws SQLException {
        List<GigPlatform> all = findAll();
        Set<String> nameSet = new HashSet<>();
        for (GigPlatform gp : all) {
            nameSet.add(gp.getPlatformName());
        }
        return nameSet;
    }

    /** Returns platform names as a sorted array for combo boxes. */
    public String[] getPlatformNamesArray() throws SQLException {
        List<GigPlatform> all = findAll();
        String[] names = new String[all.size()];
        for (int i = 0; i < all.size(); i++) {
            names[i] = all.get(i).getPlatformName();
        }
        return names;
    }

    private GigPlatform mapRow(ResultSet rs) throws SQLException {
        return new GigPlatform(rs.getInt("platform_id"), rs.getString("platform_name"));
    }
}
