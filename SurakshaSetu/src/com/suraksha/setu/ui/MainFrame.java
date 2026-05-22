package com.suraksha.setu.ui;

import com.suraksha.setu.models.User;
import com.suraksha.setu.models.Worker;
import com.suraksha.setu.services.AuthService;
import com.suraksha.setu.services.TrustScoreUpdater;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Main application window — JFrame with JMenuBar + JTabbedPane.
 * Demonstrates: JTabbedPane, JMenuBar, JMenu, JMenuItem, event handling.
 */
public class MainFrame extends JFrame {

    private final User               currentUser;
    private final JTabbedPane        tabs;
    private       TrustScoreUpdater  updaterThread;

    public MainFrame(User user) {
        super("SurakshaSetu — " + user.getDisplayName());
        this.currentUser = user;
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1000, 680);
        setLocationRelativeTo(null);

        // Dark theme for content pane
        getContentPane().setBackground(new Color(15, 23, 42));

        // ── Menu Bar ─────────────────────────────────────────────────────────
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(15, 23, 42));
        menuBar.setBorder(BorderFactory.createEmptyBorder());

        JMenu fileMenu  = createMenu("File");
        JMenu viewMenu  = createMenu("View");
        JMenu toolsMenu = createMenu("Tools");
        JMenu helpMenu  = createMenu("Help");

        JMenuItem logoutItem   = createMenuItem("Logout");
        JMenuItem exitItem     = createMenuItem("Exit");
        JMenuItem refreshItem  = createMenuItem("Refresh Data");
        JMenuItem calcItem     = createMenuItem("Recalculate Trust Score");
        JMenuItem aboutItem    = createMenuItem("About SurakshaSetu");

        fileMenu.add(logoutItem); fileMenu.addSeparator(); fileMenu.add(exitItem);
        viewMenu.add(refreshItem);
        toolsMenu.add(calcItem);
        helpMenu.add(aboutItem);

        menuBar.add(fileMenu); menuBar.add(viewMenu);
        menuBar.add(toolsMenu); menuBar.add(helpMenu);
        setJMenuBar(menuBar);

        // ── Tabs ─────────────────────────────────────────────────────────────
        tabs = new JTabbedPane(JTabbedPane.TOP);
        tabs.setBackground(new Color(15, 23, 42));
        tabs.setForeground(Color.WHITE);
        tabs.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // Always-visible tabs
        if (user instanceof Worker) {
            Worker worker = (Worker) user;
            tabs.addTab("📊 Dashboard",    new WorkerDashboardPanel(worker));
            tabs.addTab("📝 Work Logger",  new WorkLoggerPanel(worker));
            tabs.addTab("💰 Financials",   new FinancialHubPanel(worker));
            tabs.addTab("🏦 Loans",        new LoanEligibilityPanel(worker));
            tabs.addTab("🛡️ Benefits",     new BenefitEnrollmentPanel(worker));
        }

        // Admin tab — only for admins
        if (user.isAdmin()) {
            tabs.addTab("📊 Dashboard",    new WorkerDashboardPanel(null));
            tabs.addTab("🔑 Admin Panel",  new AdminPanel());
        }

        add(tabs, BorderLayout.CENTER);

        // Status bar
        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.setBackground(new Color(15, 23, 42));
        statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)));
        JLabel statusLbl = new JLabel("  Logged in as: " + user.getDisplayName()
                + "  |  Role: " + user.getRole());
        statusLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLbl.setForeground(new Color(100, 116, 139));
        statusBar.add(statusLbl, BorderLayout.WEST);

        JLabel threadLbl = new JLabel("  Background Updater: ");
        threadLbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        threadLbl.setForeground(new Color(100, 116, 139));
        statusBar.add(threadLbl, BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        // ── Event Listeners ───────────────────────────────────────────────────
        logoutItem.addActionListener(e -> doLogout());
        exitItem.addActionListener(e -> confirmExit());
        refreshItem.addActionListener(e -> refreshCurrentTab());
        calcItem.addActionListener(e -> recalculateTrustScore());
        aboutItem.addActionListener(e -> showAbout());

        // WindowListener — cleanup on close
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) { confirmExit(); }
            @Override public void windowOpened(WindowEvent e)  { startBackgroundUpdater(threadLbl); }
        });
    }

    private void startBackgroundUpdater(JLabel statusLbl) {
        updaterThread = new TrustScoreUpdater();
        updaterThread.start();
        statusLbl.setText("  Background Updater: ● Running  ");
        statusLbl.setForeground(new Color(74, 222, 128));

        // Check thread status using isAlive()
        Timer statusTimer = new Timer(5000, e -> {
            if (updaterThread != null && updaterThread.isAlive()) {
                statusLbl.setText("  Updater: ● Active (updates: " + updaterThread.getUpdateCount() + ")  ");
            }
        });
        statusTimer.start();
    }

    private void doLogout() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (updaterThread != null) updaterThread.stopUpdater();
            new AuthService().logout();
            SwingUtilities.invokeLater(() -> {
                new LoginFrame().setVisible(true);
                dispose();
            });
        }
    }

    private void confirmExit() {
        int choice = JOptionPane.showConfirmDialog(this,
            "Exit SurakshaSetu?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            if (updaterThread != null) updaterThread.stopUpdater();
            System.exit(0);
        }
    }

    private void refreshCurrentTab() {
        Component current = tabs.getSelectedComponent();
        if (current instanceof Refreshable) {
            ((Refreshable) current).refresh();
            JOptionPane.showMessageDialog(this, "Data refreshed.", "Refresh", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void recalculateTrustScore() {
        if (currentUser instanceof Worker) {
            Worker w = (Worker) currentUser;
            try {
                com.suraksha.setu.services.TrustScoreService svc =
                        new com.suraksha.setu.services.TrustScoreService();
                double score = svc.calculateAndUpdate(w.getWorkerId());
                w.setCurrentTrustScore(score);
                JOptionPane.showMessageDialog(this,
                    String.format("Trust score updated: %.1f / 1000", score),
                    "Score Updated", JOptionPane.INFORMATION_MESSAGE);
                refreshCurrentTab();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Select a worker to recalculate.",
                "Info", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void showAbout() {
        JOptionPane.showMessageDialog(this,
            "SurakshaSetu v1.0\nDigital Identity & Trust Management\nfor Gig Economy Workers\n\n" +
            "Stack: Java 11 + Swing + PostgreSQL\nDeveloped as a DBMS Project",
            "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private JMenu createMenu(String title) {
        JMenu m = new JMenu(title);
        m.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        m.setForeground(new Color(226, 232, 240));
        return m;
    }

    private JMenuItem createMenuItem(String title) {
        JMenuItem item = new JMenuItem(title);
        item.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        return item;
    }

    /** Marker interface for panels that support data refresh. */
    public interface Refreshable {
        void refresh();
    }
}
