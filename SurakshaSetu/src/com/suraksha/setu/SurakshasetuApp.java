package com.suraksha.setu;

import com.suraksha.setu.ui.LoginFrame;

import javax.swing.*;

/**
 * Application entry point.
 * Sets Nimbus Look & Feel (with fallback to system L&F).
 * Launches LoginFrame on the Event Dispatch Thread.
 */
public class SurakshasetuApp {

    public static void main(String[] args) {
        // Set Nimbus L&F with fallback
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                System.err.println("Could not set L&F: " + ex.getMessage());
            }
        }

        // Customize Nimbus color palette
        UIManager.put("nimbusBase",          new java.awt.Color(15, 23, 42));
        UIManager.put("nimbusBlueGrey",      new java.awt.Color(30, 41, 59));
        UIManager.put("control",             new java.awt.Color(30, 41, 59));
        UIManager.put("text",                new java.awt.Color(226, 232, 240));
        UIManager.put("nimbusSelectionBackground", new java.awt.Color(59, 130, 246));

        // Globally disable tooltips — Nimbus L&F auto-generates them from
        // component text (tab titles, labels, etc.) which clutters the UI.
        ToolTipManager.sharedInstance().setEnabled(false);

        // Launch on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            // Re-assert global tooltip disable on Event Dispatch Thread (EDT)
            ToolTipManager.sharedInstance().setEnabled(false);

            System.out.println("=================================================");
            System.out.println("   SurakshaSetu — Digital Identity Platform");
            System.out.println("   Gig Worker Trust Management System v1.0");
            System.out.println("=================================================");

            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}
