package com.chatapp.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Simple database connection tester
 * Run this to test if your MySQL connection works
 */
public class DatabaseConnectionTester {
    
    public static void main(String[] args) {
        System.out.println("╔════════════════════════════════════════╗");
        System.out.println("║      Database Connection Tester        ║");
        System.out.println("╚════════════════════════════════════════╝");
        System.out.println();
        
        try {
            // Load database configuration
            Properties props = new Properties();
            InputStream input = DatabaseConnectionTester.class
                .getClassLoader().getResourceAsStream("db_config.properties");
            
            if (input == null) {
                System.err.println("❌ Cannot find db_config.properties file!");
                return;
            }
            
            props.load(input);
            
            String url = props.getProperty("db.url");
            String username = props.getProperty("db.username");
            String password = props.getProperty("db.password");
            
            System.out.println("📋 Testing connection with:");
            System.out.println("   URL: " + url);
            System.out.println("   Username: " + username);
            System.out.println("   Password: " + (password.isEmpty() ? "(empty)" : "***"));
            System.out.println();
            
            // Test connection
            System.out.println("🔍 Attempting to connect...");
            
            try (Connection conn = DriverManager.getConnection(url, username, password)) {
                if (conn != null && !conn.isClosed()) {
                    System.out.println("✅ SUCCESS! Database connection works perfectly!");
                    System.out.println("📊 Database: " + conn.getCatalog());
                    System.out.println("🔗 Connection URL: " + conn.getMetaData().getURL());
                    System.out.println();
                    System.out.println("🎉 Your chat server should now work!");
                    System.out.println("💡 Try running: .\\start-java-only.ps1");
                } else {
                    System.err.println("❌ Connection is null or closed");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("❌ Database connection failed!");
            System.err.println("📋 Error details:");
            
            if (e.getMessage().contains("Access denied")) {
                System.err.println("   🔐 Access denied - Check username/password");
                System.err.println("   💡 Solutions:");
                System.err.println("      - Make sure XAMPP MySQL is started");
                System.err.println("      - Check if root password is set in XAMPP");
                System.err.println("      - Try empty password (default XAMPP)");
            } else if (e.getMessage().contains("Connection refused") || e.getMessage().contains("Communications link failure")) {
                System.err.println("   🔌 Connection refused - MySQL server not running");
                System.err.println("   💡 Solutions:");
                System.err.println("      - Start XAMPP and click 'Start' for MySQL");
                System.err.println("      - Check if MySQL is running on port 3306");
                System.err.println("      - Try port 3307 if 3306 doesn't work");
            } else if (e.getMessage().contains("Unknown database")) {
                System.err.println("   📊 Database 'chat_app' doesn't exist");
                System.err.println("   💡 Solutions:");
                System.err.println("      - Open phpMyAdmin (http://localhost/phpmyadmin)");
                System.err.println("      - Create database named 'chat_app'");
                System.err.println("      - Import schema.sql file");
            } else {
                System.err.println("   ❓ Other error: " + e.getMessage());
            }
            
            System.err.println();
            System.err.println("📖 Full error: " + e.getMessage());
            
        } catch (IOException e) {
            System.err.println("❌ Failed to load configuration: " + e.getMessage());
        }
    }
}