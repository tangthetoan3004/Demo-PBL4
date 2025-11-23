package Controller;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;

/**
 * Conversion Queue Manager với các thuật toán tối ưu:
 * 1. Priority Queue (ưu tiên file nhỏ)
 * 2. Dynamic Thread Pool (tự động điều chỉnh số thread)
 * 3. Performance Monitoring
 * 4. Retry Mechanism
 */
public class OptimizedConversionQueueManager {
    private static OptimizedConversionQueueManager instance;
    private PriorityBlockingQueue<PriorityConversionTask> taskQueue;
    private ThreadPoolExecutor executorService;
    private PerformanceMonitor monitor;
    private ScheduledExecutorService scheduler;
    
    // Cấu hình
    private static final int MIN_THREAD_POOL_SIZE = 4;
    private static final int MAX_THREAD_POOL_SIZE = 16;
    private static final int INITIAL_THREAD_POOL_SIZE = 6;
    private static final int MAX_QUEUE_SIZE = 500; // Tăng từ 100 lên 500
    private static final long THREAD_KEEP_ALIVE_TIME = 60L; // seconds
    
    // Dynamic thread pool parameters
    private static final int QUEUE_THRESHOLD_HIGH = (int)(MAX_QUEUE_SIZE * 0.7); // 70% queue
    private static final int QUEUE_THRESHOLD_LOW = (int)(MAX_QUEUE_SIZE * 0.2); // 20% queue
    private static final double CPU_THRESHOLD_HIGH = 90.0; // 90% CPU
    private static final double CPU_THRESHOLD_LOW = 70.0; // 70% CPU
    
    private AtomicInteger currentThreadCount = new AtomicInteger(INITIAL_THREAD_POOL_SIZE);

    private OptimizedConversionQueueManager() {
        // Khởi tạo Priority Queue (file nhỏ = priority cao)
        this.taskQueue = new PriorityBlockingQueue<>(MAX_QUEUE_SIZE);
        
        // Khởi tạo Thread Pool với core và max threads
        this.executorService = new ThreadPoolExecutor(
            INITIAL_THREAD_POOL_SIZE,
            MAX_THREAD_POOL_SIZE,
            THREAD_KEEP_ALIVE_TIME,
            TimeUnit.SECONDS,
            new java.util.concurrent.LinkedBlockingQueue<>() // Internal queue cho thread pool
        );
        
        this.monitor = PerformanceMonitor.getInstance();
        
        // Scheduler để điều chỉnh thread pool động
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::adjustThreadPool, 10, 10, TimeUnit.SECONDS);
        
        // Khởi động các worker thread
        startWorkerThreads();
        
        // Monitor CPU usage (đơn giản, có thể cải thiện với JMX)
        scheduler.scheduleAtFixedRate(this::updateCpuUsage, 5, 5, TimeUnit.SECONDS);
        
