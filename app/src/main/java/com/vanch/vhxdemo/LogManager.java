package com.vanch.vhxdemo;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * LogManager for FH75 Device Communication
 * Comprehensive logging system with device info tracking
 */
public class LogManager {
    private static final String TAG = "LogManager";
    private static LogManager instance;
    private Context context;
    private FileWriter fileWriter;
    private BufferedWriter bufferedWriter;
    private File currentLogFile;
    private String currentSessionId;
    
    // Date formatters
    private SimpleDateFormat timestampFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
    private SimpleDateFormat fileNameFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
    
    private LogManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public static synchronized LogManager getInstance(Context context) {
        if (instance == null) {
            instance = new LogManager(context);
        }
        return instance;
    }
    
    /**
     * Bắt đầu session logging mới với thông tin thiết bị
     */
    public void startNewSession() {
        try {
            endSession(); // Kết thúc session hiện tại nếu có
            
            // Tạo session ID mới
            currentSessionId = "Session_" + fileNameFormat.format(new Date());
            
            // Tạo file log mới
            String fileName = "FH75_" + currentSessionId + ".txt";
            currentLogFile = new File(getLogDirectory(), fileName);
            
            // Khởi tạo writer
            if (currentLogFile.exists() || currentLogFile.createNewFile()) {
                fileWriter = new FileWriter(currentLogFile, true);
                bufferedWriter = new BufferedWriter(fileWriter);
                
                // Ghi header session
                writeLog("=".repeat(80));
                writeLog("FH75 HANDHELD READER - LOG SESSION START");
                writeLog("Session Started: " + timestampFormat.format(new Date()));
                writeLog("Session ID: " + currentSessionId);
                writeLog("=".repeat(80));
                
                // Tự động ghi thông tin thiết bị và app
                logDeviceAndAppInfo();
                
                writeLog("=".repeat(80));
                writeLog("Ready for FH75 communication logging...");
                writeLog("=".repeat(80));
                
                Log.i(TAG, "New logging session started: " + currentSessionId);
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to start new session", e);
        }
    }
    
    /**
     * Ghi log thông tin thiết bị và ứng dụng khi khởi động
     */
    public void logDeviceAndAppInfo() {
        try {
            // Thông tin thiết bị
            writeLog("[DEVICE_INFO] === DEVICE INFORMATION ===");
            writeLog("[DEVICE_INFO] Device Model: " + Build.MODEL);
            writeLog("[DEVICE_INFO] Device Brand: " + Build.BRAND);
            writeLog("[DEVICE_INFO] Device Manufacturer: " + Build.MANUFACTURER);
            writeLog("[DEVICE_INFO] Device Product: " + Build.PRODUCT);
            writeLog("[DEVICE_INFO] Android Version: " + Build.VERSION.RELEASE);
            writeLog("[DEVICE_INFO] API Level: " + Build.VERSION.SDK_INT);
            writeLog("[DEVICE_INFO] Build ID: " + Build.ID);
            writeLog("[DEVICE_INFO] Build Type: " + Build.TYPE);
            writeLog("[DEVICE_INFO] Build Tags: " + Build.TAGS);
            writeLog("[DEVICE_INFO] Hardware: " + Build.HARDWARE);
            writeLog("[DEVICE_INFO] Board: " + Build.BOARD);
            writeLog("[DEVICE_INFO] CPU ABI: " + Build.CPU_ABI);
            
            // Thông tin ứng dụng
            writeLog("[APP_INFO] === APPLICATION INFORMATION ===");
            try {
                PackageManager pm = context.getPackageManager();
                PackageInfo packageInfo = pm.getPackageInfo(context.getPackageName(), 0);
                writeLog("[APP_INFO] App Name: " + pm.getApplicationLabel(packageInfo.applicationInfo));
                writeLog("[APP_INFO] Package Name: " + packageInfo.packageName);
                writeLog("[APP_INFO] Version Name: " + packageInfo.versionName);
                writeLog("[APP_INFO] Version Code: " + packageInfo.versionCode);
                writeLog("[APP_INFO] Target SDK: " + packageInfo.applicationInfo.targetSdkVersion);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    writeLog("[APP_INFO] Min SDK: " + packageInfo.applicationInfo.minSdkVersion);
                }
                writeLog("[APP_INFO] Install Location: " + packageInfo.installLocation);
                writeLog("[APP_INFO] First Install Time: " + timestampFormat.format(new Date(packageInfo.firstInstallTime)));
                writeLog("[APP_INFO] Last Update Time: " + timestampFormat.format(new Date(packageInfo.lastUpdateTime)));
            } catch (PackageManager.NameNotFoundException e) {
                writeLog("[APP_INFO] Could not retrieve app info: " + e.getMessage());
            }
            
            // Thời gian khởi động app
            writeLog("[APP_STARTUP] === APP STARTUP ===");
            writeLog("[APP_STARTUP] App Started At: " + timestampFormat.format(new Date()));
            writeLog("[APP_STARTUP] Log File: " + currentLogFile.getName());
            
        } catch (Exception e) {
            writeLog("[ERROR] Failed to log device/app info: " + e.getMessage());
        }
    }
    
