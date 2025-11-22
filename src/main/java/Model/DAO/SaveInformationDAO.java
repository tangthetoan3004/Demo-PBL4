package Model.DAO;

import Model.BEAN.information;
import java.sql.Connection;
import java.sql.Timestamp;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class SaveInformationDAO {
	public int saveFileHistory(int userId, Timestamp currentTime, String fileName, String finalOutputDocx, String status) {
	    String query = "INSERT INTO information (UserID, DateConvert, FileName, FilePath, Status) VALUES (?, ?, ?, ?, ?)";
	    try (Connection conn = ConnectDB.connectDatabase();
	         PreparedStatement pstmt = conn.prepareStatement(query, PreparedStatement.RETURN_GENERATED_KEYS)) {

	        pstmt.setInt(1, userId);
	        pstmt.setTimestamp(2, currentTime);
	        pstmt.setString(3, fileName);
	        pstmt.setString(4, finalOutputDocx);
	        pstmt.setString(5, status);

	        int result = pstmt.executeUpdate();
	        if (result > 0) {
	            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
	                if (generatedKeys.next()) return generatedKeys.getInt(1);
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
        return -1; 
	}
	
	 public List<information> getFileHistory(int userId) {
        List<information> historyList = new ArrayList<>();
        try (Connection connection = ConnectDB.connectDatabase()) {
            String query = "SELECT * FROM information WHERE UserID = ? ORDER BY DateConvert DESC";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                information info = new information(
                	resultSet.getInt("InforID"),
                    resultSet.getInt("UserID"),
                    resultSet.getTimestamp("DateConvert"),
                    resultSet.getString("FileName"),
                    resultSet.getString("FilePath"),
                    resultSet.getString("Status")
                );
                historyList.add(info);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return historyList;
    }
	 
	 public String getFilePath(int inforId) {
        try (Connection connection = ConnectDB.connectDatabase()) {
            String query = "SELECT FilePath FROM information WHERE InforID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, inforId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) return resultSet.getString("FilePath");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
	 
	 public boolean UpdateInformation(int InforID, String Status) {
        try (Connection connection = ConnectDB.connectDatabase()) {
            String query = "Update information set Status = ?  WHERE InforID = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, Status);
            statement.setInt(2, InforID);
            return statement.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
	 }
}

