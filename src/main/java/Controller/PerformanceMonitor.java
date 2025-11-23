package Controller;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Monitor hiệu năng hệ thống để điều chỉnh thread pool động
 */
public class PerformanceMonitor {
    private static PerformanceMonitor instance;
    
    // Thống kê
    private AtomicLong totalProcessed = new AtomicLong(0);
    private AtomicLong totalSuccess = new AtomicLong(0);
    private AtomicLong totalFailed = new AtomicLong(0);
    private AtomicLong totalProcessingTime = new AtomicLong(0);
    private AtomicInteger currentQueueSize = new AtomicInteger(0);
    
    // Metrics cho dynamic thread pool
    private double cpuUsage = 0.0;
    
    private PerformanceMonitor() {}
    
    public static synchronized PerformanceMonitor getInstance() {
        if (instance == null) {
            instance = new PerformanceMonitor();
        }
        return instance;
    }
    
    public void recordTaskStart() {
        currentQueueSize.incrementAndGet();
    }
    
    public void recordTaskComplete(boolean success, long processingTimeMs) {
        currentQueueSize.decrementAndGet();
        totalProcessed.incrementAndGet();
        if (success) {
            totalSuccess.incrementAndGet();
        } else {
            totalFailed.incrementAndGet();
        }
        totalProcessingTime.addAndGet(processingTimeMs);
    }
    
    public void updateCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }
    
    public double getCpuUsage() {
        return cpuUsage;
    }
    
    public int getCurrentQueueSize() {
        return currentQueueSize.get();
    }
    
    public long getTotalProcessed() {
        return totalProcessed.get();
    }
    
    public long getTotalSuccess() {
        return totalSuccess.get();
    }
    
    public long getTotalFailed() {
        return totalFailed.get();
    }
    
    public double getSuccessRate() {
        long total = totalProcessed.get();
        if (total == 0) return 0.0;
        return (double) totalSuccess.get() / total * 100.0;
    }
    
    public double getAverageProcessingTime() {
        long total = totalProcessed.get();
        if (total == 0) return 0.0;
        return (double) totalProcessingTime.get() / total / 1000.0; // seconds
    }
    
    public String getStatistics() {
        return String.format(
            "Stats[processed=%d, success=%d, failed=%d, success_rate=%.2f%%, avg_time=%.2fs, queue=%d, cpu=%.1f%%]",
            totalProcessed.get(), totalSuccess.get(), totalFailed.get(),
            getSuccessRate(), getAverageProcessingTime(), currentQueueSize.get(), cpuUsage
        );
    }
}