    /**
     * Ghi log kết nối Bluetooth với thông tin chi tiết thiết bị
     */
    public void logBluetoothConnection(String deviceName, String deviceAddress, boolean success) {
        String status = success ? "SUCCESS" : "FAILED";
        writeLog("[BLUETOOTH_CONNECT] === BLUETOOTH CONNECTION ===");
        writeLog("[BLUETOOTH_CONNECT] Status: " + status);
        writeLog("[BLUETOOTH_CONNECT] Device Name: " + (deviceName != null ? deviceName : "Unknown"));
        writeLog("[BLUETOOTH_CONNECT] Device Address: " + (deviceAddress != null ? deviceAddress : "Unknown"));
        writeLog("[BLUETOOTH_CONNECT] Connection Time: " + timestampFormat.format(new Date()));
        
        if (success) {
            writeLog("[BLUETOOTH_CONNECT] Connection established successfully");
            // Log thông tin về loại thiết bị dựa trên tên
            if (deviceName != null) {
                if (deviceName.toLowerCase().contains("fh75") || deviceName.toLowerCase().contains("vh75")) {
                    writeLog("[DEVICE_TYPE] Connected Device Type: FH75/VH75 Handheld Reader");
                    writeLog("[DEVICE_TYPE] Device Category: RFID Handheld Scanner");
                    writeLog("[DEVICE_TYPE] Protocol: Bluetooth RFCOMM");
                } else {
                    writeLog("[DEVICE_TYPE] Connected Device Type: " + deviceName);
                    writeLog("[DEVICE_TYPE] Device Category: Unknown Bluetooth Device");
                }
            }
        } else {
            writeLog("[BLUETOOTH_CONNECT] Connection failed");
        }
    }
    
    /**
     * Ghi log ngắt kết nối Bluetooth
     */
    public void logBluetoothDisconnection(String deviceName) {
        writeLog("[BLUETOOTH_DISCONNECT] Device disconnected: " + (deviceName != null ? deviceName : "Unknown"));
    }
    
    /**
     * Ghi log dữ liệu gửi
     */
    public void logDataSent(byte[] data, String description) {
        String hexData = bytesToHexString(data);
        writeLog("[DATA_SENT] " + hexData + " | " + description);
    }
    
    /**
     * Ghi log dữ liệu nhận
     */
    public void logDataReceived(byte[] data, String description) {
        String hexData = bytesToHexString(data);
        writeLog("[DATA_RECEIVED] " + hexData + " | " + description);
    }
    
    /**
     * Ghi log thông tin
     */
    public void logInfo(String message) {
        writeLog("[INFO] " + message);
    }
    
    /**
     * Ghi log lỗi
     */
    public void logError(String message, Exception e) {
        writeLog("[ERROR] " + message + (e != null ? " - " + e.getMessage() : ""));
        if (e != null) {
            e.printStackTrace();
        }
    }
    
    /**
     * Ghi log RFID scan
     */
    public void logRFIDScan(String tagId, String description) {
        writeLog("[RFID_SCAN] Tag: " + tagId + " | " + description);
    }
    
    /**
     * Kết thúc session hiện tại
     */
    public void endSession() {
        try {
            if (bufferedWriter != null) {
                writeLog("=".repeat(80));
                writeLog("SESSION ENDED: " + timestampFormat.format(new Date()));
                writeLog("=".repeat(80));
                bufferedWriter.close();
                bufferedWriter = null;
            }
            if (fileWriter != null) {
                fileWriter.close();
                fileWriter = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "Error ending session", e);
        }
    }
    
    /**
     * Lấy thư mục lưu log - sử dụng thư mục Download để dễ truy cập
     */
    public File getLogDirectory() {
        // Sử dụng thư mục Download/FH75_Logs để dễ truy cập từ File Manager
        File logDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "FH75_Logs");
        
        if (!logDir.exists()) {
            logDir.mkdirs();
        }
        return logDir;
    }
    
    /**
     * Lấy file log hiện tại
     */
    public File getCurrentLogFile() {
        return currentLogFile;
    }
    
    /**
     * Ghi log với timestamp
     */
    private void writeLog(String message) {
        try {
            if (bufferedWriter != null) {
                String timestampedMessage = "[" + timestampFormat.format(new Date()) + "] " + message;
                bufferedWriter.write(timestampedMessage);
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error writing to log file", e);
        }
    }
    
    /**
     * Chuyển đổi byte array thành hex string
     */
    private String bytesToHexString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }
    
    /**
     * Dọn dẹp log files cũ
     */
    private void cleanOldLogs() {
        File logDir = getLogDirectory();
        File[] logFiles = logDir.listFiles((dir, name) -> name.startsWith("FH75_") && name.endsWith(".txt"));
        
        if (logFiles != null && logFiles.length > 10) { // Giữ lại 10 file gần nhất
            java.util.Arrays.sort(logFiles, (f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified()));
            
            for (int i = 10; i < logFiles.length; i++) {
                boolean deleted = logFiles[i].delete();
                Log.d(TAG, "Deleted old log file: " + logFiles[i].getName() + " - " + deleted);
            }
        }
    }
}