package Model.DAO;

import Model.BEAN.users;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class CheckLoginDAO {
    
    public users getUserForLogin(String username) {
        String query = "SELECT UserID, Password FROM users WHERE Username = ?";
        try (Connection conn = ConnectDB.connectDatabase();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                users user = new users();
                user.setId(rs.getInt("UserID"));
                user.setPassword(rs.getString("Password")); 
                user.setUsername(username);
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; 
    }
}

