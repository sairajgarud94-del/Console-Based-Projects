
import java.sql.*;

public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:12345/chatdb";
    private static final String USER = "root"; 
    private static final String PASSWORD = ""; 

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static void saveMessage(String username, String message) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO messages (username, message) VALUES (?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, message);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("DB Error: " + e.getMessage());
        }
    }
}
