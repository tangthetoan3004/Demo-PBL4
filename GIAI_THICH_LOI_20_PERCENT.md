# Giải Thích: Tại Sao Lỗi 20% Nhưng Có 8 File Lỗi Trong Database

## Phân Tích Kết Quả

### Từ JMeter Summary Report:
- **Upload PDF**: Error % = 20.00% (2/10 requests failed)
- **TOTAL**: Error % = 10.00% (2/20 total requests failed)

### Từ Database:
- Có **8 entries với Status = "Failed"**

## Nguyên Nhân Có Thể

### 1. **Requests Từ Các Test Trước Đó** ⚠️
- Database có thể chứa entries từ các test trước đó
- Các entries "Failed" từ test cũ vẫn còn trong database
- **Giải pháp**: Xóa dữ liệu test cũ hoặc filter theo thời gian cụ thể

### 2. **Exception Không Được Xử Lý Đúng** ✅ (ĐÃ FIX)
- **Vấn đề cũ**: Khi có exception xảy ra trong `WorkStealingQueueManager.compute()` hoặc `OptimizedConversionQueueManager`, entry đã được save vào DB nhưng status không được update
- **Hậu quả**: Entry bị bỏ lại với status "Processing" hoặc không được update → có thể bị hiển thị là "Failed" nếu có logic khác
- **Đã fix**: Thêm exception handling để đảm bảo status luôn được update ngay cả khi có exception

### 3. **Requests Thực Tế Khác Với JMeter Report**
- JMeter có thể đếm requests khác với thực tế
- Có thể có requests được gửi nhưng không được đếm trong JMeter
- Có thể có retry từ client side (browser, network)

### 4. **Race Condition Hoặc Concurrent Issues**
- Nhiều threads xử lý cùng lúc có thể tạo ra các entries không mong muốn
- Có thể có duplicate requests do network retry

## Code Fix Đã Áp Dụng

### 1. WorkStealingQueueManager.java
```java
// FIX: Đảm bảo update status nếu có exception xảy ra
catch (Exception e) {
    // ... logging ...
    if (task != null) {
        try {
            int inforID = task.getInforID();
            if (inforID > 0) {
                task.updateStatus("Failed");
            }
        } catch (Exception updateEx) {
            // Handle update exception
        }
    }
}
```

### 2. OptimizedConversionQueueManager.java
- Áp dụng fix tương tự để đảm bảo status luôn được update

## Cách Kiểm Tra

### 1. Kiểm Tra Database
```sql
-- Xem tất cả entries Failed trong khoảng thời gian test
SELECT * FROM information 
WHERE Status = 'Failed' 
AND DateConvert >= '2025-11-23 15:50:00'
AND DateConvert <= '2025-11-23 15:51:00'
ORDER BY DateConvert;
```

### 2. Kiểm Tra Logs
- Xem server logs để biết có bao nhiêu requests thực sự được xử lý
- Tìm các log: `[WORK STEALING]`, `[WORKER]`, `[QUEUE]`

### 3. So Sánh Với JMeter
- JMeter chỉ đếm HTTP responses
- Database đếm tất cả entries được tạo (kể cả từ retry, exception, etc.)

## Kết Luận

**Nguyên nhân chính**: Có thể là kết hợp của:
1. ✅ **Exception không được xử lý đúng** (ĐÃ FIX)
2. ⚠️ **Entries từ test trước đó** (Cần xóa dữ liệu test cũ)
3. ⚠️ **Requests thực tế khác với JMeter report** (Cần kiểm tra logs)

**Sau khi fix**: Hệ thống sẽ đảm bảo:
- Mỗi request chỉ tạo 1 entry trong database
- Status luôn được update đúng (Success/Failed)
- Không còn entries bị bỏ lại với status "Processing"

## Khuyến Nghị

1. **Xóa dữ liệu test cũ** trước khi test mới
2. **Kiểm tra logs** để xác nhận số requests thực tế
3. **Test lại** với fix mới để xác nhận vấn đề đã được giải quyết

