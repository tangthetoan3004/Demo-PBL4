package Controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Rate Limiter để giới hạn số lượng task của mỗi user
 * Tránh spam và đảm bảo công bằng cho tất cả users
 */
public class UserRateLimiter {
    private static UserRateLimiter instance;

    // Cấu hình rate limiting
    private static final int MAX_TASKS_PER_USER = 5; // 5 tasks/user/phút
    private static final long RATE_LIMIT_WINDOW_MS = 60 * 1000; // 1 phút

    // Map lưu số task của mỗi user trong time window
    private ConcurrentHashMap<Integer, UserRateLimit> userLimits = new ConcurrentHashMap<>();

    private UserRateLimiter() {
    }

    public static synchronized UserRateLimiter getInstance() {
        if (instance == null) {
            instance = new UserRateLimiter();
        }
        return instance;
    }

    /**
     * Kiểm tra xem user có thể submit thêm task không
     * 
     * @param userId User ID
     * @return true nếu có thể submit, false nếu đã vượt quá limit
     */
    public boolean canSubmitTask(int userId) {
        UserRateLimit limit = userLimits.computeIfAbsent(userId, k -> new UserRateLimit());
        return limit.canSubmit();
    }

    /**
     * Ghi nhận user đã submit 1 task
     */
    public void recordTaskSubmission(int userId) {
        UserRateLimit limit = userLimits.computeIfAbsent(userId, k -> new UserRateLimit());
        limit.recordSubmission();
    }

    /**
     * Lấy số task còn lại của user trong window hiện tại
     */
    public int getRemainingTasks(int userId) {
        UserRateLimit limit = userLimits.get(userId);
        if (limit == null)
            return MAX_TASKS_PER_USER;
        return limit.getRemaining();
    }

    /**
     * Lấy số task đã submit của user trong window hiện tại
     */
    public int getSubmittedTasks(int userId) {
        UserRateLimit limit = userLimits.get(userId);
        if (limit == null)
            return 0;
        return limit.getCurrentCount();
    }

    /**
     * Class lưu thông tin rate limit của 1 user
     */
    private static class UserRateLimit {
        private AtomicInteger taskCount = new AtomicInteger(0);
        private AtomicLong windowStart = new AtomicLong(System.currentTimeMillis());

        public boolean canSubmit() {
            long now = System.currentTimeMillis();
            long start = windowStart.get();

            // Nếu đã hết window, reset
            if (now - start > RATE_LIMIT_WINDOW_MS) {
                if (windowStart.compareAndSet(start, now)) {
                    taskCount.set(0);
                }
            }

            return taskCount.get() < MAX_TASKS_PER_USER;
        }

        public void recordSubmission() {
            long now = System.currentTimeMillis();
            long start = windowStart.get();

            // Reset nếu đã hết window
            if (now - start > RATE_LIMIT_WINDOW_MS) {
                if (windowStart.compareAndSet(start, now)) {
                    taskCount.set(0);
                }
            }

            taskCount.incrementAndGet();
        }

        public int getCurrentCount() {
            long now = System.currentTimeMillis();
            long start = windowStart.get();

            // Reset nếu đã hết window
            if (now - start > RATE_LIMIT_WINDOW_MS) {
                if (windowStart.compareAndSet(start, now)) {
                    taskCount.set(0);
                }
            }

            return taskCount.get();
        }

        public int getRemaining() {
            return Math.max(0, MAX_TASKS_PER_USER - getCurrentCount());
        }
    }

    /**
     * Cleanup các user đã hết hạn (không submit task trong 5 phút)
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        userLimits.entrySet().removeIf(entry -> {
            long start = entry.getValue().windowStart.get();
            return (now - start) > (5 * 60 * 1000); // 5 phút
        });
    }
}
