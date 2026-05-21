package com.chatapp.view;

import com.chatapp.database.DBConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.sql.*;
import java.util.Base64;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class ChatScreen extends JFrame {

    private final String currentUser;
    private final JTextPane chatLog = new JTextPane();
    private final JTextField msgField = new JTextField();
    private final JPanel userListPanel = new JPanel();
    private final JLabel chatHeaderLabel = new JLabel("Global Chat Room");
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    private final Timer refreshTimer;

    private List<String> loadedMessages = new ArrayList<>();
    private List<String> loadedUsers = new ArrayList<>();
    // Holds a pending image before sending
    private byte[] pendingImageData = null;
    private String pendingImageMime = null;
    private String pendingImageName = null;

    public ChatScreen(String username) {
        this.currentUser = username;

        setTitle("ChatApp - " + currentUser);
        setSize(850, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Main background panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(24, 24, 28));

        // 1. Sidebar Panel (Left)
        JPanel sidebar = new JPanel(new BorderLayout());
        sidebar.setPreferredSize(new Dimension(240, 600));
        sidebar.setBackground(new Color(18, 18, 22));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(40, 40, 45)));

        // Sidebar Header
        JLabel sidebarTitle = new JLabel("Conversations", SwingConstants.LEFT);
        sidebarTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        sidebarTitle.setForeground(Color.WHITE);
        sidebarTitle.setBorder(new EmptyBorder(20, 20, 15, 20));
        sidebar.add(sidebarTitle, BorderLayout.NORTH);

        // Sidebar Users list (Scrollable)
        userListPanel.setLayout(new BoxLayout(userListPanel, BoxLayout.Y_AXIS));
        userListPanel.setBackground(new Color(18, 18, 22));
        JScrollPane sidebarScroll = new JScrollPane(userListPanel);
        sidebarScroll.setBorder(null);
        sidebarScroll.getVerticalScrollBar().setUnitIncrement(12);
        sidebarScroll.getViewport().setBackground(new Color(18, 18, 22));
        sidebar.add(sidebarScroll, BorderLayout.CENTER);

        // Sidebar bottom profile card
        JPanel profileCard = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        profileCard.setBackground(new Color(28, 28, 32));
        profileCard.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(40, 40, 45)));
        
        // Green status dot
        JPanel statusDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(new Color(0, 204, 136));
                g2d.fillOval(0, 0, 10, 10);
            }
        };
        statusDot.setPreferredSize(new Dimension(10, 10));
        statusDot.setBackground(new Color(28, 28, 32));

        JLabel profileName = new JLabel(currentUser);
        profileName.setFont(new Font("Segoe UI", Font.BOLD, 15));
        profileName.setForeground(Color.WHITE);

        // Logout button
        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFocusPainted(false);
        logoutBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        logoutBtn.setBackground(new Color(200, 50, 50));
        logoutBtn.setForeground(Color.WHITE);
        logoutBtn.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        logoutBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutBtn.addActionListener(e -> {
            // Close chat screen and return to login screen
            dispose();
            new LoginScreen();
        });

        profileCard.add(statusDot);
        profileCard.add(profileName);
        profileCard.add(Box.createHorizontalGlue()); // push logout to right
        profileCard.add(logoutBtn);
        sidebar.add(profileCard, BorderLayout.SOUTH);

        // 2. Chat Area Panel (Center)
        JPanel chatArea = new JPanel(new BorderLayout());
        chatArea.setBackground(new Color(24, 24, 28));

        // Chat header banner
        JPanel chatHeader = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 15));
        chatHeader.setBackground(new Color(24, 24, 28));
        chatHeader.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 40, 45)));
        
        chatHeaderLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        chatHeaderLabel.setForeground(Color.WHITE);
        chatHeader.add(chatHeaderLabel);
        chatArea.add(chatHeader, BorderLayout.NORTH);

        // Messages log (HTML-capable JTextPane for bubble styling)
        chatLog.setEditable(false);
        chatLog.setContentType("text/html");
        chatLog.setBackground(new Color(24, 24, 28));
        chatLog.setBorder(new EmptyBorder(10, 15, 10, 15));
        
        JScrollPane chatScroll = new JScrollPane(chatLog);
        chatScroll.setBorder(null);
        chatScroll.getViewport().setBackground(new Color(24, 24, 28));
        chatScroll.getVerticalScrollBar().setUnitIncrement(16);
        chatArea.add(chatScroll, BorderLayout.CENTER);
