package com.chatapp.auth;

import com.chatapp.database.DatabaseManager;
import com.chatapp.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserAuthService {
    private DatabaseManager dbManager;

    public UserAuthService() {
        this.dbManager = DatabaseManager.getInstance();
    }

    // Register new member
    public boolean registerMember(String email, String password, String username) {
        return registerUser(email, password, username, "member");
    }

    // Register new admin
    public boolean registerAdmin(String email, String password, String username) {
        return registerUser(email, password, username, "admin");
    }

    private boolean registerUser(String email, String password, String username, String userType) {
        String tableName = userType.equals("admin") ? "admins" : "members";
        String sql = "INSERT INTO " + tableName + " (email, password, username, status) VALUES (?, ?, ?, 'offline')";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
            
            pstmt.setString(1, email);
            pstmt.setString(2, hashedPassword);
            pstmt.setString(3, username);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Login for admin
    public User loginAdmin(String email, String password) {
        return loginUser(email, password, "admin");
    }

    // Login for member
    public User loginMember(String email, String password) {
        return loginUser(email, password, "member");
    }

    private User loginUser(String email, String password, String userType) {
        String tableName = userType.equals("admin") ? "admins" : "members";
        String sql = "SELECT * FROM " + tableName + " WHERE email = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                String storedPassword = rs.getString("password");
                
                if (BCrypt.checkpw(password, storedPassword)) {
                    User user = new User();
                    user.setId(rs.getInt("id"));
                    user.setEmail(rs.getString("email"));
                    user.setUsername(rs.getString("username"));
                    user.setStatus(rs.getString("status"));
                    user.setUserType(userType);
                    
                    // Update status to online
                    updateUserStatus(email, userType, "online");
                    user.setStatus("online");
                    
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return null;
    }

    // Update user status
    public boolean updateUserStatus(String email, String userType, String status) {
        String tableName = userType.equals("admin") ? "admins" : "members";
        String sql = "UPDATE " + tableName + " SET status = ? WHERE email = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setString(2, email);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Get all members (for admin dashboard)
    public List<User> getAllMembers() {
        return getAllUsers("member");
    }

    // Get all admins (for member dashboard)
    public List<User> getAllAdmins() {
        return getAllUsers("admin");
    }

    private List<User> getAllUsers(String userType) {
        String tableName = userType.equals("admin") ? "admins" : "members";
        String sql = "SELECT id, email, username, status FROM " + tableName + " ORDER BY username";
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setUsername(rs.getString("username"));
                user.setStatus(rs.getString("status"));
                user.setUserType(userType);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    // Get online users
    public List<User> getOnlineUsers(String userType) {
        String tableName = userType.equals("admin") ? "admins" : "members";
        String sql = "SELECT id, email, username, status FROM " + tableName + 
                     " WHERE status = 'online' ORDER BY username";
        
        List<User> users = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setEmail(rs.getString("email"));
                user.setUsername(rs.getString("username"));
                user.setStatus(rs.getString("status"));
                user.setUserType(userType);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return users;
    }

    // Get a user by email (search both members and admins)
    public User getUserByEmail(String email) {
        try (Connection conn = dbManager.getConnection()) {
            String[] tables = {"members", "admins"};
            for (String table : tables) {
                String sql = "SELECT id, email, username, status FROM " + table + " WHERE email = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, email);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setEmail(rs.getString("email"));
                        user.setUsername(rs.getString("username"));
                        user.setStatus(rs.getString("status"));
                        user.setUserType(table.equals("admins") ? "admin" : "member");
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // Backwards-compatible helper used by server code that calls getOnlineUsers() with no args
    public List<User> getOnlineUsers() {
        // default to members
        return getOnlineUsers("member");
    }

    // Backwards-compatible register/login/update signatures
    public boolean register(String email, String password, String username) {
        return registerMember(email, password, username);
    }

    public User login(String email, String password) {
        // try member first, then admin
        User user = loginMember(email, password);
        if (user == null) {
            user = loginAdmin(email, password);
        }
        return user;
    }

    public boolean updateUserStatus(String email, String status) {
        // default to member type when userType not provided
        // Attempt update for members first, then admins
        boolean ok = updateUserStatus(email, "member", status);
        if (!ok) {
            ok = updateUserStatus(email, "admin", status);
        }
        return ok;
    }
}
