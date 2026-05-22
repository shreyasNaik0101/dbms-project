package com.suraksha.setu.ui;

import com.suraksha.setu.models.Worker;
import com.suraksha.setu.services.ReportService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Financial Hub — monthly earnings summary, net income, and performance stats.
 * Uses a premium card-based design.
 */
public class FinancialHubPanel extends JPanel implements MainFrame.Refreshable {

    private final Worker        worker;
    private final ReportService reportService = new ReportService();
    private DefaultTableModel   tableModel;
    private JLabel              netIncomeLabel;
    private JPanel              statsPanel;

    public FinancialHubPanel(Worker worker) {
        this.worker = worker;
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JLabel header = new JLabel("💰 Financial Hub");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        add(header, BorderLayout.NORTH);

        // --- Stat Cards ---
        statsPanel = new JPanel(new GridLayout(1, 4, 12, 0));
        statsPanel.setOpaque(false);
        // Placeholders, will be updated in loadData
        statsPanel.add(createStatCard("Total Earnings", "₹0.00", new Color(74, 222, 128)));
        statsPanel.add(createStatCard("Total Hours", "0.0", new Color(99, 179, 237)));
        statsPanel.add(createStatCard("Net Income", "₹0.00", new Color(250, 204, 21)));
        statsPanel.add(createStatCard("Active Months", "0", new Color(167, 139, 250)));

        // --- Monthly Summary Table ---
        String[] cols = {"Month", "Gross Earnings", "Expenses", "Net Savings"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(tableModel) {
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
        styleTable(table);
        ToolTipManager.sharedInstance().unregisterComponent(table);
        if (table.getTableHeader() != null) {
            ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 41, 59));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        JLabel tableHeader = new JLabel("Monthly Performance Ledger");
        tableHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        tableHeader.setForeground(new Color(99, 179, 237));
        tableHeader.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(statsPanel, BorderLayout.NORTH);
        centerPanel.add(tableHeader, BorderLayout.CENTER);
        centerPanel.add(scroll, BorderLayout.SOUTH);

        add(centerPanel, BorderLayout.CENTER);

        netIncomeLabel = new JLabel(" ");
        netIncomeLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        netIncomeLabel.setForeground(new Color(148, 163, 184));
        add(netIncomeLabel, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, String value, Color color) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(30, 41, 59));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        
        JLabel t = new JLabel(title);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        t.setForeground(new Color(148, 163, 184));
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel v = new JLabel(value);
        v.setFont(new Font("Segoe UI", Font.BOLD, 20));
        v.setForeground(color);
        v.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        card.add(t);
        card.add(Box.createVerticalStrut(8));
        card.add(v);
        return card;
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

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<Object[]> rows = reportService.getMonthlySummaryRows(worker.getWorkerId());
            for (Object[] row : rows) tableModel.addRow(row);
            
            double net = reportService.getNetIncome(worker.getWorkerId());
            List<com.suraksha.setu.models.WorkHistory> logs = reportService.getWorkLogsForWorker(worker.getWorkerId());
            double totalEarnings = reportService.getTotalEarnings(logs);
            double totalHours    = reportService.getTotalHours(logs);

            // Update cards
            updateCard(0, String.format("₹%.2f", totalEarnings));
            updateCard(1, String.format("%.1f", totalHours));
            updateCard(2, String.format("₹%.2f", net));
            updateCard(3, String.valueOf(rows.size()));

            netIncomeLabel.setText("  * Data is calculated based on verified logs and operational expenses.");
        } catch (Exception e) {
            netIncomeLabel.setText("  Error: " + e.getMessage());
        }
    }

    private void updateCard(int index, String value) {
        JPanel card = (JPanel) statsPanel.getComponent(index);
        JLabel v = (JLabel) card.getComponent(2);
        v.setText(value);
    }

    @Override public void refresh() { loadData(); }
}
