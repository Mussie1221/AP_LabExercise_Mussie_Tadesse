package com.chatapp.view;

import com.chatapp.database.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class LoginScreen extends JFrame {

    private final JTextField userField = new JTextField();
    private final JPasswordField passField = new JPasswordField();

    public LoginScreen() {

        setTitle("ChatApp");
        setSize(450, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        // Main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(24, 24, 28));
        mainPanel.setLayout(new BorderLayout());

        // Header
        JLabel title = new JLabel("ChatApp Login", SwingConstants.CENTER);
        title.setFont(new Font("Segoe UI", Font.BOLD, 28));
        title.setForeground(Color.WHITE);
        title.setBorder(new EmptyBorder(20, 0, 10, 0));

        // Form panel
        JPanel formPanel = new JPanel();
        formPanel.setBackground(new Color(24, 24, 28));
        formPanel.setLayout(new GridLayout(4, 1, 10, 5));
        formPanel.setBorder(new EmptyBorder(20, 40, 20, 40));

        JLabel userLabel = new JLabel("Username");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(Color.WHITE);

        JLabel passLabel = new JLabel("Password");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passLabel.setForeground(Color.WHITE);

        styleTextField(userField);
        styleTextField(passField);

        formPanel.add(userLabel);
        formPanel.add(userField);
        formPanel.add(passLabel);
        formPanel.add(passField);

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(24, 24, 28));

        JButton loginBtn = new JButton("Login");
        JButton signupBtn = new JButton("Signup");

        styleButton(loginBtn, new Color(0, 120, 215));
        styleButton(signupBtn, new Color(0, 153, 102));

        buttonPanel.add(loginBtn);
        buttonPanel.add(signupBtn);

        // Actions
        loginBtn.addActionListener(e -> login());
        signupBtn.addActionListener(e -> signup());

        // Add components
        mainPanel.add(title, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        setVisible(true);
    }

    // TextField Styling
    private void styleTextField(JTextField field) {

        field.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        field.setBackground(new Color(40, 40, 45));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);

        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75)),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    // Button Styling
    private void styleButton(JButton button, Color color) {

        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(120, 40));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // Signup
    private void signup() {

        String u = userField.getText().trim();
        String p = new String(passField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Enter both fields");

            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this,
                    "Error: Could not connect to the MySQL database.\n" +
                    "1. Make sure your MySQL Server is running.\n" +
                    "2. Verify root password is 'mosses143'.\n" +
                    "3. Check your IDE's run/output window for detailed log error output.");
            return;
        }

        String sql =
                "INSERT INTO users (username, password) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u);
            ps.setString(2, p);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this,
                    "Signup successful");

        } catch (SQLIntegrityConstraintViolationException ex) {

            JOptionPane.showMessageDialog(this,
                    "Username already exists");

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Login
    private void login() {

        String u = userField.getText().trim();
        String p = new String(passField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Enter both fields");

            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(this,
                    "Error: Could not connect to the MySQL database.\n" +
                    "1. Make sure your MySQL Server is running.\n" +
                    "2. Verify root password is 'holderingP' or 'mosses143'.\n" +
                    "3. Check your IDE's run/output window for detailed log error output.");
            return;
        }

        String sql =
                "SELECT * FROM users WHERE username=? AND password=?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, u);
            ps.setString(2, p);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                dispose();

                SwingUtilities.invokeLater(() ->
                        new ChatScreen(u));

            } else {

                JOptionPane.showMessageDialog(this,
                        "Invalid username/password");
            }

        } catch (Exception ex) {

            ex.printStackTrace();

            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(LoginScreen::new);
    }
}
