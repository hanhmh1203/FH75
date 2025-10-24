# Tính năng Bluetooth Scan Logging - Demo Guide

## 🔥 **Tính năng mới đã thêm:**

### **📡 Bluetooth Device Scan Logging**
Hệ thống sẽ tự động ghi log chi tiết tất cả thiết bị Bluetooth được tìm thấy khi scan.

## 🚀 **Cách test:**

### **1. Mở app FH75Demo**
- App sẽ tự động ghi device info khi khởi động
- Log file được tạo tại: `/sdcard/Android/data/com.vanch.vhxdemo/files/FH75_Logs/`

### **2. Vào tab "Link"**
- Tab đầu tiên của app

### **3. Nhấn nút "Scan" để tìm thiết bị Bluetooth**
- Hệ thống sẽ log:
  - ✅ Bắt đầu scan
  - ✅ Paired devices (thiết bị đã kết nối trước đó)
  - ✅ New devices discovered (thiết bị mới tìm thấy)
  - ✅ Kết thúc scan với tổng số devices

### **4. Chọn device để kết nối**
- Log sẽ ghi lại device nào user chọn

## 📋 **Log format mẫu:**

### **Scan Started:**
```
[INFO] Bluetooth scan started - searching for devices...
[INFO] Found 2 paired devices
```

### **Paired Devices:**
```
[PAIRED_DEVICE] #1 - Name: FH75-Reader | Address: 00:11:22:33:44:55 | Type: Classic | Status: Already Paired
[PAIRED_DEVICE] #2 - Name: AirPods Pro | Address: AA:BB:CC:DD:EE:FF | Type: Dual Mode | Status: Already Paired
```

### **New Devices Discovered:**
```
[BLUETOOTH_DISCOVERY] New device discovered:
  - Name: Unknown Device
  - Address: 12:34:56:78:90:AB
  - Type: Low Energy
  - Bond State: Not Paired
  - RSSI: BluetoothClass{mClass=0x1f00}
```

### **Scan Completed:**
```
[INFO] Bluetooth scan completed - found 4 devices
[DEVICE_FOUND] #1 - Name: FH75-Reader | Address: 00:11:22:33:44:55 | Type: Classic | Bond: Paired
[DEVICE_FOUND] #2 - Name: AirPods Pro | Address: AA:BB:CC:DD:EE:FF | Type: Dual Mode | Bond: Paired
[DEVICE_FOUND] #3 - Name: Unknown Device | Address: 12:34:56:78:90:AB | Type: Low Energy | Bond: Not Paired
[DEVICE_FOUND] #4 - Name: Samsung TV | Address: 98:76:54:32:10:FE | Type: Classic | Bond: Not Paired
```

### **User Action:**
```
[USER_ACTION] User selected device to connect: FH75-Reader (00:11:22:33:44:55)
```

## 🔍 **Kiểm tra log file:**

### **Android Terminal:**
```bash
adb shell cat /sdcard/Android/data/com.vanch.vhxdemo/files/FH75_Logs/FH75_Session_*.txt
```

### **File Explorer:**
Navigate to: `/Android/data/com.vanch.vhxdemo/files/FH75_Logs/`

## 📊 **Thông tin được log:**

### **Device Information:**
- **Name**: Tên thiết bị (hoặc "Unknown Device")
- **Address**: MAC address
- **Type**: Classic/Low Energy/Dual Mode/Unknown
- **Bond State**: Paired/Pairing/Not Paired
- **Discovery Status**: Paired device vs New discovery

### **User Actions:**
- Device selection cho kết nối
- Auto-reconnect attempts
- Scan start/stop events

## 💡 **Lưu ý:**

1. **Permissions**: App cần Bluetooth permissions để scan
2. **Real-time**: Log được ghi real-time khi scan
3. **Persistent**: Log được lưu vĩnh viễn trong file
4. **Categorized**: Mỗi loại event có category riêng
5. **Timestamped**: Tất cả log đều có timestamp chính xác

## 🎯 **Mục đích:**

Giúp debug và support:
- Thiết bị nào được tìm thấy
- Tại sao không kết nối được
- User đã chọn device nào
- Bluetooth environment của user như thế nào

Với tính năng này, bạn có thể dễ dàng hỗ trợ khách hàng khi có vấn đề về Bluetooth connectivity! 🚀