        System.out.println("[OPTIMIZED QUEUE] Đã khởi động với Priority Queue và Dynamic Thread Pool");
        System.out.println("[OPTIMIZED QUEUE] Thread Pool: " + INITIAL_THREAD_POOL_SIZE + "-" + MAX_THREAD_POOL_SIZE);
        System.out.println("[OPTIMIZED QUEUE] Max Queue Size: " + MAX_QUEUE_SIZE);
    }

    public static synchronized OptimizedConversionQueueManager getInstance() {
        if (instance == null) {
            instance = new OptimizedConversionQueueManager();
        }
        return instance;
    }

    /**
     * Thêm task vào hàng đợi ưu tiên
     */
    public boolean enqueueTask(ConversionTaskRunnable task, File pdfFile) {
        try {
            long fileSize = pdfFile.length();
            
            // Kiểm tra file size (tăng giới hạn lên 100MB)
            if (fileSize > 100 * 1024 * 1024) {
                System.err.println("[QUEUE] File quá lớn (>100MB): " + pdfFile.getName());
                return false;
            }
            
            // Tạo priority task
            PriorityConversionTask priorityTask = new PriorityConversionTask(task, fileSize);
            
            // Thêm vào priority queue
            if (taskQueue.offer(priorityTask)) {
                monitor.recordTaskStart();
                System.out.println("[QUEUE] Task đã được thêm vào hàng đợi ưu tiên. " + 
                    priorityTask + " | Queue size: " + taskQueue.size());
                return true;
            } else {
                System.err.println("[QUEUE] Hàng đợi đầy, không thể thêm task");
                return false;
            }
        } catch (Exception e) {
            System.err.println("[QUEUE] Lỗi khi thêm task vào hàng đợi: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Khởi động các worker thread
     */
    private void startWorkerThreads() {
        for (int i = 0; i < INITIAL_THREAD_POOL_SIZE; i++) {
            executorService.submit(new OptimizedWorkerThread(i + 1));
        }
        System.out.println("[OPTIMIZED QUEUE] Đã khởi động " + INITIAL_THREAD_POOL_SIZE + " worker thread");
    }

    /**
     * Worker thread với retry mechanism
     */
    private class OptimizedWorkerThread implements Runnable {
        private final int workerId;

        public OptimizedWorkerThread(int workerId) {
            this.workerId = workerId;
        }

        @Override
        public void run() {
            System.out.println("[WORKER " + workerId + "] Đã sẵn sàng xử lý task từ priority queue");
            while (!Thread.currentThread().isInterrupted()) {
                ConversionTaskRunnable task = null;
                try {
                    // Lấy task từ priority queue (file nhỏ nhất trước)
                    PriorityConversionTask priorityTask = taskQueue.take();
                    task = priorityTask.getTask();
                    
                    System.out.println("[WORKER " + workerId + "] Đang xử lý: " + priorityTask + 
                        " (đã chờ " + priorityTask.getWaitingTime() + "ms) | Queue còn lại: " + taskQueue.size());
                    
                    // Thực thi với retry mechanism
                    long startTime = System.currentTimeMillis();
                    boolean success = executeWithRetry(task, 3); // Tối đa 3 lần retry
                    long processingTime = System.currentTimeMillis() - startTime;
                    
                    // Ghi lại metrics
                    monitor.recordTaskComplete(success, processingTime);
                    
                    System.out.println("[WORKER " + workerId + "] Hoàn thành. " + 
                        (success ? "Success" : "Failed") + " | Time: " + processingTime + "ms");
                    
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("[WORKER " + workerId + "] Đã dừng");
                    break;
                } catch (Exception e) {
                    System.err.println("[WORKER " + workerId + "] Lỗi: " + e.getMessage());
                    e.printStackTrace();
                    
                    // FIX: Đảm bảo update status nếu có exception xảy ra
                    // (trường hợp executeWithRetry throw exception trước khi vào retry loop)
                    if (task != null) {
                        try {
                            int inforID = task.getInforID();
                            if (inforID > 0) {
                                // Nếu đã có entry trong DB, update status = Failed
                                task.updateStatus("Failed");
                                System.err.println("[WORKER " + workerId + "] Đã update status Failed cho task ID: " + inforID);
                            }
                        } catch (Exception updateEx) {
                            System.err.println("[WORKER " + workerId + "] Không thể update status: " + updateEx.getMessage());
                        }
                    }
                }
            }
        }
        
        /**
         * Thực thi task với retry mechanism (exponential backoff)
         * FIX: Chỉ save database 1 lần, không tạo duplicate entries khi retry
         */
        private boolean executeWithRetry(ConversionTaskRunnable task, int maxRetries) {
            // FIX: Save database entry TRƯỚC retry loop (chỉ 1 lần)
            int inforID = task.saveToDatabase();
            if (inforID <= 0) {
                System.err.println("[WORKER " + workerId + "] Không thể lưu thông tin vào database");
                return false;
            }

            // Retry loop: chỉ thực hiện conversion, không save database nữa
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    // Chỉ thực hiện conversion, không gọi task.run() (vì run() sẽ save DB lại)
                    task.executeConversion();
                    
                    // Conversion thành công
                    task.updateStatus("Success");
                    task.cleanupOnSuccess();
                    return true; // Success
                    
                } catch (Exception e) {
                    if (attempt < maxRetries) {
                        // Exponential backoff: 1s, 2s, 4s
                        long backoffTime = (long) Math.pow(2, attempt - 1) * 1000;
                        System.out.println("[WORKER " + workerId + "] Retry attempt " + 
                            attempt + "/" + maxRetries + " sau " + backoffTime + "ms | Error: " + e.getMessage());
                        try {
                            Thread.sleep(backoffTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            task.updateStatus("Failed");
                            return false;
                        }
                    } else {
                        // Đã hết số lần retry
                        System.err.println("[WORKER " + workerId + "] Đã hết số lần retry cho task ID: " + inforID);
                        e.printStackTrace();
                        task.updateStatus("Failed");
                    }
                }
            }
            return false;
        }
    }

    /**
     * Điều chỉnh thread pool động dựa trên queue size và CPU usage
     */
    private void adjustThreadPool() {
        int queueSize = taskQueue.size();
        int poolSize = executorService.getPoolSize();
        double cpuUsage = monitor.getCpuUsage();
        
        // Tăng thread nếu queue cao và CPU thấp
        if (queueSize > QUEUE_THRESHOLD_HIGH && cpuUsage < CPU_THRESHOLD_LOW && poolSize < MAX_THREAD_POOL_SIZE) {
            int newSize = Math.min(poolSize + 2, MAX_THREAD_POOL_SIZE);
            executorService.setCorePoolSize(newSize);
            executorService.setMaximumPoolSize(newSize);
            currentThreadCount.set(newSize);
            System.out.println("[DYNAMIC POOL] Tăng thread pool lên " + newSize + 
                " (queue=" + queueSize + ", cpu=" + String.format("%.1f", cpuUsage) + "%)");
        }
        // Giảm thread nếu queue thấp và CPU cao
        else if (queueSize < QUEUE_THRESHOLD_LOW && cpuUsage > CPU_THRESHOLD_HIGH && poolSize > MIN_THREAD_POOL_SIZE) {
            int newSize = Math.max(poolSize - 1, MIN_THREAD_POOL_SIZE);
            executorService.setCorePoolSize(newSize);
            executorService.setMaximumPoolSize(newSize);
            currentThreadCount.set(newSize);
            System.out.println("[DYNAMIC POOL] Giảm thread pool xuống " + newSize + 
                " (queue=" + queueSize + ", cpu=" + String.format("%.1f", cpuUsage) + "%)");
        }
    }

    /**
     * Cập nhật CPU usage (ước tính dựa trên active threads)
     * Có thể cải thiện với JMX hoặc thư viện bên thứ 3
     */
    private void updateCpuUsage() {
        try {
            // Ước tính CPU usage dựa trên active threads và queue size
            int activeThreads = executorService.getActiveCount();
            int queueSize = taskQueue.size();
            
            // CPU usage ước tính: active threads × 15% + queue pressure
            double estimatedCpu = Math.min(activeThreads * 15.0 + (queueSize * 0.1), 100.0);
            monitor.updateCpuUsage(estimatedCpu);
        } catch (Exception e) {
            // Fallback: ước tính cơ bản
            int activeThreads = executorService.getActiveCount();
            double estimatedCpu = Math.min(activeThreads * 15.0, 100.0);
            monitor.updateCpuUsage(estimatedCpu);
        }
    }

    /**
     * Lấy số lượng task đang chờ trong hàng đợi
     */
    public int getQueueSize() {
        return taskQueue.size();
    }

    /**
     * Lấy thống kê hiệu năng
     */
    public String getStatistics() {
        return monitor.getStatistics() + 
            " | Threads: " + executorService.getPoolSize() + "/" + MAX_THREAD_POOL_SIZE +
            " | Active: " + executorService.getActiveCount();
    }

    /**
     * Dừng tất cả worker và xóa hàng đợi
     */
    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (taskQueue != null) {
            taskQueue.clear();
        }
        System.out.println("[OPTIMIZED QUEUE] Đã dừng tất cả worker và xóa hàng đợi");
    }
}

