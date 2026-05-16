package com.suraksha.setu.ui;

import com.suraksha.setu.models.Worker;
import com.suraksha.setu.services.WorkCertificateService;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog displaying the generated work certificate in a JTextArea.
 */
public class CertificateDialog extends JDialog {

    public CertificateDialog(Window owner, Worker worker) {
        super(owner, "Work Certificate — " + worker.getFullName(), ModalityType.APPLICATION_MODAL);
        setSize(560, 520);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(new Color(15, 23, 42));
        setLayout(new BorderLayout(12, 12));

        JLabel header = new JLabel("  📄 Digital Work Certificate");
        header.setFont(new Font("Segoe UI", Font.BOLD, 16));
        header.setForeground(new Color(99, 179, 237));
        header.setBorder(BorderFactory.createEmptyBorder(12, 0, 4, 0));
        add(header, BorderLayout.NORTH);

        JTextArea certArea = new JTextArea();
        certArea.setEditable(false);
        certArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        certArea.setBackground(new Color(30, 41, 59));
        certArea.setForeground(new Color(226, 232, 240));
        certArea.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        certArea.setLineWrap(false);

        // Generate certificate text using WorkCertificateService
        try {
            WorkCertificateService svc = new WorkCertificateService();
            certArea.setText(svc.generateCertificate(worker));
        } catch (Exception e) {
            certArea.setText("Error generating certificate: " + e.getMessage());
        }

        JScrollPane scroll = new JScrollPane(certArea);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(51, 65, 85)));
        scroll.getViewport().setBackground(new Color(30, 41, 59));
        add(scroll, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.setOpaque(false);

        JButton copyBtn = new JButton("📋 Copy");
        copyBtn.setBackground(new Color(71, 85, 105));
        copyBtn.setForeground(Color.WHITE);
        copyBtn.setFocusPainted(false);
        copyBtn.setBorderPainted(false);
        copyBtn.addActionListener(e -> {
            certArea.selectAll();
            certArea.copy();
            JOptionPane.showMessageDialog(this, "Certificate copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(new Color(59, 130, 246));
        closeBtn.setForeground(Color.WHITE);
        closeBtn.setFocusPainted(false);
        closeBtn.setBorderPainted(false);
        closeBtn.addActionListener(e -> dispose());

        btnPanel.add(copyBtn); btnPanel.add(closeBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
}
