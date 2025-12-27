# Performance Monitoring

## Tổng quan

Hệ thống Performance Monitoring đã được tích hợp vào các điểm quan trọng sau khi refactor để đo lường hiệu suất.

## Các điểm đã được đo lường

1. **MessageAdapter.setMessages()** - Đo thời gian update messages trong adapter
2. **MessageAdapter.DiffUtil.calculateDiff()** - Đo thời gian tính toán DiffUtil
3. **ChatViewModel.mergeMessages()** - Đo thời gian merge messages
4. **ChatViewModel.mergeMessages.sort()** - Đo thời gian sort messages
5. **ChatViewModel.addMessage()** - Đo thời gian thêm message mới
6. **ChatLocalRepository.saveMessages()** - Đo thời gian lưu messages vào database

## Cách xem kết quả

### 1. Xem trong Logcat

Trong DEBUG build, các measurements sẽ tự động log:
- Operations > 100ms: Log level WARN (⚠️)
- Operations > 50ms: Log level DEBUG (⏱️)

Filter logcat với tag: `PerformanceMonitor`

### 2. Xem Summary

Gọi trong code để xem tổng hợp:
```kotlin
PerformanceMonitor.printSummary()
```

### 3. Lấy thời gian trung bình

```kotlin
val avgTime = PerformanceMonitor.getAverageTime("ChatViewModel.mergeMessages")
```

### 4. So sánh Before/After

Để so sánh trước và sau refactor, sử dụng `PerformanceComparison`:

```kotlin
// Trước refactor
PerformanceComparison.recordBefore("operation_name", timeMs)

// Sau refactor  
PerformanceComparison.recordAfter("operation_name", timeMs)

// In báo cáo
PerformanceComparison.printComparisonReport()
```

## Ví dụ Output

```
PerformanceMonitor: ⏱️ ChatViewModel.mergeMessages took 45ms
PerformanceMonitor: ⏱️ MessageAdapter.DiffUtil.calculateDiff took 32ms
PerformanceMonitor: ⚠️ Slow operation: ChatLocalRepository.saveMessages took 150ms

=== Performance Summary ===
ChatViewModel.mergeMessages: avg=42.5ms, min=35ms, max=58ms, count=10
MessageAdapter.DiffUtil.calculateDiff: avg=28.3ms, min=22ms, max=35ms, count=10
ChatLocalRepository.saveMessages: avg=145.2ms, min=120ms, max=180ms, count=5
========================
```

## Lưu ý

- Performance monitoring chỉ hoạt động trong DEBUG builds
- Trong production builds, không có overhead từ monitoring
- Measurements được giới hạn 100 lần đo gần nhất cho mỗi operation

