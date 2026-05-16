package com.suraksha.setu.ui;

import com.suraksha.setu.dao.TrustScoreDAO;
import com.suraksha.setu.models.TrustScoreAudit;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Modal dialog showing trust score audit trail for a worker.
 */
public class AuditTrailDialog extends JDialog {

    public AuditTrailDialog(Window owner, int workerId) {
        super(owner, "Audit Trail — Worker #" + workerId, ModalityType.APPLICATION_MODAL);
        setSize(640, 420);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(12, 12));

        JLabel header = new JLabel("  📋 Trust Score Audit Trail");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(new Color(99, 179, 237));
        header.setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        String[] cols = {"Audit #", "Old Score", "New Score", "Change", "Reason", "Calculated At"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable table = new JTable(model);
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(new Color(226, 232, 240));
        table.setGridColor(new Color(51, 65, 85));
        table.setRowHeight(28);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(new Color(99, 179, 237));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 41, 59));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        add(scroll, BorderLayout.CENTER);

        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(71, 85, 105));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(e -> dispose());
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);

        // Load data
        try {
            TrustScoreDAO dao = new TrustScoreDAO();
            List<TrustScoreAudit> audits = dao.findByWorkerId(workerId);
            for (TrustScoreAudit a : audits) {
                double delta = a.getDelta();
                String deltaStr = (delta >= 0 ? "▲ +" : "▼ ") + String.format("%.1f", delta);
                model.addRow(new Object[]{
                    a.getAuditId(),
                    String.format("%.1f", a.getOldScore()),
                    String.format("%.1f", a.getNewScore()),
                    deltaStr, a.getReason(), a.getCalculatedAt()
                });
            }
        } catch (Exception e) {
            model.addRow(new Object[]{"—", "—", "—", "—", "Error: " + e.getMessage(), "—"});
        }
    }
}
