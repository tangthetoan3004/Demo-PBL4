package Controller;

import Model.BO.SaveInformationBO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession; 
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder; // Import thêm cái này

@WebServlet("/DownloadController")
public class DownloadController extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private SaveInformationBO saveInformationBO;
    
    @Override
    public void init() throws ServletException {
        saveInformationBO = new SaveInformationBO();
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("UserID") == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        
        String inforIdParam = request.getParameter("inforId");
        if (inforIdParam == null || inforIdParam.isEmpty()) return;

        try {
            int inforId = Integer.parseInt(inforIdParam);
            String filePath = saveInformationBO.getFilePath(inforId);

            if (filePath != null) {
                File downloadFile = new File(filePath);
                if (downloadFile.exists()) {
                    response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                    response.setContentLength((int) downloadFile.length());
                    
                    // --- XỬ LÝ TÊN FILE ---
                    String cleanFileName = downloadFile.getName();
                    if (cleanFileName.startsWith("output_")) {
                        cleanFileName = cleanFileName.substring(7);
                    }

                    // QUAN TRỌNG: Mã hóa tên file tiếng Việt để trình duyệt không bị lỗi
                    String encodedFileName = URLEncoder.encode(cleanFileName, "UTF-8").replace("+", "%20");
                    
                    // Cấu hình Header chuẩn cho mọi trình duyệt
                    response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName);
                    
                    // --- KẾT THÚC XỬ LÝ ---

                    try (FileInputStream inStream = new FileInputStream(downloadFile);
                         OutputStream outStream = response.getOutputStream()) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = inStream.read(buffer)) != -1) {
                            outStream.write(buffer, 0, bytesRead);
                        }
                    }
                } else {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "File không tồn tại.");
                }
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }
}