package Controller;

import Model.BO.SaveInformationBO;
import java.io.File;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class ConversionTaskRunnable implements Runnable {
    private int userId;
    private File pdfFile;
    @SuppressWarnings("unused")
    private String outputPath; // Used in constructor to set finalOutputDocx
    private SaveInformationBO saveInformationBO;
    private Timestamp uploadTime; // Thời gian upload (được set khi tạo task)
    private Integer inforID; // Lưu ID để update status sau retry
    private String finalOutputDocx; // Lưu output path

    public ConversionTaskRunnable(int userId, File pdfFile, String outputPath) {
        this.userId = userId;
        this.pdfFile = pdfFile;
        this.outputPath = outputPath;
        this.saveInformationBO = new SaveInformationBO();
        // Lưu thời gian upload ngay khi tạo task
        this.uploadTime = Timestamp.valueOf(LocalDateTime.now());
        this.finalOutputDocx = outputPath + File.separator + "output_" + pdfFile.getName().replace(".pdf", ".docx");
    }

    /**
     * Lưu thông tin file vào database TRƯỚC KHI bắt đầu conversion
     * Chỉ gọi 1 lần, không gọi trong retry loop
     */
    public int saveToDatabase() {
        if (inforID == null) {
            inforID = saveInformationBO.saveFileInformation(userId, uploadTime, pdfFile.getName(), finalOutputDocx,
                    "Processing");
        }
        return inforID;
    }

    /**
     * Chỉ thực hiện conversion, không save database
     * Được gọi trong retry loop
     */
    public void executeConversion() throws Exception {
        PdfToDocxConverter.convertPdfToDocx(pdfFile.getAbsolutePath(), finalOutputDocx);
    }

    /**
     * Update status trong database
     */
    public void updateStatus(String status) {
        if (inforID != null && inforID > 0) {
            saveInformationBO.UpdateInformation(inforID, status);
        }
    }

    /**
     * Xóa file PDF sau khi conversion thành công
     */
    public void cleanupOnSuccess() {
        if (pdfFile.exists()) {
            pdfFile.delete();
        }
    }

    /**
     * Phương thức run() cũ - giữ lại để tương thích
     * NHƯNG không nên dùng trong retry loop
     */
    @Override
    public void run() {
        // Lưu database entry (chỉ 1 lần)
        if (inforID == null) {
            inforID = saveToDatabase();
        }

        String status = "Failed";
        try {
            executeConversion();
            status = "Success";
        } catch (Exception e) {
            // Re-throw để retry mechanism có thể catch
            throw new RuntimeException("Conversion failed: " + e.getMessage(), e);
        } finally {
            updateStatus(status);
            // Chỉ xóa file nếu conversion thành công
            if ("Success".equals(status)) {
                cleanupOnSuccess();
            }
        }
    }

    public File getPdfFile() {
        return pdfFile;
    }

    public int getInforID() {
        return inforID != null ? inforID : -1;
    }
}