-- Create database
CREATE DATABASE IF NOT EXISTS chat_app;

USE chat_app;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    status ENUM('online', 'offline') DEFAULT 'offline',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    last_seen DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Messages table
CREATE TABLE IF NOT EXISTS messages (
    id INT PRIMARY KEY AUTO_INCREMENT,
    sender VARCHAR(255) NOT NULL,
    receiver VARCHAR(255) NULL,
    message TEXT NOT NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sender) REFERENCES users(email) ON DELETE CASCADE,
    FOREIGN KEY (receiver) REFERENCES users(email) ON DELETE CASCADE
);

-- Files table
CREATE TABLE IF NOT EXISTS files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    filename VARCHAR(255) NOT NULL,
    file_data LONGBLOB NOT NULL,
    file_type VARCHAR(100),
    sender VARCHAR(255) NOT NULL,
    receiver VARCHAR(255) NULL,
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender) REFERENCES users(email) ON DELETE CASCADE,
    FOREIGN KEY (receiver) REFERENCES users(email) ON DELETE CASCADE
);

-- Indexes for messages table
CREATE INDEX idx_messages_sender ON messages(sender);
CREATE INDEX idx_messages_receiver ON messages(receiver);
CREATE INDEX idx_messages_timestamp ON messages(timestamp);

-- Indexes for files table
CREATE INDEX idx_files_sender ON files(sender);
CREATE INDEX idx_files_receiver ON files(receiver);
CREATE INDEX idx_files_timestamp ON files(timestamp);
