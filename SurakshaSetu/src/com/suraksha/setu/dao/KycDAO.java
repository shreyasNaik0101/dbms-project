package com.suraksha.setu.dao;

import com.suraksha.setu.models.KycDocument;
import com.suraksha.setu.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** DAO for kyc_documents table. */
public class KycDAO extends GenericDAO<KycDocument> {

    @Override
    public void save(KycDocument doc) throws SQLException {
        String sql = "INSERT INTO kyc_documents (worker_id, document_type, status) VALUES (?,?,?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, doc.getWorkerId());
            ps.setString(2, doc.getDocumentType());
            ps.setString(3, doc.getStatus() != null ? doc.getStatus() : "Pending");
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) doc.setDocId(keys.getInt(1));
            }
        }
    }

    @Override
    public KycDocument findById(int id) throws SQLException {
        String sql = "SELECT * FROM kyc_documents WHERE doc_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        }
    }

    public List<KycDocument> findByWorkerId(int workerId) throws SQLException {
        String sql = "SELECT * FROM kyc_documents WHERE worker_id=?";
        List<KycDocument> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, workerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        }
        return list;
    }

    @Override public List<KycDocument> findAll() throws SQLException { return new ArrayList<>(); }
    @Override public void update(KycDocument doc) throws SQLException {
        String sql = "UPDATE kyc_documents SET status=? WHERE doc_id=?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, doc.getStatus());
            ps.setInt(2, doc.getDocId());
            ps.executeUpdate();
        }
    }
    @Override public void delete(int id) throws SQLException { }

    private KycDocument mapRow(ResultSet rs) throws SQLException {
        return new KycDocument(
            rs.getInt("doc_id"),
            rs.getInt("worker_id"),
            rs.getString("document_type"),
            rs.getString("status"),
            rs.getTimestamp("upload_date")
        );
    }
}
