package com.chatapp.database;

import com.chatapp.model.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private final DatabaseManager dbManager;

    public MessageRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    /**
     * Save a message to the database
     */
    public boolean saveMessage(String sender, String receiver, String messageContent) {
        String sql = "INSERT INTO messages (sender, receiver, message) VALUES (?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            if (conn == null) {
                System.err.println("❌ Database connection is null!");
                return false;
            }

            System.out.println("💾 Attempting to save message: sender=" + sender + ", receiver=" + receiver + ", content=" + messageContent);

            pstmt.setString(1, sender);
            pstmt.setString(2, receiver); // null for public messages
            pstmt.setString(3, messageContent);

            int rowsAffected = pstmt.executeUpdate();
            
            if (rowsAffected > 0) {
                System.out.println("✅ Message saved successfully! Rows affected: " + rowsAffected);
                return true;
            } else {
                System.err.println("⚠️ No rows were inserted!");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("❌ Error saving message to database:");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get recent public messages (limit)
     */
    public List<Message> getRecentPublicMessages(int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username FROM messages m " +
                     "JOIN users u ON m.sender = u.email " +
                     "WHERE m.receiver IS NULL " +
                     "ORDER BY m.timestamp DESC LIMIT ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message msg = new Message();
                msg.setType("message");
                msg.setSender(rs.getString("sender"));
                msg.setUsername(rs.getString("username"));
                msg.setContent(rs.getString("message"));
                msg.setTimestamp(rs.getTimestamp("timestamp").getTime());
                messages.add(msg);
            }

            // Reverse to show oldest first
            java.util.Collections.reverse(messages);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Get private messages between two users
     */
    public List<Message> getPrivateMessages(String user1, String user2, int limit) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, u.username FROM messages m " +
                     "JOIN users u ON m.sender = u.email " +
                     "WHERE ((m.sender = ? AND m.receiver = ?) OR (m.sender = ? AND m.receiver = ?)) " +
                     "ORDER BY m.timestamp DESC LIMIT ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user1);
            pstmt.setString(2, user2);
            pstmt.setString(3, user2);
            pstmt.setString(4, user1);
            pstmt.setInt(5, limit);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Message msg = new Message();
                msg.setType("private_message");
                msg.setSender(rs.getString("sender"));
                msg.setReceiver(rs.getString("receiver"));
                msg.setUsername(rs.getString("username"));
                msg.setContent(rs.getString("message"));
                msg.setTimestamp(rs.getTimestamp("timestamp").getTime());
                messages.add(msg);
            }

            java.util.Collections.reverse(messages);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return messages;
    }

    /**
     * Mark messages as read
     */
    public boolean markAsRead(String receiver, String sender) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE receiver = ? AND sender = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, receiver);
            pstmt.setString(2, sender);

            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
