package Controller;

import Model.BO.SaveInformationBO;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ConversionTaskRunnable implements Runnable {
    private int userId;
    private File pdfFile;
    private String outputPath;
    private SaveInformationBO saveInformationBO;

    public ConversionTaskRunnable(int userId, File pdfFile, String outputPath) {
        this.userId = userId;
        this.pdfFile = pdfFile;
        this.outputPath = outputPath;
        this.saveInformationBO = new SaveInformationBO();
    }

    @Override
    public void run() {
        String finalOutputDocx = outputPath + File.separator + "output_" + pdfFile.getName().replace(".pdf", ".docx");
        Timestamp currentTime = Timestamp.valueOf(LocalDateTime.now());
        
        int inforID = saveInformationBO.saveFileInformation(userId, currentTime, pdfFile.getName(), finalOutputDocx, "Processing");
        String status = "Failed"; 
        try {
            PdfToDocxConverter.convertPdfToDocx(pdfFile.getAbsolutePath(), finalOutputDocx);
            status = "Success";
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            saveInformationBO.UpdateInformation(inforID, status);
            pdfFile.delete();
        }
    }
}