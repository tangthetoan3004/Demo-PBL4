package Controller;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;

/**
 * Queue Manager với Work Stealing Algorithm
 * Sử dụng ForkJoinPool để tự động work stealing giữa các thread
 * Kết hợp với Rate Limiting và Load Balancing
 */
public class WorkStealingQueueManager {
    private static WorkStealingQueueManager instance;

    private ForkJoinPool forkJoinPool;

    private java.util.concurrent.PriorityBlockingQueue<PriorityConversionTask> taskQueue;

    private UserRateLimiter rateLimiter;

    private PerformanceMonitor monitor;

    private java.util.concurrent.ConcurrentHashMap<Long, AtomicInteger> threadTaskCount = new java.util.concurrent.ConcurrentHashMap<>();

    private static final int PARALLELISM = Runtime.getRuntime().availableProcessors() * 2;
    private static final int MAX_QUEUE_SIZE = 2000;
    private static final int MAX_TASKS_PER_USER = 5;

    private java.util.concurrent.ScheduledExecutorService scheduler;

    private WorkStealingQueueManager() {
        this.forkJoinPool = new ForkJoinPool(
                PARALLELISM,
                ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                null,
                true);

        this.taskQueue = new java.util.concurrent.PriorityBlockingQueue<>(MAX_QUEUE_SIZE);
        this.rateLimiter = UserRateLimiter.getInstance();

        this.monitor = PerformanceMonitor.getInstance();

        this.scheduler = java.util.concurrent.Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> rateLimiter.cleanup(), 5, 5, java.util.concurrent.TimeUnit.MINUTES);

        startWorkStealingWorkers();

        System.out.println("[WORK STEALING QUEUE] Đã khởi động với " + PARALLELISM + " threads");
        System.out.println("[WORK STEALING QUEUE] Max Queue Size: " + MAX_QUEUE_SIZE);
        System.out.println("[WORK STEALING QUEUE] Rate Limit: " + MAX_TASKS_PER_USER + " tasks/user/phút");
    }

    public static synchronized WorkStealingQueueManager getInstance() {
        if (instance == null) {
            instance = new WorkStealingQueueManager();
        }
        return instance;
    }

    public boolean enqueueTask(ConversionTaskRunnable task, File pdfFile, int userId) {
        // Rate limiting check
        if (!rateLimiter.canSubmitTask(userId)) {
            System.err.println("[RATE LIMIT] User " + userId + " đã vượt quá limit (" +
                    rateLimiter.getSubmittedTasks(userId) + "/" + MAX_TASKS_PER_USER + " tasks/phút)");
            return false;
        }

        long fileSize = pdfFile.length();
        if (fileSize > 30 * 1024 * 1024) {
            System.err.println("[QUEUE] File quá lớn (>30MB): " + pdfFile.getName());
            return false;
        }

        if (taskQueue.size() >= MAX_QUEUE_SIZE) {
            System.err.println("[QUEUE] Hàng đợi đầy (" + taskQueue.size() + "/" + MAX_QUEUE_SIZE + ")");
            return false;
        }

        PriorityConversionTask priorityTask = new PriorityConversionTask(task, fileSize);

        if (taskQueue.offer(priorityTask)) {
            rateLimiter.recordTaskSubmission(userId);
            monitor.recordTaskStart();

            System.out.println("[QUEUE] Task đã được thêm. " + priorityTask +
                    " | Queue: " + taskQueue.size() + "/" + MAX_QUEUE_SIZE +
                    " | User " + userId + ": " + rateLimiter.getRemainingTasks(userId) + " tasks còn lại");

            forkJoinPool.submit(new WorkStealingTask(priorityTask));

            return true;
        }

        return false;
    }

    private void startWorkStealingWorkers() {
        System.out.println("[WORK STEALING] ForkJoinPool đã sẵn sàng với " + PARALLELISM + " threads");
    }

    private class WorkStealingTask extends RecursiveAction {
        private PriorityConversionTask priorityTask;

        public WorkStealingTask(PriorityConversionTask priorityTask) {
            this.priorityTask = priorityTask;
        }

        @Override
        protected void compute() {
            String threadName = Thread.currentThread().getName();
            long threadId = threadName.hashCode();

            threadTaskCount.computeIfAbsent(threadId, k -> new AtomicInteger(0)).incrementAndGet();

            try {
                ConversionTaskRunnable task = priorityTask.getTask();

                System.out.println("[WORK STEALING] Thread " + threadId +
                        " đang xử lý: " + priorityTask +
                        " | Queue: " + taskQueue.size() +
                        " | Thread tasks: " + threadTaskCount.get(threadId).get());

                long startTime = System.currentTimeMillis();

                boolean success = executeWithRetry(task, 3);

                long processingTime = System.currentTimeMillis() - startTime;

                monitor.recordTaskComplete(success, processingTime);

                System.out.println("[WORK STEALING] Thread " + threadId +
                        " hoàn thành. " + (success ? "Success" : "Failed") +
                        " | Time: " + processingTime + "ms");

            } catch (Exception e) {
                System.err.println("[WORK STEALING] Thread " + threadId + " lỗi: " + e.getMessage());
                e.printStackTrace();
                monitor.recordTaskComplete(false, 0);
            } finally {
                // Giảm task count
                AtomicInteger count = threadTaskCount.get(threadId);
                if (count != null) {
                    count.decrementAndGet();
                }

                stealWork(threadId);
            }
        }

        private void stealWork(long threadId) {
            PriorityConversionTask nextTask = taskQueue.poll();
            if (nextTask != null) {
                // Submit task mới để xử lý
                forkJoinPool.submit(new WorkStealingTask(nextTask));
                System.out.println("[WORK STEALING] Thread " + threadId + " đã steal task từ queue");
            }
        }

        private boolean executeWithRetry(ConversionTaskRunnable task, int maxRetries) {
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    task.run();
                    return true;
                } catch (Exception e) {
                    if (attempt < maxRetries) {
                        long backoffTime = (long) Math.pow(2, attempt - 1) * 1000;
                        System.out.println("[WORK STEALING] Retry " + attempt + "/" + maxRetries +
                                " sau " + backoffTime + "ms");
                        try {
                            Thread.sleep(backoffTime);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    } else {
                        System.err.println("[WORK STEALING] Đã hết số lần retry");
                        e.printStackTrace();
                    }
                }
            }
            return false;
        }
    }

    public java.util.Map<Long, Integer> getThreadLoadDistribution() {
        java.util.Map<Long, Integer> distribution = new java.util.HashMap<>();
        threadTaskCount.forEach((threadId, count) -> {
            distribution.put(threadId, count.get());
        });
        return distribution;
    }

    public int getQueueSize() {
        return taskQueue.size();
    }

    public int getActiveThreads() {
        return forkJoinPool.getActiveThreadCount();
    }

    public String getStatistics() {
        java.util.Map<Long, Integer> loadDist = getThreadLoadDistribution();
        int totalThreadTasks = loadDist.values().stream().mapToInt(Integer::intValue).sum();

        return monitor.getStatistics() +
                " | Threads: " + getActiveThreads() + "/" + PARALLELISM +
                " | Queue: " + taskQueue.size() + "/" + MAX_QUEUE_SIZE +
                " | Thread Load: " + totalThreadTasks;
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        if (forkJoinPool != null) {
            forkJoinPool.shutdownNow();
        }
        if (taskQueue != null) {
            taskQueue.clear();
        }
        System.out.println("[WORK STEALING QUEUE] Đã dừng tất cả worker và xóa hàng đợi");
    }
}
