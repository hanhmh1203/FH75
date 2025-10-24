# Hệ thống Logging FH75 - Hướng dẫn sử dụng

## Tổng quan
Hệ thống logging đã được tích hợp vào FH75Demo để ghi lại tất cả hoạt động kết nối và truyền dữ liệu với thiết bị FH75. Hệ thống sẽ tự động tạo file log cho từng phiên làm việc.

## Tính năng chính

### 1. LogManager Class
- **Singleton pattern**: Đảm bảo chỉ có một instance duy nhất
- **Session-based logging**: Mỗi phiên làm việc có file log riêng
- **Timestamp**: Mỗi log entry có timestamp chính xác
- **Categorized logging**: Các loại log khác nhau (INFO, ERROR, BLUETOOTH, DATA, RFID)

### 2. Tích hợp với VH73Device
- **Automatic logging**: Tự động log khi kết nối/ngắt kết nối
- **Data transmission**: Ghi lại tất cả dữ liệu gửi/nhận
- **Error tracking**: Ghi lại các lỗi xảy ra trong quá trình communication
- **Device identification**: Tự động nhận diện loại thiết bị FH75/VH75
- **Detailed connection info**: Ghi chi tiết thông tin kết nối Bluetooth

### 3. System Information Logging
- **Device details**: Model, brand, manufacturer, Android version, API level
- **App information**: Version, package name, install time, target SDK
- **Session tracking**: Timestamp, session ID, log file location
- **Automatic startup logging**: Tự động ghi thông tin khi khởi động app

### 3. File Management
- **Location**: `/Android/data/com.vanch.vhxdemo/files/FH75_Logs/`
- **Naming**: `FH75_Log_YYYYMMDD_HHMMSS.txt`
- **Auto cleanup**: Tự động xóa log cũ hơn số lượng cho phép
- **Device info header**: Mỗi session bắt đầu với thông tin thiết bị và app
- **Structured format**: Log được phân loại rõ ràng theo [CATEGORY]

## Cấu trúc File Log

### Format log entry:
```
===============================================================================
FH75 HANDHELD READER - LOG SESSION START
Session Started: 2024-01-15 14:30:23.456
Log File: FH75_Log_20240115_143023.txt
===============================================================================
[DEVICE_INFO] === DEVICE INFORMATION ===
[DEVICE_INFO] Device Model: SM-G991B
[DEVICE_INFO] Device Brand: samsung
[DEVICE_INFO] Device Manufacturer: samsung
[DEVICE_INFO] Android Version: 13
[DEVICE_INFO] API Level: 33
[APP_INFO] === APPLICATION INFORMATION ===
[APP_INFO] App Name: FH75Demo
[APP_INFO] Package Name: com.vanch.vhxdemo
[APP_INFO] Version Name: 1.0.18
[APP_INFO] Version Code: 18
[APP_STARTUP] === APP STARTUP ===
[APP_STARTUP] App Started At: 2024-01-15 14:30:23.789
===============================================================================
Ready for FH75 communication logging...
===============================================================================
[INFO] VH73Device created for: FH75-Reader (00:11:22:33:44:55)
[BLUETOOTH_CONNECT] === BLUETOOTH CONNECTION ===
[BLUETOOTH_CONNECT] Status: SUCCESS
[BLUETOOTH_CONNECT] Device Name: FH75-Reader
[BLUETOOTH_CONNECT] Device Address: 00:11:22:33:44:55
[BLUETOOTH_CONNECT] Connection Time: 2024-01-15 14:30:26.456
[DEVICE_TYPE] Connected Device Type: FH75/VH75 Handheld Reader
[DEVICE_TYPE] Device Category: RFID Handheld Scanner
[DEVICE_TYPE] Protocol: Bluetooth RFCOMM
[DATA_SENT] F0 03 01 02 03 | Command sent to FH75
[DATA_RECEIVED] F4 05 00 01 02 03 04 | Response received from FH75
```

## Cách sử dụng

### 1. Automatic Logging (Tự động)
Khi sử dụng VH73Device, logging sẽ tự động hoạt động:
```java
VH73Device device = new VH73Device(activity, bluetoothDevice);
device.connect(); // Tự động log kết nối
device.sendCommand(command); // Tự động log dữ liệu gửi
byte[] result = device.getCmdResult(); // Tự động log dữ liệu nhận
device.disconnect(); // Tự động log ngắt kết nối
```

### 2. Manual Logging (Thủ công)
Có thể thêm log tùy chỉnh:
```java
LogManager logManager = LogManager.getInstance(this);

// Bắt đầu session mới
logManager.startNewSession();

// Log thông tin
logManager.logInfo("Custom information");

// Log lỗi
logManager.logError("Error message", exception);

// Log RFID scan
logManager.logRFIDScan("E2801160600002045CB7A965", "Tag scanned");

// Kết thúc session
logManager.endSession();
```

### 3. Accessing Log Files
```java
LogManager logManager = LogManager.getInstance(this);
File logDir = logManager.getLogDirectory();
File currentLogFile = logManager.getCurrentLogFile();

// List all log files
File[] logFiles = logDir.listFiles();
```

## Testing
Sử dụng TestLogManager activity để kiểm tra:
```java
Intent intent = new Intent(this, TestLogManager.class);
startActivity(intent);
```

## Permissions Required
Đã thêm vào AndroidManifest.xml:
- `WRITE_EXTERNAL_STORAGE`
- `READ_EXTERNAL_STORAGE` 
- `MANAGE_EXTERNAL_STORAGE` (Android 11+)

## Lưu ý quan trọng

### 1. File Location
- Log files được lưu trong app-specific external storage
- Không cần request runtime permissions cho Android 10+
- Files sẽ bị xóa khi uninstall app

### 2. Performance
- Logging được thực hiện bất đồng bộ để không ảnh hưởng performance
- File buffer được flush tự động
- Maximum file size được kiểm soát

### 3. Privacy
- Không log sensitive data như passwords
- Chỉ log protocol data và connection info
- Files chỉ accessible bởi app

## Troubleshooting

### 1. Log files không được tạo
- Kiểm tra permissions trong Settings > Apps > FH75Demo > Permissions
- Đảm bảo external storage available
- Check Android version và permission model

### 2. Log files quá lớn
- Hệ thống tự động cleanup sau 30 ngày
- Có thể manually clean bằng cách gọi `cleanOldLogs()`

### 3. Performance issues
- Log writing được optimize với BufferedWriter
- Nếu vẫn chậm, có thể điều chỉnh buffer size trong LogManager

## Session Management

### Automatic Session
- Session tự động bắt đầu khi tạo VH73Device
- Session kết thúc khi app đóng hoặc device disconnect

### Manual Session Control
```java
logManager.startNewSession(); // Force start new session
logManager.endSession();      // Force end current session
```

## File Export

Để gửi log files cho support:
1. Navigate to `/Android/data/com.vanch.vhxdemo/files/FH75_Logs/`
2. Copy file log cần thiết
3. Attach vào email hoặc upload lên cloud storage

## Future Enhancements
- Remote logging via network
- Log compression
- Real-time log viewer
- Log analysis tools