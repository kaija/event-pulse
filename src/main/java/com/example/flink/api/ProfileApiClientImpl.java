package com.example.flink.api;

import com.example.flink.model.EventHistory;
import com.example.flink.model.UserProfile;
import com.example.flink.model.Visit;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

/**
 * ProfileApiClient 的實作類別
 * 使用 Java HttpClient 實作 HTTP 請求，呼叫外部 API 取得使用者資料
 */
public class ProfileApiClientImpl implements ProfileApiClient {
    private static final Logger logger = LoggerFactory.getLogger(ProfileApiClientImpl.class);
    
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final int timeoutMs;
    
    /**
     * 建立 ProfileApiClientImpl 實例
     * @param baseUrl API 基礎 URL
     * @param timeoutMs 請求逾時時間（毫秒）
     */
    public ProfileApiClientImpl(String baseUrl, int timeoutMs) {
        this.baseUrl = baseUrl;
        this.timeoutMs = timeoutMs;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(timeoutMs))
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public UserProfile getUserProfile(String userId) throws ApiException {
        if (userId == null || userId.isEmpty()) {
            throw new ApiException("User ID cannot be null or empty");
        }
        
        String url = baseUrl + "/users/" + userId + "/profile";
        logger.debug("Fetching user profile from: {}", url);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                String errorMsg = String.format("API returned status %d for getUserProfile: %s", 
                    response.statusCode(), response.body());
                logger.error(errorMsg);
                throw new ApiException(errorMsg);
            }
            
            UserProfile profile = objectMapper.readValue(response.body(), UserProfile.class);
            logger.debug("Successfully fetched user profile for userId: {}", userId);
            return profile;
            
        } catch (IOException e) {
            String errorMsg = String.format("Failed to fetch user profile for userId: %s", userId);
            logger.error(errorMsg, e);
            throw new ApiException(errorMsg, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = String.format("Request interrupted while fetching user profile for userId: %s", userId);
            logger.error(errorMsg, e);
            throw new ApiException(errorMsg, e);
        }
    }
    
    @Override
    public Visit getCurrentVisit(String userId) throws ApiException {
        if (userId == null || userId.isEmpty()) {
            throw new ApiException("User ID cannot be null or empty");
        }
        
        String url = baseUrl + "/users/" + userId + "/visit";
        logger.debug("Fetching current visit from: {}", url);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                String errorMsg = String.format("API returned status %d for getCurrentVisit: %s", 
                    response.statusCode(), response.body());
                logger.error(errorMsg);
                throw new ApiException(errorMsg);
            }
            
            Visit visit = objectMapper.readValue(response.body(), Visit.class);
            logger.debug("Successfully fetched current visit for userId: {}", userId);
            return visit;
            
        } catch (IOException e) {
            String errorMsg = String.format("Failed to fetch current visit for userId: %s", userId);
            logger.error(errorMsg, e);
            throw new ApiException(errorMsg, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = String.format("Request interrupted while fetching current visit for userId: %s", userId);
            logger.error(errorMsg, e);
            throw new ApiException(errorMsg, e);
        }
    }
    
    @Override
    public List<EventHistory> getEventHistory(String userId) throws ApiException {
        if (userId == null || userId.isEmpty()) {
            throw new ApiException("User ID cannot be null or empty");
        }
        
        String url = baseUrl + "/users/" + userId + "/history";
        logger.debug("Fetching event history from: {}", url);
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMillis(timeoutMs))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                String errorMsg = String.format("API returned status %d for getEventHistory: %s", 
                    response.statusCode(), response.body());
                logger.error(errorMsg);
                throw new ApiException(errorMsg);
            }
            
            List<EventHistory> history = objectMapper.readValue(response.body(), 
                new TypeReference<List<EventHistory>>() {});
            logger.debug("Successfully fetched event history for userId: {}", userId);
            return history;
            
        } catch (IOException e) {
            String errorMsg = String.format("Failed to fetch event history for userId: %s", userId);
            logger.error(errorMsg, e);
            throw new ApiException(errorMsg, e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            String errorMsg = String.format("Request interrupted while fetching event history for userId: %s", userId);
            logger.error(errorMsg, e);
            throw new ApiException(errorMsg, e);
        }
    }
}
