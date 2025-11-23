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

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String outputPath = "D:\\JAVA_2024\\Data";
        File outputDir = new File(outputPath);
        if (!outputDir.exists())
            outputDir.mkdirs();
        outputPath = outputPath.replace("\\", "/");

        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("UserID") : null;

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not logged in.");
            return;
        }

        // Sử dụng WorkStealingQueueManager (version mới nhất với Work Stealing, Rate
        // Limiting, Load Balancing)
        WorkStealingQueueManager queueManager = (WorkStealingQueueManager) getServletContext()
                .getAttribute("workStealingQueueManager");
        if (queueManager == null) {
            throw new ServletException("Work Stealing Queue Manager not initialized.");
        }

        // Xử lý upload nhiều file với rate limiting
        int successCount = 0;
        int failCount = 0;
        String errorMessage = null;
        UserRateLimiter rateLimiter = UserRateLimiter.getInstance();

        for (Part pdfPart : request.getParts()) {
            if (pdfPart.getName().equals("pdfFile") && pdfPart.getSize() > 0) {
                String pdfFileName = pdfPart.getSubmittedFileName();
                if (pdfFileName != null && !pdfFileName.isEmpty() && pdfFileName.toLowerCase().endsWith(".pdf")) {
                    // Kiểm tra rate limit trước khi xử lý file
                    if (!rateLimiter.canSubmitTask(userId)) {
                        int submitted = rateLimiter.getSubmittedTasks(userId);
                        errorMessage = "Bạn đã vượt quá giới hạn (" + submitted
                                + "/5 tasks/phút). Không thể xử lý thêm file nào.";
                        failCount++;
                        continue; // Bỏ qua file này, tiếp tục với file khác
                    }

                    String safeFileName = new File(pdfFileName).getName();
                    File uploadedFile = new File(outputPath + File.separator + safeFileName);

                    try {
                        pdfPart.write(uploadedFile.getAbsolutePath());
                        ConversionTaskRunnable task = new ConversionTaskRunnable(userId, uploadedFile, outputPath);

                        // Đẩy task vào Work Stealing Queue với Rate Limiting
                        boolean enqueued = queueManager.enqueueTask(task, uploadedFile, userId);
                        if (enqueued) {
                            successCount++;
                        } else {
                            // File đã được upload nhưng không thể enqueue (queue đầy hoặc file quá lớn)
                            uploadedFile.delete(); // Xóa file đã upload
                            failCount++;
                            if (errorMessage == null) {
                                errorMessage = "Một số file không thể được thêm vào hàng đợi (hàng đợi đầy hoặc file quá lớn >30MB).";
                            }
                        }
                    } catch (Exception e) {
                        failCount++;
                        if (uploadedFile.exists()) {
                            uploadedFile.delete(); // Xóa file nếu có lỗi
                        }
                        if (errorMessage == null) {
                            errorMessage = "Lỗi khi xử lý file: " + e.getMessage();
                        }
                    }
                } else {
                    failCount++;
                    if (errorMessage == null) {
                        errorMessage = "Một số file không phải định dạng PDF đã bị bỏ qua.";
                    }
                }
            }
        }

        // Xử lý response
        if (successCount == 0 && failCount > 0) {
            // Tất cả file đều thất bại
            if (errorMessage != null && errorMessage.contains("vượt quá giới hạn")) {
                response.setStatus(429);
                response.setContentType("text/plain; charset=UTF-8");
                response.getWriter().write(errorMessage);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        errorMessage != null ? errorMessage : "Không thể xử lý file nào.");
            }
        } else if (successCount > 0) {
            // Có ít nhất một file thành công
            if (failCount > 0) {
                // Một số file thành công, một số thất bại
                response.setContentType("text/html; charset=UTF-8");
                response.getWriter().write("<script>alert('Đã tải lên thành công " + successCount +
                        " file. " + failCount + " file không thể xử lý. " +
                        (errorMessage != null ? errorMessage : "")
                        + "'); window.location.href='HistoryController';</script>");
            } else {
                // Tất cả file đều thành công
                response.sendRedirect("HistoryController");
            }
        } else {
            // Không có file nào được chọn
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Không có file nào được chọn.");
        }
    }
}