package Controller;

import java.io.File;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

@WebServlet("/PdfController")
@MultipartConfig(fileSizeThreshold = 1024 * 1024 * 2, maxFileSize = 1024 * 1024 * 30, maxRequestSize = 1024 * 1024 * 50)
public class PdfController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    	String outputPath = "D:\\JAVA_2024\\Data"; 
        File outputDir = new File(outputPath);
        if (!outputDir.exists()) outputDir.mkdirs();
        outputPath = outputPath.replace("\\", "/");

        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("UserID") : null;

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        }

        ConversionQueueManager queueManager = (ConversionQueueManager) getServletContext().getAttribute("conversionQueueManager");
        if (queueManager == null) throw new ServletException("Conversion Queue Manager not initialized.");

        for (Part pdfPart : request.getParts()) {
            if (pdfPart.getName().equals("pdfFile") && pdfPart.getSize() > 0) {
                String pdfFileName = pdfPart.getSubmittedFileName();
                if (pdfFileName != null && !pdfFileName.isEmpty() && pdfFileName.toLowerCase().endsWith(".pdf")) {
                    String safeFileName = new File(pdfFileName).getName();
                    File uploadedFile = new File(outputPath + File.separator + safeFileName);
                    pdfPart.write(uploadedFile.getAbsolutePath());

                    ConversionTaskRunnable task = new ConversionTaskRunnable(userId, uploadedFile, outputPath);
                    // Đẩy task vào HÀNG ĐỢI để xử lý
                    boolean enqueued = queueManager.enqueueTask(task);
                    if (!enqueued) {
                        response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Hàng đợi đầy, vui lòng thử lại sau.");
                        return;
                    }
                }
            }
        }
        response.sendRedirect("HistoryController");
    }
}