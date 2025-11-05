package com.chatapp.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public class DatabaseManager {
    private static DatabaseManager instance;
    private String url;
    private String username;
    private String password;

    private DatabaseManager() {
        loadDatabaseConfig();
        testInitialConnection();
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    private void loadDatabaseConfig() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("db_config.properties")) {
            if (input == null) {
                System.err.println("Unable to find db_config.properties");
                return;
            }
            props.load(input);
            this.url = props.getProperty("db.url");
            this.username = props.getProperty("db.username");
            this.password = props.getProperty("db.password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void testInitialConnection() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Test the connection and immediately close it
            try (Connection testConn = DriverManager.getConnection(url, username, password)) {
                System.out.println("✅ Database connected successfully!");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("❌ Database connection failed!");
            e.printStackTrace();
        }
    }

    /**
     * Get a new database connection.
     * IMPORTANT: The caller is responsible for closing this connection!
     * Use try-with-resources: try (Connection conn = dbManager.getConnection()) { ... }
     */
    public Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            conn.setAutoCommit(true); // Ensure auto-commit is enabled
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Driver not found", e);
        }
    }

    /**
     * Test if we can establish a database connection
     */
    public boolean testConnection() {
        try (Connection testConn = getConnection()) {
            return testConn != null && !testConn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
}
