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
 * API trả về trạng thái rate limit của user
 */
@WebServlet("/RateLimitStatusController")
public class RateLimitStatusController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("UserID") : null;
        
        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        UserRateLimiter rateLimiter = UserRateLimiter.getInstance();
        
        Map<String, Object> status = new HashMap<>();
        status.put("userId", userId);
        status.put("submittedTasks", rateLimiter.getSubmittedTasks(userId));
        status.put("remainingTasks", rateLimiter.getRemainingTasks(userId));
        status.put("maxTasksPerUser", 20);
        status.put("windowMinutes", 1);
        status.put("canSubmit", rateLimiter.canSubmitTask(userId));

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(status));
    }
}

