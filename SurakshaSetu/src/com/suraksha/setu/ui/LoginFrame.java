package com.suraksha.setu.ui;

import com.suraksha.setu.models.User;
import com.suraksha.setu.models.Worker;
import com.suraksha.setu.services.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Login window — first screen the user sees.
 * Demonstrates: JFrame, JTextField, JPasswordField, JButton, ActionListener.
 */
public class LoginFrame extends JFrame {

    private final JTextField     usernameField;
    private final JPasswordField passwordField;
    private final JLabel         statusLabel;
    private final AuthService    authService = new AuthService();

    public LoginFrame() {
        super("SurakshaSetu — Login");
        
        // Globally disable tooltips
        ToolTipManager.sharedInstance().setEnabled(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 520);
        setLocationRelativeTo(null);
        setResizable(false);

        // Main panel with gradient background
        JPanel main = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, new Color(15, 23, 42),
                        0, getHeight(), new Color(30, 41, 59)));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // ── Logo / Header ────────────────────────────────────────────────────
        JPanel header = new JPanel(new GridLayout(3, 1, 0, 4));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(40, 30, 20, 30));

        JLabel logo = new JLabel("🛡 SurakshaSetu", SwingConstants.CENTER);
        logo.setFont(new Font("Segoe UI", Font.BOLD, 26));
        logo.setForeground(new Color(99, 179, 237));

        JLabel tagline = new JLabel("Digital Identity for Gig Workers", SwingConstants.CENTER);
        tagline.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tagline.setForeground(new Color(148, 163, 184));

        JLabel title = new JLabel("Sign In to Your Account", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        header.add(logo); header.add(tagline); header.add(title);

        // ── Form ─────────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        form.setBorder(BorderFactory.createEmptyBorder(10, 40, 10, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 0, 6, 0);
        gbc.weightx = 1.0;

        usernameField = createField("Username");
        passwordField = new JPasswordField();
        styleField(passwordField);

        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(252, 129, 74));

        JButton loginBtn = createButton("Sign In", new Color(59, 130, 246));
        JButton regBtn   = createButton("Register New Worker", new Color(34, 197, 94));

        // Demo hint
        JLabel hint = new JLabel("<html><center><font color='#94a3b8' size='2'>" +
                "Demo: username <b>rajesh_k</b> / password <b>password123</b><br>" +
                "Admin: username <b>admin</b> / password <b>admin123</b></font></center></html>",
                SwingConstants.CENTER);

        gbc.gridy = 0; form.add(makeLabel("Username"), gbc);
        gbc.gridy = 1; form.add(usernameField, gbc);
        gbc.gridy = 2; form.add(makeLabel("Password"), gbc);
        gbc.gridy = 3; form.add(passwordField, gbc);
        gbc.gridy = 4; form.add(statusLabel, gbc);
        gbc.gridy = 5; form.add(loginBtn, gbc);
        gbc.gridy = 6; form.add(regBtn, gbc);
        gbc.gridy = 7; form.add(hint, gbc);

        // ── Event Handling ───────────────────────────────────────────────────
        // ActionListener on login button
        loginBtn.addActionListener(e -> doLogin());

        // ActionListener on register button
        regBtn.addActionListener(e -> openRegister());

        // Allow pressing Enter in password field
        passwordField.addActionListener(e -> doLogin());

        // DocumentListener — real-time clear of error label when typing
        usernameField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { statusLabel.setText(" "); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { statusLabel.setText(" "); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { statusLabel.setText(" "); }
        });

        // WindowListener for cleanup
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                System.out.println("Application closing.");
                System.exit(0);
            }
        });

        main.add(header, BorderLayout.NORTH);
        main.add(form, BorderLayout.CENTER);
        setContentPane(main);
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("⚠ Please enter username and password.");
            return;
        }
        try {
            User user = authService.login(username, password);
            if (user == null) {
                statusLabel.setText("✗ Invalid username or password.");
                passwordField.setText("");
                return;
            }
            // Open main window
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(user);
                mainFrame.setVisible(true);
                dispose();
            });
        } catch (Exception ex) {
            statusLabel.setText("✗ DB Error: " + ex.getMessage());
        }
    }

    private void openRegister() {
        RegisterWorkerDialog dialog = new RegisterWorkerDialog(this);
        dialog.setVisible(true);
    }

    // ── UI Helper Methods ────────────────────────────────────────────────────
    private JTextField createField(String placeholder) {
        JTextField f = new JTextField();
        styleField(f);
        return f;
    }

    private void styleField(JTextField f) {
        f.setPreferredSize(new Dimension(0, 38));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBackground(new Color(30, 41, 59));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1),
            BorderFactory.createEmptyBorder(4, 10, 4, 10)));
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(0, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        l.setForeground(new Color(148, 163, 184));
        return l;
    }
}
