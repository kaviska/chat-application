import java.sql.*;

public class TestDbConnection {
    public static void main(String[] args) {
        String url = "jdbc:mysql://localhost:3306/chat_app?useSSL=false&serverTimezone=UTC";
        String username = "root";
        String password = "KaviskaDilshan12#$";
        
        try {
            // Test connection
            Class.forName("com.mysql.cj.jdbc.Driver");
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("✅ Database connection successful!");
            
            // Count messages
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM messages");
            if (rs.next()) {
                System.out.println("📊 Total messages in database: " + rs.getInt("count"));
            }
            
            // Show recent messages
            rs = stmt.executeQuery("SELECT * FROM messages ORDER BY timestamp DESC LIMIT 5");
            System.out.println("\n📝 Recent messages:");
            while (rs.next()) {
                System.out.println("  - From: " + rs.getString("sender") + 
                                   ", To: " + rs.getString("receiver") +
                                   ", Message: " + rs.getString("message") +
                                   ", Time: " + rs.getTimestamp("timestamp"));
            }
            
            conn.close();
            
        } catch (Exception e) {
            System.err.println("❌ Database error:");
            e.printStackTrace();
        }
    }
}
