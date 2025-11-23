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
    private Timestamp uploadTime; // Thời gian upload (được set khi tạo task)

    public ConversionTaskRunnable(int userId, File pdfFile, String outputPath) {
        this.userId = userId;
        this.pdfFile = pdfFile;
        this.outputPath = outputPath;
        this.saveInformationBO = new SaveInformationBO();
        // Lưu thời gian upload ngay khi tạo task
        this.uploadTime = Timestamp.valueOf(LocalDateTime.now());
    }

    @Override
    public void run() {
        String finalOutputDocx = outputPath + File.separator + "output_" + pdfFile.getName().replace(".pdf", ".docx");
        // Sử dụng thời gian upload thay vì thời gian khi bắt đầu xử lý
        // Để đảm bảo thứ tự hiển thị đúng với thứ tự upload

        int inforID = saveInformationBO.saveFileInformation(userId, uploadTime, pdfFile.getName(), finalOutputDocx,
                "Processing");
        String status = "Failed";
        try {
            PdfToDocxConverter.convertPdfToDocx(pdfFile.getAbsolutePath(), finalOutputDocx);
            status = "Success";
        } catch (Exception e) {
            // Re-throw để retry mechanism có thể catch
            throw new RuntimeException("Conversion failed: " + e.getMessage(), e);
        } finally {
            saveInformationBO.UpdateInformation(inforID, status);
            // Chỉ xóa file nếu conversion thành công
            if ("Success".equals(status)) {
                pdfFile.delete();
            }
        }
    }

    public File getPdfFile() {
        return pdfFile;
    }
}