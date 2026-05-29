package com.suraksha.setu.ui;

import com.suraksha.setu.exceptions.InsufficientEarningsException;
import com.suraksha.setu.exceptions.LoanApplicationException;
import com.suraksha.setu.models.Worker;
import com.suraksha.setu.services.LoanEligibilityService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Loan Eligibility Panel — check eligibility and apply for loans.
 */
public class LoanEligibilityPanel extends JPanel implements MainFrame.Refreshable {

    private final Worker                 worker;
    private final LoanEligibilityService loanService = new LoanEligibilityService();
    private JLabel                       statusLabel;
    private JComboBox<LoanEligibilityService.LoanProvider> providerBox;
    private JTextField                   amountField;
    private DefaultTableModel            tableModel;

    public LoanEligibilityPanel(Worker worker) {
        this.worker = worker;
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(16, 16));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        buildUI();
        loadProviders();
        checkEligibility();
    }

    private void buildUI() {
        JLabel header = new JLabel("🏦 Loan Eligibility");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        add(header, BorderLayout.NORTH);

        // ── Eligibility Result ────────────────────────────────────────────
        statusLabel = new JLabel("Checking eligibility...");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        statusLabel.setForeground(new Color(250, 204, 21));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        // ── Application Form ──────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(30, 41, 59));
        form.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(51, 65, 85)),
            BorderFactory.createEmptyBorder(16, 20, 16, 20)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);

        providerBox = new JComboBox<>();
        providerBox.setBackground(new Color(30, 41, 59));
        providerBox.setForeground(Color.WHITE);
        providerBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        providerBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setBackground(isSelected ? new Color(51, 65, 85) : new Color(30, 41, 59));
                l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
                if (value == null) {
                    l.setText("No providers available (Trust Score < 600)");
                    l.setForeground(new Color(248, 113, 113));
                } else {
                    l.setText(value.toString());
                    l.setForeground(Color.WHITE);
                }
                return l;
            }
        });

        amountField = new JTextField();
        amountField.setBackground(new Color(15, 23, 42));
        amountField.setForeground(Color.WHITE);
        amountField.setCaretColor(Color.WHITE);
        amountField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        amountField.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105)),
            BorderFactory.createEmptyBorder(4, 8, 4, 8)));

        JButton applyBtn = new JButton("Apply for Loan");
        applyBtn.setBackground(new Color(59, 130, 246));
        applyBtn.setForeground(Color.WHITE);
        applyBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        applyBtn.setFocusPainted(false);
        applyBtn.setBorderPainted(false);
        applyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        applyBtn.addActionListener(e -> applyLoan());

        gbc.gridx=0; gbc.gridy=0;
        form.add(fLabel("Loan Provider"), gbc);
        gbc.gridx=1; form.add(providerBox, gbc);
        gbc.gridx=0; gbc.gridy=1;
        form.add(fLabel("Amount (₹)"), gbc);
        gbc.gridx=1; form.add(amountField, gbc);
        gbc.gridx=0; gbc.gridy=2; gbc.gridwidth=2;
        form.add(applyBtn, gbc);

        // ── Applications Table ────────────────────────────────────────────
        String[] cols = {"Loan ID", "Provider", "Amount", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable appTable = new JTable(tableModel) {
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
        appTable.setBackground(new Color(30, 41, 59));
        appTable.setForeground(new Color(226, 232, 240));
        appTable.setGridColor(new Color(51, 65, 85));
        appTable.setRowHeight(28);
        appTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        appTable.getTableHeader().setBackground(new Color(15, 23, 42));
        appTable.getTableHeader().setForeground(new Color(99, 179, 237));
        appTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        ToolTipManager.sharedInstance().unregisterComponent(appTable);
        if (appTable.getTableHeader() != null) {
            ToolTipManager.sharedInstance().unregisterComponent(appTable.getTableHeader());
        }

        JScrollPane scroll = new JScrollPane(appTable);
        scroll.getViewport().setBackground(new Color(30, 41, 59));
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));

        JLabel appHeader = new JLabel("My Loan Applications");
        appHeader.setFont(new Font("Segoe UI", Font.BOLD, 14));
        appHeader.setForeground(new Color(99, 179, 237));
        appHeader.setBorder(BorderFactory.createEmptyBorder(12, 0, 8, 0));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(statusLabel, BorderLayout.NORTH);
        center.add(form, BorderLayout.CENTER);

        JPanel south = new JPanel(new BorderLayout());
        south.setOpaque(false);
        south.add(appHeader, BorderLayout.NORTH);
        south.add(scroll, BorderLayout.CENTER);

        add(center, BorderLayout.NORTH);
        add(south, BorderLayout.CENTER);

        loadApplications();
    }

    private void checkEligibility() {
        try {
            LoanEligibilityService.EligibilityResult result = loanService.checkEligibility(worker);
            if (result.eligible) {
                statusLabel.setText("✅ Eligible! Max loan: ₹" + String.format("%.0f", result.maxLoanAmount));
                statusLabel.setForeground(new Color(74, 222, 128));
                amountField.setText(String.format("%.0f", result.maxLoanAmount));
            } else {
                statusLabel.setText("❌ Not eligible: " + result.reason);
                statusLabel.setForeground(new Color(248, 113, 113));
            }
        } catch (InsufficientEarningsException e) {
            statusLabel.setText("❌ Income too low: " + e.getMessage());
            statusLabel.setForeground(new Color(248, 113, 113));
        } catch (Exception e) {
            statusLabel.setText("⚠ Error: " + e.getMessage());
            statusLabel.setForeground(new Color(250, 204, 21));
        }
    }

    private void loadProviders() {
        try {
            providerBox.removeAllItems();
            List<LoanEligibilityService.LoanProvider> providers =
                loanService.getEligibleProviders(worker.getCurrentTrustScore());
            if (providers.isEmpty()) {
                providerBox.addItem(null);
            } else {
                for (LoanEligibilityService.LoanProvider p : providers) {
                    providerBox.addItem(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Could not load providers: " + e.getMessage());
        }
    }

    private void applyLoan() {
        LoanEligibilityService.LoanProvider provider =
            (LoanEligibilityService.LoanProvider) providerBox.getSelectedItem();
        if (provider == null) {
            JOptionPane.showMessageDialog(this, "Please select a loan provider.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Enter a valid loan amount.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            loanService.applyForLoan(worker.getWorkerId(), provider.providerId, amount);
            JOptionPane.showMessageDialog(this, "Loan application submitted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            loadApplications();
        } catch (LoanApplicationException e) {
            JOptionPane.showMessageDialog(this, e.getReason(), "Application Failed", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadApplications() {
        tableModel.setRowCount(0);
        try {
            com.suraksha.setu.dao.LoanApplicationDAO dao = new com.suraksha.setu.dao.LoanApplicationDAO();
            List<com.suraksha.setu.models.LoanApplication> apps = dao.findByWorkerId(worker.getWorkerId());
            for (com.suraksha.setu.models.LoanApplication la : apps) {
                tableModel.addRow(new Object[]{
                    la.getLoanId(), la.getProviderName(),
                    String.format("₹%.2f", la.getLoanAmount()),
                    la.getStatus(), la.getAppliedDate()
                });
            }
        } catch (Exception e) {
            System.err.println("Could not load applications: " + e.getMessage());
        }
    }

    @Override public void refresh() { checkEligibility(); loadProviders(); loadApplications(); }

    private JLabel fLabel(String text) {
        JLabel l = new JLabel(text + ":");
        l.setForeground(new Color(148, 163, 184));
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }
}
