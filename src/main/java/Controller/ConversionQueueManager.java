package Controller;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Quản lý hàng đợi chuyển đổi PDF -> DOCX
 * Đảm bảo rõ ràng về việc sử dụng hàng đợi (Queue) để xử lý tác vụ lớn
 */
public class ConversionQueueManager {
    private static ConversionQueueManager instance;
    private BlockingQueue<ConversionTaskRunnable> taskQueue;
    private ExecutorService executorService;
    private static final int THREAD_POOL_SIZE = 4;
    private static final int MAX_QUEUE_SIZE = 100;

    private ConversionQueueManager() {
        // Khởi tạo hàng đợi với giới hạn
        this.taskQueue = new LinkedBlockingQueue<>(MAX_QUEUE_SIZE);
        this.executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        
        // Khởi động các worker thread để xử lý task từ hàng đợi
        startWorkerThreads();
    }

    public static synchronized ConversionQueueManager getInstance() {
        if (instance == null) {
            instance = new ConversionQueueManager();
        }
        return instance;
    }

    /**
     * Thêm task vào hàng đợi để xử lý
     * @param task Task cần xử lý
     * @return true nếu thêm thành công, false nếu hàng đợi đầy
     */
    public boolean enqueueTask(ConversionTaskRunnable task) {
        try {
            // Thêm vào hàng đợi (blocking nếu hàng đợi đầy)
            taskQueue.put(task);
            System.out.println("[QUEUE] Task đã được thêm vào hàng đợi. Số task đang chờ: " + taskQueue.size());
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("[QUEUE] Lỗi khi thêm task vào hàng đợi: " + e.getMessage());
            return false;
        }
    }

    /**
     * Khởi động các worker thread để xử lý task từ hàng đợi
     */
    private void startWorkerThreads() {
        for (int i = 0; i < THREAD_POOL_SIZE; i++) {
            executorService.submit(new WorkerThread(i + 1));
        }
        System.out.println("[QUEUE] Đã khởi động " + THREAD_POOL_SIZE + " worker thread để xử lý hàng đợi");
    }

    /**
     * Worker thread xử lý task từ hàng đợi
     */
    private class WorkerThread implements Runnable {
        private final int workerId;

        public WorkerThread(int workerId) {
            this.workerId = workerId;
        }

        @Override
        public void run() {
            System.out.println("[WORKER " + workerId + "] Đã sẵn sàng xử lý task từ hàng đợi");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // Lấy task từ hàng đợi (blocking nếu hàng đợi rỗng)
                    ConversionTaskRunnable task = taskQueue.take();
                    System.out.println("[WORKER " + workerId + "] Đang xử lý task. Số task còn lại: " + taskQueue.size());
                    
                    // Thực thi task
                    task.run();
                    
                    System.out.println("[WORKER " + workerId + "] Hoàn thành task");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("[WORKER " + workerId + "] Đã dừng");
                    break;
                } catch (Exception e) {
                    System.err.println("[WORKER " + workerId + "] Lỗi khi xử lý task: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Lấy số lượng task đang chờ trong hàng đợi
     */
    public int getQueueSize() {
        return taskQueue.size();
    }

    /**
     * Dừng tất cả worker và xóa hàng đợi
     */
    public void shutdown() {
        executorService.shutdownNow();
        taskQueue.clear();
        System.out.println("[QUEUE] Đã dừng tất cả worker và xóa hàng đợi");
    }
}

