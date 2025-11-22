package Model.BEAN;

import java.sql.Timestamp;

public class information {
    private int inforID;
    private int userID;
    private Timestamp dateConvert;
    private String fileName;
    private String FilePath;
    private String status;
    
    public information() {}

    public information(int InforID, int UserID, Timestamp DateConvert, String FileName, String FilePath, String Status) {
    	this.inforID = InforID;
        this.userID = UserID;
        this.dateConvert = DateConvert;
        this.fileName = FileName;
        this.FilePath = FilePath;
        this.status = Status;
    }

    public int getInforID() { return inforID; }
    public void setInforID(int InforID) { this.inforID = InforID; }
    public int getUserID() { return userID; }
    public void setUserID(int UserID) { this.userID = UserID; }
    public Timestamp getDateConvert() { return dateConvert; }
    public void setDateConvert(Timestamp DateConvert) { this.dateConvert = DateConvert; }
    public String getFileName() { return fileName; }
    public void setFileName(String FileName) { this.fileName = FileName; }
    public String getFilePath() { return FilePath; }
    public void setFilePath(String FilePath) { this.FilePath = FilePath; }
    public String getStatus() { return status; }
    public void setStatus(String Status) { this.status = Status; }
    
    @Override
    public String toString() {
        return "Task[id=" + inforID + ", user=" + userID + ", file=" + fileName + "]";
    }
}

