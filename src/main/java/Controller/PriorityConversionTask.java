package Controller;

/**
 * Task với priority để sử dụng trong PriorityQueue
 * File nhỏ hơn = priority cao hơn (số nhỏ hơn)
 */
public class PriorityConversionTask implements Comparable<PriorityConversionTask> {
    private ConversionTaskRunnable task;
    private int priority; // Số càng nhỏ = priority càng cao
    private long fileSize;
    private long enqueueTime;

    public PriorityConversionTask(ConversionTaskRunnable task, long fileSize) {
        this.task = task;
        this.fileSize = fileSize;
        this.enqueueTime = System.currentTimeMillis();
        
        // Tính priority: file nhỏ hơn = priority cao hơn
        // File < 5MB: priority 1-3
        // File 5-15MB: priority 4-6
        // File > 15MB: priority 7-10
        if (fileSize < 5 * 1024 * 1024) {
            this.priority = 1 + (int)(fileSize / (1024 * 1024)); // 1-4
        } else if (fileSize < 15 * 1024 * 1024) {
            this.priority = 5 + (int)((fileSize - 5 * 1024 * 1024) / (2 * 1024 * 1024)); // 5-9
        } else {
            this.priority = 10; // File lớn nhất
        }
    }

    public ConversionTaskRunnable getTask() {
        return task;
    }

    public int getPriority() {
        return priority;
    }

    public long getFileSize() {
        return fileSize;
    }

    public long getEnqueueTime() {
        return enqueueTime;
    }

    public long getWaitingTime() {
        return System.currentTimeMillis() - enqueueTime;
    }

    @Override
    public int compareTo(PriorityConversionTask other) {
        // So sánh priority (số nhỏ hơn = ưu tiên cao hơn)
        int priorityCompare = Integer.compare(this.priority, other.priority);
        if (priorityCompare != 0) {
            return priorityCompare;
        }
        // Nếu cùng priority, ưu tiên file đã chờ lâu hơn (FIFO)
        return Long.compare(this.enqueueTime, other.enqueueTime);
    }

    @Override
    public String toString() {
        return String.format("PriorityTask[priority=%d, size=%.2fMB, wait=%dms]", 
            priority, fileSize / (1024.0 * 1024.0), getWaitingTime());
    }
}

