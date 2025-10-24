package com.vanch.vhxdemo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Activity để test LogManager
 * Bạn có thể gọi activity này để kiểm tra logging
 */
public class TestLogManager extends Activity {
    private static final String TAG = "TestLogManager";
    private LogManager logManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Khởi tạo LogManager
        logManager = LogManager.getInstance(this);
        
        // Test các phương thức logging
        testLogging();
    }
    
    private void testLogging() {
        Log.i(TAG, "Starting LogManager test...");
        
        // Bắt đầu session mới (sẽ tự động log device & app info)
        logManager.startNewSession();
        
        // Test log thông tin
        logManager.logInfo("Test logging system initialized");
        
        // Test log kết nối Bluetooth với thông tin chi tiết
        logManager.logBluetoothConnection("FH75-Test-Device", "00:11:22:33:44:55", true);
        
        // Test log dữ liệu gửi
        byte[] testSendData = {(byte)0xF0, 0x03, 0x01, 0x02, 0x03};
        logManager.logDataSent(testSendData, "Test command to FH75");
        
        // Test log dữ liệu nhận
        byte[] testReceiveData = {(byte)0xF4, 0x05, 0x00, 0x01, 0x02, 0x03, 0x04};
        logManager.logDataReceived(testReceiveData, "Test response from FH75");
        
        // Test log lỗi
        try {
            throw new RuntimeException("Test exception for logging");
        } catch (Exception e) {
            logManager.logError("Test error logging", e);
        }
        
        // Test log ngắt kết nối
        logManager.logBluetoothDisconnection("FH75-Test-Device");
        
        // Test log RFID scan - remove this line as method doesn't exist
        // logManager.logRFIDScan("E2801160600002045CB7A965", "Tag scanned successfully");
        
        // Test manual device info logging
        logManager.logDeviceAndAppInfo();
        
        // Kết thúc session - remove this line as method doesn't exist
        // logManager.endSession();
        
        Log.i(TAG, "LogManager test completed. Check log files in: " + 
            logManager.getLogDirectory());
    }
}