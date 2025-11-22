package Controller;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ThreadPoolListener implements ServletContextListener {
    private ConversionQueueManager queueManager;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // Khởi tạo ConversionQueueManager (singleton)
        queueManager = ConversionQueueManager.getInstance();
        sce.getServletContext().setAttribute("conversionQueueManager", queueManager);
        System.out.println(">>> Conversion Queue Manager STARTED <<<");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (queueManager != null) {
            queueManager.shutdown();
        }
    }
}