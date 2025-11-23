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

@WebServlet("/WorkStealingStatusController")
public class WorkStealingStatusController extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Integer userId = (session != null) ? (Integer) session.getAttribute("UserID") : null;

        if (userId == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        WorkStealingQueueManager queueManager = (WorkStealingQueueManager) getServletContext()
                .getAttribute("workStealingQueueManager");

        if (queueManager == null) {
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Work Stealing Queue Manager not available");
            return;
        }

        PerformanceMonitor monitor = PerformanceMonitor.getInstance();

        Map<String, Object> status = new HashMap<>();
        status.put("queueSize", queueManager.getQueueSize());
        status.put("activeThreads", queueManager.getActiveThreads());
        status.put("statistics", queueManager.getStatistics());
        status.put("totalProcessed", monitor.getTotalProcessed());
        status.put("totalSuccess", monitor.getTotalSuccess());
        status.put("totalFailed", monitor.getTotalFailed());
        status.put("successRate", monitor.getSuccessRate());
        status.put("averageProcessingTime", monitor.getAverageProcessingTime());

        Map<Long, Integer> loadDistribution = queueManager.getThreadLoadDistribution();
        status.put("threadLoadDistribution", loadDistribution);

        if (!loadDistribution.isEmpty()) {
            int totalTasks = loadDistribution.values().stream().mapToInt(Integer::intValue).sum();
            double avgLoad = (double) totalTasks / loadDistribution.size();
            double variance = loadDistribution.values().stream()
                    .mapToDouble(count -> Math.pow(count - avgLoad, 2))
                    .average()
                    .orElse(0.0);
            double stdDev = Math.sqrt(variance);

            status.put("loadBalanceAverage", avgLoad);
            status.put("loadBalanceStdDev", stdDev);
            status.put("loadBalanceVariance", variance);
            status.put("isBalanced", stdDev < 2.0);
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(new Gson().toJson(status));
    }
}
