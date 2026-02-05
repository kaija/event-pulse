package com.example.flink.api;

import com.example.flink.model.EventHistory;
import com.example.flink.model.UserProfile;
import com.example.flink.model.Visit;

import java.util.List;

/**
 * Profile API 客戶端介面
 * 用於從外部 API 取得使用者資料、訪問資訊和事件歷史
 */
public interface ProfileApiClient {
    /**
     * 取得使用者資料
     * @param userId 使用者識別碼
     * @return 使用者資料（包含 country, city, language, continent, timezone）
     * @throws ApiException 當 API 呼叫失敗時
     */
    UserProfile getUserProfile(String userId) throws ApiException;
    
    /**
     * 取得使用者當前訪問資訊
     * @param userId 使用者識別碼
     * @return 訪問資訊（包含 uuid, os, browser, device, landing_page 等）
     * @throws ApiException 當 API 呼叫失敗時
     */
    Visit getCurrentVisit(String userId) throws ApiException;
    
    /**
     * 取得使用者事件歷史
     * @param userId 使用者識別碼
     * @return 事件歷史列表
     * @throws ApiException 當 API 呼叫失敗時
     */
    List<EventHistory> getEventHistory(String userId) throws ApiException;
}
