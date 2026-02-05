package com.example.flink.api;

/**
 * API 呼叫例外類別
 * 當 Profile API 呼叫失敗時拋出此例外
 */
public class ApiException extends Exception {
    
    /**
     * 建立 API 例外
     * @param message 錯誤訊息
     */
    public ApiException(String message) {
        super(message);
    }
    
    /**
     * 建立 API 例外
     * @param message 錯誤訊息
     * @param cause 原因例外
     */
    public ApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
