package com.suraksha.setu.ui;

import com.suraksha.setu.dao.WorkerDAO;
import com.suraksha.setu.models.Worker;
import com.suraksha.setu.services.AuthService;
import com.suraksha.setu.util.PasswordHasher;
import com.suraksha.setu.util.DigitalIDGenerator;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Modern, beautifully styled modal dialog to register a new worker.
 */
public class RegisterWorkerDialog extends JDialog {

    private final WorkerDAO workerDAO = new WorkerDAO();
    private final AuthService authService = new AuthService();

    private JTextField nameField;
    private JTextField phoneField;
    private JTextField aadhaarField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel statusLabel;
    private boolean registrationSuccessful = false;

    public RegisterWorkerDialog(Window owner) {
        super(owner, "Register New Worker", ModalityType.APPLICATION_MODAL);
        setSize(480, 600);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(16, 16));

        // Header Panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(20, 24, 0, 24));

        JLabel title = new JLabel("➕ Register New Worker");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(new Color(99, 179, 237));
        headerPanel.add(title, BorderLayout.NORTH);

        JLabel subtitle = new JLabel("Create a new digital identity and user account.");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitle.setForeground(new Color(148, 163, 184));
        headerPanel.add(subtitle, BorderLayout.SOUTH);

        add(headerPanel, BorderLayout.NORTH);

        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(30, 41, 59));
        formPanel.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.weightx = 1.0;

        // Form fields
        gbc.gridy = 0;
        formPanel.add(createFormLabel("Full Name:"), gbc);
        nameField = createStyledTextField("e.g. Rajesh Kumar");
        gbc.gridy = 1;
        formPanel.add(nameField, gbc);

        gbc.gridy = 2;
        formPanel.add(createFormLabel("Phone Number:"), gbc);
        phoneField = createStyledTextField("e.g. 9876543210");
        gbc.gridy = 3;
        formPanel.add(phoneField, gbc);

        gbc.gridy = 4;
        formPanel.add(createFormLabel("Aadhaar Number:"), gbc);
        aadhaarField = createStyledTextField("12-digit number");
        gbc.gridy = 5;
        formPanel.add(aadhaarField, gbc);

        gbc.gridy = 6;
        formPanel.add(createFormLabel("Account Username:"), gbc);
        usernameField = createStyledTextField("Username for login");
        gbc.gridy = 7;
        formPanel.add(usernameField, gbc);

        gbc.gridy = 8;
        formPanel.add(createFormLabel("Account Password:"), gbc);
        passwordField = createStyledPasswordField();
        gbc.gridy = 9;
        formPanel.add(passwordField, gbc);

        // Add a vertical spacer to push everything to the top
        gbc.gridy = 10;
        gbc.weighty = 1.0;
        formPanel.add(Box.createGlue(), gbc);

        // Scroll Pane wrapping formPanel
        JScrollPane scrollPane = new JScrollPane(formPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85), 1));
        scrollPane.getViewport().setBackground(new Color(30, 41, 59));
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

        // Wrap scrollPane to keep it padded
        JPanel centerWrapper = new JPanel(new BorderLayout());
        centerWrapper.setOpaque(false);
        centerWrapper.setBorder(BorderFactory.createEmptyBorder(8, 24, 8, 24));
        centerWrapper.add(scrollPane, BorderLayout.CENTER);
        add(centerWrapper, BorderLayout.CENTER);

        // Bottom Action Panel
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(0, 24, 20, 24));

        statusLabel = new JLabel(" ");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusLabel.setForeground(new Color(239, 68, 68)); // Error color
        bottomPanel.add(statusLabel, BorderLayout.NORTH);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        btnPanel.setOpaque(false);

        JButton cancelBtn = createButton("Cancel", new Color(71, 85, 105));
        cancelBtn.addActionListener(e -> dispose());

        JButton submitBtn = createButton("Register", new Color(34, 197, 94));
        submitBtn.addActionListener(e -> handleRegistration());

        btnPanel.add(cancelBtn);
        btnPanel.add(submitBtn);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);

        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(new Color(148, 163, 184));
        return label;
    }

    private JTextField createStyledTextField(String placeholder) {
        JTextField f = new JTextField();
        f.setBackground(new Color(15, 23, 42));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        // No tooltip — labels above each field already provide context
        return f;
    }

    private JPasswordField createStyledPasswordField() {
        JPasswordField f = new JPasswordField();
        f.setBackground(new Color(15, 23, 42));
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(71, 85, 105)),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
        return f;
    }

    private JButton createButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        return btn;
    }

    private void handleRegistration() {
        String name = nameField.getText().trim();
        String phone = phoneField.getText().trim();
        String aadhaar = aadhaarField.getText().trim();
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Validations
        if (name.isEmpty()) {
            showError("⚠ Full name is required.");
            return;
        }
        if (name.length() < 3) {
            showError("⚠ Name must be at least 3 characters.");
            return;
        }
        if (!PasswordHasher.isValidPhone(phone)) {
            showError("⚠ Phone must be a valid 10-digit number starting with 6-9.");
            return;
        }
        if (!PasswordHasher.isValidAadhaar(aadhaar)) {
            showError("⚠ Aadhaar must be a valid 12-digit number.");
            return;
        }
        if (username.isEmpty()) {
            showError("⚠ Username is required.");
            return;
        }
        if (username.length() < 4) {
            showError("⚠ Username must be at least 4 characters.");
            return;
        }
        if (password.isEmpty()) {
            showError("⚠ Password is required.");
            return;
        }
        if (password.length() < 6) {
            showError("⚠ Password must be at least 6 characters.");
            return;
        }

        try {
            // Check if username is taken
            if (authService.isUsernameTaken(username)) {
                showError("⚠ Username is already taken.");
                return;
            }

            statusLabel.setText("Processing registration...");
            statusLabel.setForeground(new Color(250, 204, 21)); // Warning/Info yellow

            // 1. Generate digital ID seeded from Aadhaar
            String digitalId = DigitalIDGenerator.generateID(aadhaar);

            // 2. Hash the Aadhaar for database storage
            String aadhaarHash = PasswordHasher.hash(aadhaar);

            // 3. Create the Worker model
            Worker worker = new Worker(0, digitalId, name, phone, aadhaarHash, 500.0, new Timestamp(System.currentTimeMillis()));

            // 4. Save worker to DB
            workerDAO.save(worker);

            // 5. Register account for user
            authService.registerWorkerAccount(worker.getWorkerId(), username, password);

            registrationSuccessful = true;
            JOptionPane.showMessageDialog(this,
                    "✓ Worker registered successfully!\n" +
                            "Digital ID: " + digitalId + "\n" +
                            "Username: " + username,
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (SQLException e) {
            showError("✗ DB Error: " + e.getMessage());
        }
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setForeground(new Color(239, 68, 68)); // Error red
    }

    public boolean isRegistrationSuccessful() {
        return registrationSuccessful;
    }
}
