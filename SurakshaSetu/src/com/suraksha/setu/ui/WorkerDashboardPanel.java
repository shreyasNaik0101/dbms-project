package com.suraksha.setu.ui;

import com.suraksha.setu.dao.TrustScoreDAO;
import com.suraksha.setu.dao.WorkHistoryDAO;
import com.suraksha.setu.models.Worker;
import com.suraksha.setu.services.TrustScoreService;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * Worker Dashboard — profile card, trust score gauge, recent work summary.
 * Demonstrates: JPanel, JLabel, custom painting, Refreshable.
 */
public class WorkerDashboardPanel extends JPanel implements MainFrame.Refreshable {

    private Worker worker;
    private JLabel nameLabel, idLabel, phoneLabel, scoreLabel, tierLabel, kpiLabel;
    private JProgressBar scoreBar;
    private final TrustScoreService trustScoreService = new TrustScoreService();
    private final com.suraksha.setu.dao.WorkerDAO workerDAO = new com.suraksha.setu.dao.WorkerDAO();

    public WorkerDashboardPanel(Worker worker) {
        this.worker = worker;
        setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));
        buildUI();
    }

    private void buildUI() {
        // ── Page header ────────────────────────────────────────────────────
        JLabel header = new JLabel(worker != null ? "Worker Dashboard" : "Admin Overview");
        header.setFont(new Font("Segoe UI", Font.BOLD, 22));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 12, 0));
        add(header, BorderLayout.NORTH);

        if (worker == null) {
            add(new JLabel("Select a worker from the Admin Panel.", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }

        // ── Main content ───────────────────────────────────────────────────
        JPanel center = new JPanel(new GridLayout(1, 2, 20, 0));
        center.setOpaque(false);
        center.add(buildProfileCard());
        center.add(buildScoreCard());
        add(center, BorderLayout.CENTER);

        // ── Bottom KPI bar ─────────────────────────────────────────────────
        kpiLabel = new JLabel(" ");
        kpiLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        kpiLabel.setForeground(new Color(148, 163, 184));
        add(kpiLabel, BorderLayout.SOUTH);

        loadKPIs();
    }

    private JPanel buildProfileCard() {
        JPanel card = createCard("👤 Profile");
        nameLabel  = infoLabel("Name",       worker.getFullName());
        idLabel    = infoLabel("Digital ID", worker.getDigitalWorkId());
        phoneLabel = infoLabel("Phone",      worker.getPhone());
        JLabel joinLabel = infoLabel("Member Since",
                worker.getJoiningDate() != null ? worker.getJoiningDate().toString().substring(0, 10) : "N/A");

        // Certificate button
        JButton certBtn = createActionButton("📄 View Certificate");
        certBtn.addActionListener(e -> {
            CertificateDialog dialog = new CertificateDialog(
                SwingUtilities.getWindowAncestor(this), worker);
            dialog.setVisible(true);
        });

        JButton auditBtn = createActionButton("📋 Audit Trail");
        auditBtn.addActionListener(e -> {
            AuditTrailDialog dialog = new AuditTrailDialog(
                SwingUtilities.getWindowAncestor(this), worker.getWorkerId());
            dialog.setVisible(true);
        });

        card.add(nameLabel); card.add(idLabel); card.add(phoneLabel);
        card.add(joinLabel); card.add(Box.createVerticalStrut(12));
        card.add(certBtn); card.add(auditBtn);
        return card;
    }

    private JPanel buildScoreCard() {
        JPanel card = createCard("🎯 Trust Score");

        scoreLabel = new JLabel(String.format("%.0f / 1000", worker.getCurrentTrustScore()));
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        scoreLabel.setForeground(scoreColor(worker.getCurrentTrustScore()));
        scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        tierLabel = new JLabel(getTier(worker.getCurrentTrustScore()));
        tierLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tierLabel.setForeground(scoreColor(worker.getCurrentTrustScore()));
        tierLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        scoreBar = new JProgressBar(0, 1000);
        scoreBar.setValue((int) worker.getCurrentTrustScore());
        scoreBar.setForeground(scoreColor(worker.getCurrentTrustScore()));
        scoreBar.setBackground(new Color(30, 41, 59));
        scoreBar.setPreferredSize(new Dimension(0, 16));
        scoreBar.setBorderPainted(false);

        JButton recalcBtn = createActionButton("🔄 Recalculate Score");
        recalcBtn.addActionListener(e -> recalculate());

        JLabel formula = new JLabel("<html><center><font color='#64748b' size='2'>" +
                "Formula: 0.4×Consistency + 0.35×Rating + 0.25×IncomeLevel<br>" +
                "Scaled to 0–1000</font></center></html>", SwingConstants.CENTER);

        card.add(scoreLabel); card.add(Box.createVerticalStrut(4));
        card.add(tierLabel); card.add(Box.createVerticalStrut(16));
        card.add(scoreBar); card.add(Box.createVerticalStrut(16));
        card.add(recalcBtn); card.add(Box.createVerticalStrut(8));
        card.add(formula);
        return card;
    }

    private void recalculate() {
        try {
            double newScore = trustScoreService.calculateAndUpdate(worker.getWorkerId());
            worker.setCurrentTrustScore(newScore);
            // Update all score-related UI elements
            scoreLabel.setText(String.format("%.0f / 1000", newScore));
            scoreLabel.setForeground(scoreColor(newScore));
            tierLabel.setText(getTier(newScore));
            tierLabel.setForeground(scoreColor(newScore));
            scoreBar.setValue((int) newScore);
            scoreBar.setForeground(scoreColor(newScore));
            // Refresh loan panel too if visible
            JOptionPane.showMessageDialog(this,
                String.format("Trust score updated to: %.1f / 1000", newScore),
                "Score Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadKPIs() {
        try {
            WorkHistoryDAO dao = new WorkHistoryDAO();
            List<com.suraksha.setu.models.WorkHistory> logs = dao.findByWorkerId(worker.getWorkerId());

            // Lambda + stream (Unit II)
            double totalEarnings = logs.stream().mapToDouble(wh -> wh.getEarnings()).sum();
            double totalHours    = logs.stream().mapToDouble(wh -> wh.getHoursLogged()).sum();
            long   totalGigs     = logs.size();

            kpiLabel.setText(String.format("  📦 Total Gigs: %d   |   ⏱ Total Hours: %.1f   |   💵 Total Earnings: ₹%.2f",
                    totalGigs, totalHours, totalEarnings));
        } catch (SQLException e) {
            kpiLabel.setText("  Could not load KPI data.");
        }
    }

    @Override
    public void refresh() {
        if (worker == null) return;
        // Re-fetch worker from DB to get the latest trust score
        try {
            Worker fresh = workerDAO.findById(worker.getWorkerId());
            if (fresh != null) {
                worker.setCurrentTrustScore(fresh.getCurrentTrustScore());
                double score = fresh.getCurrentTrustScore();
                scoreLabel.setText(String.format("%.0f / 1000", score));
                scoreLabel.setForeground(scoreColor(score));
                tierLabel.setText(getTier(score));
                tierLabel.setForeground(scoreColor(score));
                scoreBar.setValue((int) score);
                scoreBar.setForeground(scoreColor(score));
            }
        } catch (java.sql.SQLException ignored) {}
        loadKPIs();
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JPanel createCard(String title) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(30, 41, 59), 0, getHeight(), new Color(15, 23, 42)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setPaint(new Color(51, 65, 85));
                g2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLbl = new JLabel(title);
        titleLbl.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titleLbl.setForeground(new Color(99, 179, 237));
        titleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.add(titleLbl);
        card.add(Box.createVerticalStrut(14));
        return card;
    }

    private JLabel infoLabel(String key, String value) {
        JLabel l = new JLabel("<html><font color='#94a3b8'>" + key + ": </font>" +
                "<font color='#e2e8f0'><b>" + (value != null ? value : "N/A") + "</b></font></html>");
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        return l;
    }

    private JButton createActionButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btn.setBackground(new Color(51, 65, 85));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        return btn;
    }

    private Color scoreColor(double score) {
        if (score >= 750) return new Color(74, 222, 128);   // green
        if (score >= 550) return new Color(250, 204, 21);   // yellow
        return new Color(248, 113, 113);                    // red
    }

    private String getTier(double score) {
        if (score >= 800) return "★ PLATINUM TIER";
        if (score >= 650) return "● GOLD TIER";
        if (score >= 500) return "◆ SILVER TIER";
        return "▲ BRONZE TIER";
    }
}
