package Model.BO;

import Model.BEAN.information;
import Model.DAO.SaveInformationDAO;
import java.util.List;
import java.sql.Timestamp;

public class SaveInformationBO {
	SaveInformationDAO saveInformationDAO = new SaveInformationDAO();
	
	public int saveFileInformation(int userId,Timestamp currentTime,String fileName,String finalOutputDocx,String status) {
        return saveInformationDAO.saveFileHistory(userId,currentTime,fileName,finalOutputDocx,status);
    }
	
	public List<information> getFileHistory(int userId) {
        return saveInformationDAO.getFileHistory(userId);
    }
	
	public String getFilePath(int inforId) {
        return saveInformationDAO.getFilePath(inforId);
    }
	
	public boolean UpdateInformation(int InforID, String status) {
		return saveInformationDAO.UpdateInformation(InforID, status);
	}
}

