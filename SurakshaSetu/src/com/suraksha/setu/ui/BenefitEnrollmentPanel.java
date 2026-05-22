package com.suraksha.setu.ui;

import com.suraksha.setu.dao.BenefitSchemeDAO;
import com.suraksha.setu.models.BenefitScheme;
import com.suraksha.setu.models.Worker;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Panel for Benefit Scheme Enrollment.
 * Allows workers to view and enroll in social security schemes.
 */
public class BenefitEnrollmentPanel extends JPanel implements MainFrame.Refreshable {

    private final Worker worker;
    private final BenefitSchemeDAO benefitDAO = new BenefitSchemeDAO();
    private DefaultTableModel availableModel;
    private DefaultTableModel enrolledModel;
    private JLabel statusLabel;

    public BenefitEnrollmentPanel(Worker worker) {
        this.worker = worker;
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        buildUI();
        refresh();
    }

    private void buildUI() {
        JLabel header = new JLabel("🛡️ Benefit Schemes");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        add(header, BorderLayout.NORTH);

        JPanel mainContent = new JPanel(new GridLayout(2, 1, 0, 20));
        mainContent.setOpaque(false);

        // --- Available Schemes ---
        JPanel availablePanel = createSectionPanel("Available Schemes");
        String[] availCols = {"ID", "Scheme Name", "Coverage (₹)", "Premium (₹/mo)", "Action"};
        availableModel = new DefaultTableModel(availCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only action column is "editable" (for buttons)
            }
        };
        JTable availTable = new JTable(availableModel) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                return null;
            }
            @Override
            protected javax.swing.table.JTableHeader createDefaultTableHeader() {
                return new javax.swing.table.JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(java.awt.event.MouseEvent e) {
                        return null;
                    }
                };
            }
        };
        styleTable(availTable);
        ToolTipManager.sharedInstance().unregisterComponent(availTable);
        if (availTable.getTableHeader() != null) {
            ToolTipManager.sharedInstance().unregisterComponent(availTable.getTableHeader());
        }
        availablePanel.add(new JScrollPane(availTable), BorderLayout.CENTER);

        // --- Enrolled Schemes ---
        JPanel enrolledPanel = createSectionPanel("My Enrolled Schemes");
        String[] enrollCols = {"ID", "Scheme Name", "Coverage (₹)", "Premium (₹/mo)"};
        enrolledModel = new DefaultTableModel(enrollCols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable enrollTable = new JTable(enrolledModel) {
            @Override
            public String getToolTipText(java.awt.event.MouseEvent e) {
                return null;
            }
            @Override
            protected javax.swing.table.JTableHeader createDefaultTableHeader() {
                return new javax.swing.table.JTableHeader(columnModel) {
                    @Override
                    public String getToolTipText(java.awt.event.MouseEvent e) {
                        return null;
                    }
                };
            }
        };
        styleTable(enrollTable);
        ToolTipManager.sharedInstance().unregisterComponent(enrollTable);
        if (enrollTable.getTableHeader() != null) {
            ToolTipManager.sharedInstance().unregisterComponent(enrollTable.getTableHeader());
        }
        enrolledPanel.add(new JScrollPane(enrollTable), BorderLayout.CENTER);

        mainContent.add(availablePanel);
        mainContent.add(enrolledPanel);
        add(mainContent, BorderLayout.CENTER);

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(148, 163, 184));
        add(statusLabel, BorderLayout.SOUTH);

        // Add action button functionality for Available Schemes
        availTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = availTable.rowAtPoint(evt.getPoint());
                int col = availTable.columnAtPoint(evt.getPoint());
                if (col == 4 && row >= 0) {
                    enrollInScheme((int) availTable.getValueAt(row, 0));
                }
            }
        });
    }

    private JPanel createSectionPanel(String title) {
        JPanel p = new JPanel(new BorderLayout(0, 10));
        p.setOpaque(false);
        JLabel l = new JLabel(title);
        l.setFont(new Font("Segoe UI", Font.BOLD, 15));
        l.setForeground(new Color(99, 179, 237));
        p.add(l, BorderLayout.NORTH);
        return p;
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(30, 41, 59));
        t.setForeground(new Color(226, 232, 240));
        t.setGridColor(new Color(51, 65, 85));
        t.setRowHeight(32);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setBackground(new Color(15, 23, 42));
        t.getTableHeader().setForeground(new Color(99, 179, 237));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setSelectionBackground(new Color(51, 65, 85));
    }

    private void enrollInScheme(int schemeId) {
        try {
            // Check if already enrolled
            List<BenefitScheme> enrolled = benefitDAO.findEnrolledSchemes(worker.getWorkerId());
            boolean alreadyEnrolled = enrolled.stream().anyMatch(e -> e.getSchemeId() == schemeId);
            if (alreadyEnrolled) {
                statusLabel.setText("ℹ Already enrolled in this scheme.");
                statusLabel.setForeground(new Color(250, 204, 21));
                return;
            }
            benefitDAO.enrollWorker(worker.getWorkerId(), schemeId, new Date(System.currentTimeMillis()));
            statusLabel.setText("✓ Enrolled successfully!");
            statusLabel.setForeground(new Color(74, 222, 128));
            refresh();
        } catch (SQLException e) {
            statusLabel.setText("✗ Error enrolling: " + e.getMessage());
            statusLabel.setForeground(new Color(248, 113, 113));
        }
    }

    @Override
    public void refresh() {
        availableModel.setRowCount(0);
        enrolledModel.setRowCount(0);
        try {
            List<BenefitScheme> all = benefitDAO.findAll();
            List<BenefitScheme> enrolled = benefitDAO.findEnrolledSchemes(worker.getWorkerId());

            for (BenefitScheme bs : all) {
                boolean isEnrolled = enrolled.stream().anyMatch(e -> e.getSchemeId() == bs.getSchemeId());
                availableModel.addRow(new Object[]{
                    bs.getSchemeId(), bs.getSchemeName(),
                    String.format("₹%.0f", bs.getCoverageAmount()),
                    String.format("₹%.0f", bs.getPremiumCost()),
                    isEnrolled ? "Already Enrolled" : "Enroll Now"
                });
            }

            for (BenefitScheme bs : enrolled) {
                enrolledModel.addRow(new Object[]{
                    bs.getSchemeId(), bs.getSchemeName(),
                    String.format("₹%.0f", bs.getCoverageAmount()),
                    String.format("₹%.0f", bs.getPremiumCost())
                });
            }
        } catch (SQLException e) {
            statusLabel.setText("✗ Error loading schemes: " + e.getMessage());
        }
    }
}
