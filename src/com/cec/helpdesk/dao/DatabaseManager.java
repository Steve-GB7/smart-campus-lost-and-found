package com.cec.helpdesk.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DESIGN PATTERN — SINGLETON
 *
 * Thread-safe, lazy-initialized Singleton that manages the JDBC connection
 * to the MySQL database.  Only one instance exists throughout the application
 * lifecycle, preventing connection leaks and ensuring centralized config.
 */
public class DatabaseManager {

    // ── Singleton instance (volatile for thread safety) ─────
    private static volatile DatabaseManager instance;

    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/cec_helpdesk"
                                                 + "?useSSL=false&serverTimezone=Asia/Kolkata"
                                                 + "&allowPublicKeyRetrieval=true";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    private static String getEnvOrProp(String propKey, String envKey, String defaultVal) {
        String val = System.getProperty(propKey);
        if (val != null && !val.trim().isEmpty()) return val;
        val = System.getenv(envKey);
        if (val != null && !val.trim().isEmpty()) return val;
        return defaultVal;
    }

    private static final String URL      = getEnvOrProp("db.url", "DB_URL", DEFAULT_URL);
    private static final String USER     = getEnvOrProp("db.user", "DB_USER", DEFAULT_USER);
    private static final String PASSWORD = getEnvOrProp("db.password", "DB_PASSWORD", DEFAULT_PASSWORD);

    private Connection connection;

    // ── private constructor (prevents external instantiation) ─
    private DatabaseManager() { }

    /**
     * Returns the single DatabaseManager instance (double-checked locking).
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (DatabaseManager.class) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }

    /**
     * Returns an active JDBC connection, creating one if necessary.
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("MySQL JDBC driver not found", e);
            }
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
        return connection;
    }

    /**
     * Closes the active connection if one exists.
     */
    public void closeConnection() {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }
}
