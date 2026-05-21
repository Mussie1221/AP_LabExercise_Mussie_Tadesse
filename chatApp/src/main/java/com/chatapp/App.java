package com.chatapp;

import com.chatapp.view.LoginScreen;
import javax.swing.SwingUtilities;

/**
 * Main application entry point for the Swing Chat Application.
 */
public class App {

    public static void main(String[] args) {
        // Set clean modern system style for desktop OS integration
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Fallback silently to standard styling if Nimbus is not available
        }

        // Initialize the login screen on the Swing Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new LoginScreen());
    }
}
