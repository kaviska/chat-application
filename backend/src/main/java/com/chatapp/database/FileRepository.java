package com.chatapp.database;

import com.chatapp.model.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileRepository {
    private final DatabaseManager databaseManager;

    public FileRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public void saveFile(File file) throws SQLException {
        String sql = "INSERT INTO files (filename, file_data, file_type, sender, receiver) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = databaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, file.getFilename());
            pstmt.setBytes(2, file.getFileData());
            pstmt.setString(3, file.getFileType());
            pstmt.setString(4, file.getSender());
            pstmt.setString(5, file.getReceiver());

            pstmt.executeUpdate();
        }
    }

    public List<File> getFilesForUser(String email) throws SQLException {
        String sql = "SELECT * FROM files WHERE sender = ? OR receiver = ? OR receiver IS NULL ORDER BY timestamp DESC";
        List<File> files = new ArrayList<>();

        try (Connection conn = databaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, email);
            pstmt.setString(2, email);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    File file = new File();
                    file.setId(rs.getInt("id"));
                    file.setFilename(rs.getString("filename"));
                    file.setFileData(rs.getBytes("file_data"));
                    file.setFileType(rs.getString("file_type"));
                    file.setSender(rs.getString("sender"));
                    file.setReceiver(rs.getString("receiver"));
                    file.setTimestamp(rs.getTimestamp("timestamp"));
                    files.add(file);
                }
            }
        }
        return files;
    }
}