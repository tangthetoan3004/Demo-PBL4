package Controller;

import com.google.gson.Gson;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * API trả về trạng thái hàng đợi tối ưu với thống kê chi tiết
 */
@WebServlet("/OptimizedQueueStatusController")
public class OptimizedQueueStatusController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("UserID") : null;
        
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        OptimizedConversionQueueManager queueManager = (OptimizedConversionQueueManager) 
            getServletContext().getAttribute("optimizedConversionQueueManager");
        
        if (queueManager == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Optimized Queue Manager not available");
            return;
        }

        PerformanceMonitor monitor = PerformanceMonitor.getInstance();
        
        Map<String, Object> status = new HashMap<>();
        status.put("queueSize", queueManager.getQueueSize());
        status.put("statistics", queueManager.getStatistics());
        status.put("totalProcessed", monitor.getTotalProcessed());
        status.put("totalSuccess", monitor.getTotalSuccess());
        status.put("totalFailed", monitor.getTotalFailed());
        status.put("successRate", monitor.getSuccessRate());
        status.put("averageProcessingTime", monitor.getAverageProcessingTime());
        status.put("cpuUsage", monitor.getCpuUsage());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(status));
    }
}

