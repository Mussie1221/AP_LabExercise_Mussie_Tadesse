package com.chatapp.database;

import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Robust database connection utility targeting the 'chat' database
 * with the single password 'mosses143'. Uses standard Class.forName driver loading.
 */
public class DBConnection {

    private static final Logger LOGGER = Logger.getLogger(DBConnection.class.getName());

    // Database configuration targets
    private static final String DB_NAME = "chat";
    private static final String PASSWORD = "mosses143";
    private static final String USER = "root";

    private static String resolvedUrl = null;
    private static boolean isInitialized = false;

    static {
        // Standard Class.forName driver loading as requested
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            LOGGER.info("MySQL JDBC Driver Class.forName loaded successfully.");
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.mysql.jdbc.Driver");
                LOGGER.info("Legacy MySQL JDBC Driver Class.forName loaded successfully.");
            } catch (ClassNotFoundException ex) {
                LOGGER.log(Level.SEVERE, "CRITICAL: Could not find or load MySQL Connector/J class in the classpath!", ex);
                System.err.println("\n========================================================");
                System.err.println("WARNING: MySQL JDBC Driver jar is NOT in the classpath.");
                System.err.println("Please click the 'Reload All Maven Projects' button in IntelliJ.");
                System.err.println("========================================================\n");
            }
        }
    }

    /**
     * Obtains a Connection to the local MySQL database 'chat'.
     */
    public static Connection getConnection() {
    try {
        Class.forName("com.mysql.cj.jdbc.Driver");
    } catch (ClassNotFoundException e) {
        LOGGER.log(Level.SEVERE, "MySQL JDBC Driver not found", e);
    }
    if (resolvedUrl != null) {
            try {
                Connection conn = DriverManager.getConnection(resolvedUrl, USER, PASSWORD);
                ensureSchemaInitialized(conn);
                return conn;
            } catch (SQLException e) {
                resolvedUrl = null;
            }
        }

        SQLException lastException = null;

        // Connect to MySQL server root to verify/create database 'chat'
        String serverUrl = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            LOGGER.info("Connecting to MySQL server root to verify/create 'chat' with password 'mosses143'...");
            try (Connection tempConn = DriverManager.getConnection(serverUrl, USER, PASSWORD);
                 Statement stmt = tempConn.createStatement()) {
                stmt.execute("CREATE DATABASE IF NOT EXISTS chat;");
                LOGGER.info("Database 'chat' verified/created successfully!");
            }

            // Connect directly to the 'chat' database
            String targetUrl = "jdbc:mysql://localhost:3306/chat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
            Connection conn = DriverManager.getConnection(targetUrl, USER, PASSWORD);
            
            resolvedUrl = targetUrl;
            LOGGER.info("Database connection successfully established to 'chat'!");
            
            ensureSchemaInitialized(conn);
            return conn;
        } catch (SQLException e) {
            lastException = e;
            LOGGER.log(Level.WARNING, "Direct root connection/creation failed, trying direct connection to 'chat'...", e);
        }

        // Try direct connection fallback to pre-existing 'chat' database
        String directUrl = "jdbc:mysql://localhost:3306/chat?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        try {
            Connection conn = DriverManager.getConnection(directUrl, USER, PASSWORD);
            resolvedUrl = directUrl;
            ensureSchemaInitialized(conn);
            return conn;
        } catch (SQLException e) {
            lastException = e;
        }

        // Output detailed console diagnostics on connection failure
        System.err.println("\n========================================================");
        System.err.println("CRITICAL ERROR: DATABASE CONNECTION FAILED!");
        System.err.println("Configuration used:");
        System.err.println("  - Database Name: " + DB_NAME);
        System.err.println("  - Username: " + USER);
        System.err.println("  - Password: " + PASSWORD);
        System.err.println("\nPlease check the following diagnostics:");
        if (lastException != null) {
            System.err.println("  - Error Code: " + lastException.getErrorCode());
            System.err.println("  - SQL State: " + lastException.getSQLState());
            System.err.println("  - Message: " + lastException.getMessage());
            System.err.println("\nStack Trace:");
            lastException.printStackTrace();
        } else {
            System.err.println("  - No SQLException captured.");
        }
        System.err.println("========================================================\n");

        return null;
    }

    /**
     * Checks and creates the users and messages tables in the 'chat' database if not exists.
     */
    private static synchronized void ensureSchemaInitialized(Connection conn) {
        if (isInitialized) {
            return;
        }

        try (Statement stmt = conn.createStatement()) {
            // 1. Create users table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                                       "id INT AUTO_INCREMENT PRIMARY KEY," +
                                       "username VARCHAR(50) NOT NULL UNIQUE," +
                                       "password VARCHAR(255) NOT NULL" +
                                       ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.execute(createUsersTable);

            // 2. Create images table
            String createImagesTable = "CREATE TABLE IF NOT EXISTS images (" +
                                      "id INT AUTO_INCREMENT PRIMARY KEY," +
                                      "image_data LONGBLOB," +
                                      "image_mime VARCHAR(255)," +
                                      "image_name VARCHAR(255)" +
                                      ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
            stmt.execute(createImagesTable);

            // 3. Check if 'messages' table exists
            boolean tableExists = false;
            try (ResultSet rs = conn.getMetaData().getTables("chat", null, "messages", null)) {
                if (rs.next()) {
                    tableExists = true;
                }
            }

            if (tableExists) {
                // Check if it has 'image_id' column
                boolean hasImageId = false;
                try (ResultSet rs = conn.getMetaData().getColumns("chat", null, "messages", "image_id")) {
                    if (rs.next()) {
                        hasImageId = true;
                    }
                }

                if (!hasImageId) {
                    LOGGER.info("Detected old 'messages' table schema. Performing migration...");
                    
                    // Rename old table
                    stmt.execute("RENAME TABLE messages TO messages_old;");
                    
                    // Create new messages table
                    String createMessagesTable = "CREATE TABLE messages (" +
                                                 "id INT AUTO_INCREMENT PRIMARY KEY," +
                                                 "sender VARCHAR(50) NOT NULL," +
                                                 "message_text TEXT NOT NULL," +
                                                 "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                                 "image_id INT NULL," +
                                                 "FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE SET NULL" +
                                                 ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
                    stmt.execute(createMessagesTable);
                    
                    // Migrate data from messages_old to new schema
                    String selectOld = "SELECT sender, message_text, sent_at, image_data, image_mime, image_name FROM messages_old";
                    try (Statement selectStmt = conn.createStatement();
                         ResultSet oldRs = selectStmt.executeQuery(selectOld)) {
                        
                        while (oldRs.next()) {
                            String sender = oldRs.getString("sender");
                            String text = oldRs.getString("message_text");
                            Timestamp ts = oldRs.getTimestamp("sent_at");
                            byte[] imgData = oldRs.getBytes("image_data");
                            String imgMime = oldRs.getString("image_mime");
                            String imgName = oldRs.getString("image_name");
                            
                            Long imageId = null;
                            if (imgData != null) {
                                String insertImg = "INSERT INTO images (image_data, image_mime, image_name) VALUES (?, ?, ?)";
                                try (PreparedStatement psImg = conn.prepareStatement(insertImg, Statement.RETURN_GENERATED_KEYS)) {
                                    psImg.setBytes(1, imgData);
                                    psImg.setString(2, imgMime);
                                    psImg.setString(3, imgName);
                                    psImg.executeUpdate();
                                    try (ResultSet gk = psImg.getGeneratedKeys()) {
                                        if (gk.next()) {
                                            imageId = gk.getLong(1);
                                        }
                                    }
                                }
                            }
                            
                            String insertMsg = "INSERT INTO messages (sender, message_text, timestamp, image_id) VALUES (?, ?, ?, ?)";
                            try (PreparedStatement psMsg = conn.prepareStatement(insertMsg)) {
                                psMsg.setString(1, sender);
                                psMsg.setString(2, text);
                                psMsg.setTimestamp(3, ts);
                                if (imageId != null) {
                                    psMsg.setLong(4, imageId);
                                } else {
                                    psMsg.setNull(4, java.sql.Types.INTEGER);
                                }
                                psMsg.executeUpdate();
                            }
                        }
                    }
                    
                    // Drop old table
                    stmt.execute("DROP TABLE messages_old;");
                    LOGGER.info("Database migration complete!");
                }
            } else {
                // Table doesn't exist, create it from scratch
                String createMessagesTable = "CREATE TABLE messages (" +
                                             "id INT AUTO_INCREMENT PRIMARY KEY," +
                                             "sender VARCHAR(50) NOT NULL," +
                                             "message_text TEXT NOT NULL," +
                                             "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                                             "image_id INT NULL," +
                                             "FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE SET NULL" +
                                             ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;";
                stmt.execute(createMessagesTable);
            }

            isInitialized = true;
            LOGGER.info("Swing chat database schemas verified and initialized successfully in 'chat'.");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error initializing database schemas in 'chat'", e);
        }
    }
}
