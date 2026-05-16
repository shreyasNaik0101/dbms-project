package com.suraksha.setu.dao;

import com.suraksha.setu.util.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for skills_master and worker_skills (Many-to-Many). */
public class SkillDAO {

    public List<String> findSkillsByWorker(int workerId) throws SQLException {
        String sql = "SELECT sm.skill_name FROM skills_master sm " +
                     "JOIN worker_skills ws ON sm.skill_id = ws.skill_id " +
                     "WHERE ws.worker_id = ?";
        List<String> skills = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) skills.add(rs.getString("skill_name"));
            }
        }
        return skills;
    }

    public void addSkillToWorker(int workerId, int skillId, double years) throws SQLException {
        String sql = "INSERT INTO worker_skills (worker_id, skill_id, years_experience) VALUES (?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            ps.setInt(2, skillId);
            ps.setDouble(3, years);
            ps.executeUpdate();
        }
    }
}
