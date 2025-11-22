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
 * API trả về trạng thái hàng đợi chuyển đổi
 * Giúp client biết có bao nhiêu task đang chờ xử lý
 */
@WebServlet("/QueueStatusController")
public class QueueStatusController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("UserID") : null;
        
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        ConversionQueueManager queueManager = (ConversionQueueManager) 
            getServletContext().getAttribute("conversionQueueManager");
        
        if (queueManager == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Queue Manager not available");
            return;
        }

        Map<String, Object> status = new HashMap<>();
        status.put("queueSize", queueManager.getQueueSize());
        status.put("message", "Số task đang chờ trong hàng đợi: " + queueManager.getQueueSize());

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(status));
    }
}

