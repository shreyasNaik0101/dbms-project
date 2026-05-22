package com.suraksha.setu.ui;

import com.suraksha.setu.dao.WorkerDAO;
import com.suraksha.setu.models.Worker;
import com.suraksha.setu.services.TrustScoreService;
import com.suraksha.setu.util.WorkerFilter;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Admin Panel — manage all workers, view analytics, recalculate scores.
 * Demonstrates: WorkerFilter lambda, Collections.sort(), JTable.
 */
public class AdminPanel extends JPanel implements MainFrame.Refreshable {

    private final WorkerDAO          workerDAO    = new WorkerDAO();
    private final TrustScoreService  trustService = new TrustScoreService();
    private DefaultTableModel        tableModel;
    private JTextField               searchField;
    private JLabel                   statusLabel;
    private List<Worker>             allWorkers;

    public AdminPanel() {
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        buildUI();
        loadWorkers();
    }

    private void buildUI() {
        JLabel header = new JLabel("🔑 Admin Panel");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        add(header, BorderLayout.NORTH);

        // ── Toolbar ───────────────────────────────────────────────────────
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        searchField = new JTextField(20);
        searchField.setBackground(new Color(30, 41, 59));
        searchField.setForeground(Color.WHITE);
        searchField.setCaretColor(Color.WHITE);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));


        JButton searchBtn    = toolBtn("🔍 Search",      new Color(59, 130, 246));
        JButton refreshBtn   = toolBtn("🔄 Refresh",     new Color(71, 85, 105));
        JButton recalcAllBtn = toolBtn("⚡ Recalc All", new Color(234, 179, 8));
        JButton sortBtn      = toolBtn("↕ Sort Score",  new Color(71, 85, 105));
        JButton registerBtn  = toolBtn("➕ Register Worker", new Color(34, 197, 94));

        toolbar.add(new JLabel("Search: ") {{setForeground(new Color(148,163,184));setFont(new Font("Segoe UI",Font.PLAIN,12));}});
        toolbar.add(searchField);
        toolbar.add(searchBtn); toolbar.add(refreshBtn);
        toolbar.add(recalcAllBtn); toolbar.add(sortBtn);
        toolbar.add(registerBtn);

        // ── Workers Table ─────────────────────────────────────────────────
        String[] cols = {"ID", "Digital ID", "Name", "Phone", "Trust Score", "Joined"};
        tableModel = new DefaultTableModel(cols, 0) {
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
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(new Color(226, 232, 240));
        table.setGridColor(new Color(51, 65, 85));
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(new Color(99, 179, 237));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setSelectionBackground(new Color(51, 65, 85));
        ToolTipManager.sharedInstance().unregisterComponent(table);
        if (table.getTableHeader() != null) {
            ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
        }

        JScrollPane scroll = new JScrollPane(table);
        scroll.getViewport().setBackground(new Color(30, 41, 59));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        statusLabel = new JLabel("  Loading...");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(148, 163, 184));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        // ── Event Listeners ───────────────────────────────────────────────
        searchBtn.addActionListener(e -> filterWorkers(searchField.getText().trim()));
        refreshBtn.addActionListener(e -> loadWorkers());
        sortBtn.addActionListener(e -> sortByscore());
        recalcAllBtn.addActionListener(e -> recalcAll());
        registerBtn.addActionListener(e -> {
            RegisterWorkerDialog dialog = new RegisterWorkerDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isRegistrationSuccessful()) {
                loadWorkers();
            }
        });

        searchField.addActionListener(e -> filterWorkers(searchField.getText().trim()));
    }

    private void loadWorkers() {
        tableModel.setRowCount(0);
        try {
            allWorkers = workerDAO.findAll();
            populateTable(allWorkers);
            statusLabel.setText("  Total workers: " + allWorkers.size());
        } catch (Exception e) {
            statusLabel.setText("  Error: " + e.getMessage());
        }
    }

    private void filterWorkers(String keyword) {
        if (allWorkers == null) return;
        if (keyword.isEmpty()) { populateTable(allWorkers); return; }

        // Lambda with WorkerFilter functional interface (Unit II)
        WorkerFilter nameFilter = WorkerFilter.byNameContains(keyword);
        List<Worker> filtered   = allWorkers.stream()
                .filter(nameFilter::test)
                .collect(Collectors.toList());
        populateTable(filtered);
        statusLabel.setText("  Found: " + filtered.size() + " workers matching '" + keyword + "'");
    }

    private void sortByscore() {
        if (allWorkers == null) return;
        // Collections.sort uses Worker.compareTo() — Comparable interface (Unit I/III)
        Collections.sort(allWorkers);
        populateTable(allWorkers);
        statusLabel.setText("  Sorted by trust score (highest first).");
    }

    private void recalcAll() {
        if (allWorkers == null) return;
        int count = 0;
        statusLabel.setText("  Recalculating...");
        for (Worker w : allWorkers) {
            try {
                trustService.calculateAndUpdate(w.getWorkerId());
                count++;
            } catch (Exception e) {
                System.err.println("Skipped worker " + w.getWorkerId() + ": " + e.getMessage());
            }
        }
        JOptionPane.showMessageDialog(this, "Recalculated scores for " + count + " workers.", "Done", JOptionPane.INFORMATION_MESSAGE);
        loadWorkers();
    }

    private void populateTable(List<Worker> workers) {
        tableModel.setRowCount(0);
        for (Worker w : workers) {
            tableModel.addRow(new Object[]{
                w.getWorkerId(), w.getDigitalWorkId(), w.getFullName(),
                w.getPhone(), String.format("%.1f", w.getCurrentTrustScore()),
                w.getJoiningDate() != null ? w.getJoiningDate().toString().substring(0, 10) : "N/A"
            });
        }
    }

    @Override public void refresh() { loadWorkers(); }

    private JButton toolBtn(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
