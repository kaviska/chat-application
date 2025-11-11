-- Create database
CREATE DATABASE IF NOT EXISTS chat_app;
USE chat_app;

-- Admin table
CREATE TABLE IF NOT EXISTS admins (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    status ENUM('online', 'offline') DEFAULT 'offline',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Member table
CREATE TABLE IF NOT EXISTS members (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    status ENUM('online', 'offline') DEFAULT 'offline',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Messages table (stores all messages between admin and members)
CREATE TABLE IF NOT EXISTS messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender_email VARCHAR(255) NOT NULL,
    sender_type ENUM('admin', 'member') NOT NULL,
    receiver_email VARCHAR(255) NOT NULL,
    receiver_type ENUM('admin', 'member') NOT NULL,
    message TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    INDEX idx_sender (sender_email, sender_type),
    INDEX idx_receiver (receiver_email, receiver_type),
    INDEX idx_timestamp (timestamp)
);

-- Insert default admin for testing
INSERT INTO admins (email, password, username, status) 
VALUES ('admin@chat.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Admin User', 'offline')
ON DUPLICATE KEY UPDATE email=email;
-- Password is 'admin123' (BCrypt hashed)

-- Insert sample member for testing
INSERT INTO members (email, password, username, status) 
VALUES ('member@chat.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Member User', 'offline')
ON DUPLICATE KEY UPDATE email=email;
-- Password is 'admin123' (BCrypt hashed)
