package Controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ThreadPoolListener implements ServletContextListener {
    private WorkStealingQueueManager workStealingQueueManager;
    private OptimizedConversionQueueManager optimizedQueueManager;
    private ConversionQueueManager queueManager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Khởi tạo WorkStealingQueueManager (version mới nhất)
        workStealingQueueManager = WorkStealingQueueManager.getInstance();
        sce.getServletContext().setAttribute("workStealingQueueManager", workStealingQueueManager);
        System.out.println(">>> Work Stealing Queue Manager STARTED <<<");
        System.out.println(">>> Features: Work Stealing, Rate Limiting, Load Balancing <<<");

        // Giữ lại các version cũ để fallback nếu cần
        optimizedQueueManager = OptimizedConversionQueueManager.getInstance();
        sce.getServletContext().setAttribute("optimizedConversionQueueManager", optimizedQueueManager);
        System.out.println(">>> Optimized Conversion Queue Manager STARTED (fallback) <<<");

        queueManager = ConversionQueueManager.getInstance();
        sce.getServletContext().setAttribute("conversionQueueManager", queueManager);
        System.out.println(">>> Legacy Conversion Queue Manager STARTED (fallback) <<<");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (workStealingQueueManager != null) {
            workStealingQueueManager.shutdown();
        }
        if (optimizedQueueManager != null) {
            optimizedQueueManager.shutdown();
        }
        if (queueManager != null) {
            queueManager.shutdown();
        }
    }
}