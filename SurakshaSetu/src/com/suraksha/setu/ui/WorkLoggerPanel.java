package com.suraksha.setu.ui;

import com.suraksha.setu.dao.GigPlatformDAO;
import com.suraksha.setu.dao.WorkHistoryDAO;
import com.suraksha.setu.models.GigPlatform;
import com.suraksha.setu.models.WorkHistory;
import com.suraksha.setu.models.Worker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Date;
import java.util.List;

/**
 * Panel for logging daily work entries.
 * Demonstrates: JComboBox with ItemListener, JSpinner, JButton, JTable.
 */
public class WorkLoggerPanel extends JPanel implements MainFrame.Refreshable {

    private final Worker worker;
    private final WorkHistoryDAO  workHistoryDAO  = new WorkHistoryDAO();
    private final GigPlatformDAO  platformDAO     = new GigPlatformDAO();

    private JComboBox<GigPlatform> platformBox;
    private JSpinner               hoursSpinner;
    private JTextField             earningsField;
    private JLabel                 statusLabel;
    private JTable                 historyTable;
    private javax.swing.table.DefaultTableModel tableModel;

    public WorkLoggerPanel(Worker worker) {
        this.worker = worker;
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        buildUI();
        loadHistory();
    }

    private void buildUI() {
        JLabel header = new JLabel("📝 Work Logger");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        add(header, BorderLayout.NORTH);

        // ── Entry Form ────────────────────────────────────────────────────
        JPanel formCard = new JPanel(new GridBagLayout());
        formCard.setBackground(new Color(30, 41, 59));
        formCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        // Platform dropdown
        platformBox = new JComboBox<>();
        platformBox.setBackground(new Color(30, 41, 59));
        platformBox.setForeground(Color.WHITE);
        platformBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        platformBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setBackground(isSelected ? new Color(51, 65, 85) : new Color(30, 41, 59));
                l.setForeground(Color.WHITE);
                l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                return l;
            }
        });
        loadPlatforms();

        // ItemListener on platform dropdown
        platformBox.addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                GigPlatform selected = (GigPlatform) platformBox.getSelectedItem();
                statusLabel.setText(" Selected: " + (selected != null ? selected.getPlatformName() : ""));
            }
        });

        // Hours spinner — use NumberEditor, style editor panel + text field directly
        hoursSpinner = new JSpinner(new SpinnerNumberModel(8.0, 0.5, 16.0, 0.5));
        hoursSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        hoursSpinner.setBackground(new Color(30, 41, 59));
        hoursSpinner.setForeground(Color.WHITE);
        hoursSpinner.setBorder(BorderFactory.createLineBorder(new Color(71, 85, 105), 1));

        // Use NumberEditor so we get a JFormattedTextField we fully control
        JSpinner.NumberEditor numEditor = new JSpinner.NumberEditor(hoursSpinner, "0.0") {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(new Color(30, 41, 59));
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        numEditor.setBackground(new Color(30, 41, 59));
        numEditor.setForeground(Color.WHITE);
        numEditor.setOpaque(true);

        JFormattedTextField spinnerTF = numEditor.getTextField();
        spinnerTF.setBackground(new Color(30, 41, 59));
        spinnerTF.setForeground(Color.WHITE);
        spinnerTF.setCaretColor(Color.WHITE);
        spinnerTF.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        spinnerTF.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        spinnerTF.setOpaque(true);
        spinnerTF.setColumns(5);
        // Force repaint override
        spinnerTF.setUI(new javax.swing.plaf.basic.BasicFormattedTextFieldUI() {
            @Override
            protected void paintBackground(Graphics g) {
                g.setColor(new Color(30, 41, 59));
                g.fillRect(0, 0, spinnerTF.getWidth(), spinnerTF.getHeight());
            }
        });

        hoursSpinner.setEditor(numEditor);

        // Earnings field with custom paintComponent subclass to bypass Nimbus background painter
        earningsField = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };
        earningsField.setOpaque(false);
        earningsField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        earningsField.setBackground(new Color(15, 23, 42));
        earningsField.setForeground(Color.WHITE);
        earningsField.setCaretColor(Color.WHITE);
        earningsField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        // DocumentListener — real-time validation
        earningsField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateEarnings(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e)  { validateEarnings(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateEarnings(); }
        });

        JButton logBtn = new JButton("➕ Log Today's Work");
        logBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        logBtn.setBackground(new Color(59, 130, 246));
        logBtn.setForeground(Color.WHITE);
        logBtn.setFocusPainted(false);
        logBtn.setBorderPainted(false);
        logBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logBtn.addActionListener(e -> logWork());

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(148, 163, 184));

        gbc.gridx=0; gbc.gridy=0; formCard.add(fieldLabel("Platform"), gbc);
        gbc.gridx=1; formCard.add(platformBox, gbc);
        gbc.gridx=0; gbc.gridy=1; formCard.add(fieldLabel("Hours Worked"), gbc);
        gbc.gridx=1; formCard.add(hoursSpinner, gbc);
        gbc.gridx=0; gbc.gridy=2; formCard.add(fieldLabel("Earnings (₹)"), gbc);
        gbc.gridx=1; formCard.add(earningsField, gbc);
        gbc.gridx=0; gbc.gridy=3; gbc.gridwidth=2; formCard.add(logBtn, gbc);
        gbc.gridy=4; formCard.add(statusLabel, gbc);

        // ── History Table ─────────────────────────────────────────────────
        String[] cols = {"Date", "Platform", "Hours", "Earnings (₹)"};
        tableModel = new javax.swing.table.DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(tableModel) {
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
        styleTable(historyTable);
        ToolTipManager.sharedInstance().unregisterComponent(historyTable);
        if (historyTable.getTableHeader() != null) {
            ToolTipManager.sharedInstance().unregisterComponent(historyTable.getTableHeader());
        }
        JScrollPane scroll = new JScrollPane(historyTable);
        scroll.setBackground(new Color(15, 23, 42));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        JLabel histHeader = new JLabel("Recent Work History");
        histHeader.setFont(new Font("Segoe UI", Font.BOLD, 15));
        histHeader.setForeground(new Color(99, 179, 237));
        histHeader.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(formCard, BorderLayout.NORTH);
        topPanel.add(histHeader, BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(topPanel, BorderLayout.NORTH);
        center.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.CENTER);
    }

    private void logWork() {
        GigPlatform platform = (GigPlatform) platformBox.getSelectedItem();
        if (platform == null) { statusLabel.setText("⚠ Select a platform."); return; }

        double hours, earnings;
        try {
            hours    = ((Number) hoursSpinner.getValue()).doubleValue();
            earnings = Double.parseDouble(earningsField.getText().trim());
            if (earnings <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            statusLabel.setText("⚠ Invalid earnings amount.");
            return;
        }

        WorkHistory wh = new WorkHistory();
        wh.setWorkerId(worker.getWorkerId());
        wh.setPlatformId(platform.getPlatformId());
        wh.setWorkDate(new Date(System.currentTimeMillis()));
        wh.setHoursLogged(hours);
        wh.setEarnings(earnings);
        wh.setCompletionDate(new Date(System.currentTimeMillis()));

        try {
            workHistoryDAO.save(wh);
            statusLabel.setText("✓ Work log saved successfully!");
            statusLabel.setForeground(new Color(74, 222, 128));
            earningsField.setText("");
            loadHistory();
        } catch (Exception ex) {
            statusLabel.setText("✗ Error: " + ex.getMessage());
            statusLabel.setForeground(new Color(248, 113, 113));
        }
    }

    private void loadPlatforms() {
        try {
            platformBox.removeAllItems();
            for (GigPlatform gp : platformDAO.findAll()) {
                platformBox.addItem(gp);
            }
        } catch (Exception e) {
            System.err.println("Could not load platforms: " + e.getMessage());
        }
    }

    private void loadHistory() {
        tableModel.setRowCount(0);
        try {
            List<WorkHistory> logs = workHistoryDAO.findByWorkerId(worker.getWorkerId());
            for (WorkHistory wh : logs) {
                tableModel.addRow(new Object[]{
                    wh.getWorkDate(), wh.getPlatformName(),
                    String.format("%.1f", wh.getHoursLogged()),
                    String.format("₹%.2f", wh.getEarnings())
                });
            }
        } catch (Exception e) {
            System.err.println("Could not load history: " + e.getMessage());
        }
    }

    private void validateEarnings() {
        String text = earningsField.getText().trim();
        try {
            double val = Double.parseDouble(text);
            earningsField.setForeground(val > 0 ? new Color(74, 222, 128) : new Color(248, 113, 113));
        } catch (NumberFormatException e) {
            earningsField.setForeground(new Color(248, 113, 113));
        }
    }

    @Override public void refresh() { loadHistory(); loadPlatforms(); }

    private JLabel fieldLabel(String text) {
        JLabel l = new JLabel(text + ":"); l.setForeground(new Color(148, 163, 184));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13)); return l;
    }

    private void styleTable(JTable t) {
        t.setBackground(new Color(30, 41, 59));
        t.setForeground(new Color(226, 232, 240));
        t.setGridColor(new Color(51, 65, 85));
        t.setRowHeight(28);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        t.getTableHeader().setBackground(new Color(15, 23, 42));
        t.getTableHeader().setForeground(new Color(99, 179, 237));
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.setSelectionBackground(new Color(51, 65, 85));
        t.setSelectionForeground(Color.WHITE);
    }
}
