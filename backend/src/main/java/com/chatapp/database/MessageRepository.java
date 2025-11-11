package com.chatapp.database;

import com.chatapp.model.Message;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {
    private DatabaseManager dbManager;

    public MessageRepository() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public boolean saveMessage(Message message) {
        String sql = "INSERT INTO messages (sender_email, sender_type, receiver_email, " +
                     "receiver_type, message, timestamp, is_read) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, message.getSenderEmail());
            pstmt.setString(2, message.getSenderType());
            pstmt.setString(3, message.getReceiverEmail());
            pstmt.setString(4, message.getReceiverType());
            pstmt.setString(5, message.getMessage());
            pstmt.setTimestamp(6, Timestamp.valueOf(message.getTimestamp()));
            pstmt.setBoolean(7, message.isRead());
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Message> getConversation(String user1Email, String user1Type, 
                                        String user2Email, String user2Type) {
        String sql = "SELECT * FROM messages WHERE " +
                     "((sender_email = ? AND sender_type = ? AND receiver_email = ? AND receiver_type = ?) OR " +
                     "(sender_email = ? AND sender_type = ? AND receiver_email = ? AND receiver_type = ?)) " +
                     "ORDER BY timestamp ASC";
        
        List<Message> messages = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, user1Email);
            pstmt.setString(2, user1Type);
            pstmt.setString(3, user2Email);
            pstmt.setString(4, user2Type);
            pstmt.setString(5, user2Email);
            pstmt.setString(6, user2Type);
            pstmt.setString(7, user1Email);
            pstmt.setString(8, user1Type);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = new Message();
                message.setId(rs.getInt("id"));
                message.setSenderEmail(rs.getString("sender_email"));
                message.setSenderType(rs.getString("sender_type"));
                message.setReceiverEmail(rs.getString("receiver_email"));
                message.setReceiverType(rs.getString("receiver_type"));
                message.setMessage(rs.getString("message"));
                message.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                message.setRead(rs.getBoolean("is_read"));
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }

    public List<Message> getRecentMessages(String userEmail, String userType, int limit) {
        String sql = "SELECT * FROM messages WHERE " +
                     "(receiver_email = ? AND receiver_type = ?) OR " +
                     "(sender_email = ? AND sender_type = ?) " +
                     "ORDER BY timestamp DESC LIMIT ?";
        
        List<Message> messages = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userEmail);
            pstmt.setString(2, userType);
            pstmt.setString(3, userEmail);
            pstmt.setString(4, userType);
            pstmt.setInt(5, limit);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Message message = new Message();
                message.setId(rs.getInt("id"));
                message.setSenderEmail(rs.getString("sender_email"));
                message.setSenderType(rs.getString("sender_type"));
                message.setReceiverEmail(rs.getString("receiver_email"));
                message.setReceiverType(rs.getString("receiver_type"));
                message.setMessage(rs.getString("message"));
                message.setTimestamp(rs.getTimestamp("timestamp").toLocalDateTime());
                message.setRead(rs.getBoolean("is_read"));
                messages.add(message);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return messages;
    }

    public boolean markAsRead(int messageId) {
        String sql = "UPDATE messages SET is_read = TRUE WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, messageId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