JPanel inputPanel = new JPanel(new BorderLayout(15, 0));
inputPanel.setBackground(new Color(24, 24, 28));
inputPanel.setBorder(new EmptyBorder(15, 20, 20, 20));




        // Sub‑panel to hold the text field and upload button
        JPanel fieldPanel = new JPanel(new BorderLayout(5, 0));
        fieldPanel.setBackground(new Color(24, 24, 28));
        styleTextField(msgField);
        fieldPanel.add(msgField, BorderLayout.CENTER);

        // Image upload button (+)
        JButton uploadBtn = new JButton("+");
        uploadBtn.setFocusPainted(false);
        uploadBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        uploadBtn.setBackground(new Color(70, 70, 75));
        uploadBtn.setForeground(Color.WHITE);
        uploadBtn.setPreferredSize(new Dimension(42, 42));
        uploadBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        uploadBtn.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new FileNameExtensionFilter("Image files", "png", "jpg", "jpeg", "gif", "bmp"));
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    pendingImageData = Files.readAllBytes(file.toPath());
                    pendingImageMime = Files.probeContentType(file.toPath());
                    pendingImageName = file.getName();
                    // Optionally give visual feedback (e.g., change button color)
                    uploadBtn.setBackground(new Color(0, 153, 255));
                } catch (IOException ex) {
                    showError("Failed to read image: " + ex.getMessage());
                }
            }
        });
        fieldPanel.add(uploadBtn, BorderLayout.EAST);

        // Send button
        JButton sendBtn = new JButton("Send");
        styleButton(sendBtn, new Color(0, 120, 215));
        sendBtn.addActionListener(e -> sendMessage());

        inputPanel.add(fieldPanel, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);
        chatArea.add(inputPanel, BorderLayout.SOUTH);

        // Assemble panels
        mainPanel.add(sidebar, BorderLayout.WEST);
        mainPanel.add(chatArea, BorderLayout.CENTER);
        add(mainPanel);

        // Load messages initially
        loadOnlineUsers();
        loadMessages();

        // Timer to poll messages and users list in real-time (every 1 second)
        refreshTimer = new Timer(1000, e -> {
            loadOnlineUsers();
            loadMessages();
        });
        refreshTimer.start();

        setVisible(true);
    }

    private void styleTextField(JTextField field) {
        field.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        field.setBackground(new Color(40, 40, 45));
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75)),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
    }

    private void styleButton(JButton button, Color color) {
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 15));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setPreferredSize(new Dimension(100, 42));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createLineBorder(color.darker()));
    }

    // Helper method to display error dialogs
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void sendMessage() {
        String msg = msgField.getText().trim();
        if (msg.isEmpty()) {
            return;
        }

        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            showError("Database connection failed. Cannot send message.");
            return;
        }

        try {
            if (pendingImageData != null) {
                // Insert image into images table first
                String imgSql = "INSERT INTO images (image_data, image_mime, image_name) VALUES (?, ?, ?)";
                try (PreparedStatement imgPs = conn.prepareStatement(imgSql, Statement.RETURN_GENERATED_KEYS)) {
                    imgPs.setBytes(1, pendingImageData);
                    imgPs.setString(2, pendingImageMime);
                    imgPs.setString(3, pendingImageName);
                    imgPs.executeUpdate();
                    try (ResultSet generatedKeys = imgPs.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            long imageId = generatedKeys.getLong(1);
                            // Insert message with image reference
                            String sql = "INSERT INTO messages (sender, message_text, image_id) VALUES (?, ?, ?)";
                            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                                ps.setString(1, currentUser);
                                ps.setString(2, msg);
                                ps.setLong(3, imageId);
                                ps.executeUpdate();
                            }
                        } else {
                            showError("Failed to obtain image ID after insert.");
                        }
                    }
                }
            } else {
                String sql = "INSERT INTO messages (sender, message_text) VALUES (?, ?)";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, currentUser);
                    ps.setString(2, msg);
                    ps.executeUpdate();
                }
            }
            System.out.println("Message sent by " + currentUser + ": " + msg + (pendingImageData != null ? " [image]" : ""));
            msgField.setText("");
            // Reset pending image after successful send
            pendingImageData = null;
            pendingImageMime = null;
            pendingImageName = null;
            loadMessages(); // Update UI instantly
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database error occurred while sending message: " + e.getMessage());
        }
    }

    /**
     * Loads the list of registered users dynamically from the database users table.
     */
    private void loadOnlineUsers() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            // silently ignore or could show UI error
            return;
        }

        List<String> users = new ArrayList<>();
        String sql = "SELECT username FROM users ORDER BY username ASC";

        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String user = rs.getString("username");
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // If the users list changed, redraw the sidebar list
        if (!users.equals(loadedUsers)) {
            loadedUsers = users;
            userListPanel.removeAll();

            // Always add a "Global Chat Room" at the top
            JPanel globalCard = createSidebarCard("Global Chat Room", "Active Session", true, false);
            userListPanel.add(globalCard);

            for (String user : loadedUsers) {
                boolean isMe = user.equals(currentUser);
                JPanel userCard = createSidebarCard(user, isMe ? "You" : "Online", false, isMe);
                userListPanel.add(userCard);
            }
            userListPanel.revalidate();
            userListPanel.repaint();
        }
    }

    /**
     * Helper to create styled user cards for the sidebar.
     */
    private JPanel createSidebarCard(String name, String subtext, boolean isGlobal, boolean isCurrent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setMaximumSize(new Dimension(240, 60));
        card.setPreferredSize(new Dimension(240, 60));
        card.setBackground(new Color(18, 18, 22));
        card.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel textPane = new JPanel(new GridLayout(2, 1));
        textPane.setBackground(new Color(18, 18, 22));

        JLabel nameLabel = new JLabel(name);
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        if (isGlobal) {
            nameLabel.setForeground(new Color(0, 153, 255));
        } else if (isCurrent) {
            nameLabel.setForeground(new Color(0, 204, 136));
        } else {
            nameLabel.setForeground(Color.WHITE);
        }
        
        JLabel subLabel = new JLabel(subtext);
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subLabel.setForeground(new Color(130, 130, 140));

        textPane.add(nameLabel);
        textPane.add(subLabel);
        card.add(textPane, BorderLayout.CENTER);

        // Hover & Active border
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(30, 30, 35)),
                new EmptyBorder(10, 15, 10, 15)
        ));

        return card;
    }

    /**
     * Reads all historical chat messages from the database and renders them as modern HTML bubble layout.
     */
    private void loadMessages() {
        Connection conn = DBConnection.getConnection();
        if (conn == null) {
            return;
        }

        List<String> rawMsgs = new ArrayList<>();
        StringBuilder html = new StringBuilder();
        
        html.append("<html><body style='font-family:Segoe UI, sans-serif; background-color:#18181c; margin:0; padding:10px;'>");
        // Load messages with optional image data via LEFT JOIN
        String sql = "SELECT m.sender, m.message_text, m.timestamp AS sent_at, i.image_data, i.image_mime FROM messages m LEFT JOIN images i ON m.image_id = i.id ORDER BY m.timestamp ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String sender = rs.getString("sender");
                String msg = rs.getString("message_text");
                Timestamp ts = rs.getTimestamp("sent_at");
                byte[] imgBytes = rs.getBytes("image_data");
                String imgMime = rs.getString("image_mime");
                String timeStr = ts != null ? timeFormat.format(ts) : "";

                rawMsgs.add(sender + "::" + msg + "::" + timeStr);

                boolean isMe = sender.equalsIgnoreCase(currentUser);

                // Build HTML for message bubble, include image if present
                if (isMe) {
                    html.append("<div style='margin-bottom:12px; text-align:right;'>")
                        .append("<div style='display:inline-block; max-width:65%; text-align:left; background-color:#0078d7; color:white; padding:10px 14px; border-radius:14px 14px 0px 14px; box-shadow: 0 1px 2px rgba(0,0,0,0.2);'>")
                        .append("<div style='font-size:11px; font-weight:bold; margin-bottom:4px; opacity:0.8;'>You</div>")
                        .append("<div style='font-size:14px;'>").append(escapeHtml(msg)).append("</div>");
                    if (imgBytes != null && imgMime != null) {
                        String base64 = Base64.getEncoder().encodeToString(imgBytes);
                        html.append("<div style='margin-top:8px;'><img src='data:" + imgMime + ";base64," + base64 + "' style='max-width:100%; border-radius:8px;'/></div>");
                    }
                    html.append("<div style='font-size:10px; text-align:right; margin-top:4px; opacity:0.6;'>" + timeStr + "</div>")
                        .append("</div></div>");
                } else {
                    html.append("<div style='margin-bottom:12px; text-align:left;'>")
                        .append("<div style='display:inline-block; max-width:65%; background-color:#28282d; color:white; padding:10px 14px; border-radius:14px 14px 14px 0px; box-shadow: 0 1px 2px rgba(0,0,0,0.2);'>")
                        .append("<div style='font-size:11px; font-weight:bold; color:#00a366; margin-bottom:4px;'>").append(sender).append("</div>")
                        .append("<div style='font-size:14px;'>").append(escapeHtml(msg)).append("</div>");
                    if (imgBytes != null && imgMime != null) {
                        String base64 = Base64.getEncoder().encodeToString(imgBytes);
                        html.append("<div style='margin-top:8px;'><img src='data:" + imgMime + ";base64," + base64 + "' style='max-width:100%; border-radius:8px;'/></div>");
                    }
                    html.append("<div style='font-size:10px; text-align:right; margin-top:4px; opacity:0.5;'>" + timeStr + "</div>")
                        .append("</div></div>");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        html.append("</body></html>");

        // Force UI update only if messages actually changed to prevent scroll jumping
        if (!rawMsgs.equals(loadedMessages)) {
            loadedMessages = rawMsgs;
            chatLog.setText(html.toString());
            
            // Auto scroll to bottom
            SwingUtilities.invokeLater(() -> {
                chatLog.setCaretPosition(chatLog.getDocument().getLength());
            });
        }
    }

    private String escapeHtml(String val) {
        if (val == null) return "";
        return val.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }

    @Override
    public void dispose() {
        if (refreshTimer != null) {
            refreshTimer.stop();
        }
        super.dispose();
    }
}
