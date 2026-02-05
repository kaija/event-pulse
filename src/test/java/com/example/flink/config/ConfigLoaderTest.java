package com.example.flink.config;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigLoader
 */
class ConfigLoaderTest {

    @Test
    void testLoadConfigFromClasspath() throws IOException {
        AppConfig config = ConfigLoader.loadConfig("application.yml");
        
        assertNotNull(config);
        assertNotNull(config.getKafka());
        assertNotNull(config.getProfileApi());
        assertNotNull(config.getFilter());
        assertNotNull(config.getActions());
        assertNotNull(config.getFlink());
        
        // Verify Kafka config
        assertEquals("localhost:9092", config.getKafka().getBootstrapServers());
        assertEquals("user-tracking-events", config.getKafka().getTopic());
        assertEquals("flink-event-trigger", config.getKafka().getConsumerGroupId());
        
        // Verify Profile API config
        assertEquals("http://localhost:8080", config.getProfileApi().getBaseUrl());
        assertEquals(5000, config.getProfileApi().getTimeoutMs());
        assertEquals(3, config.getProfileApi().getRetryAttempts());
        
        // Verify Filter config
        assertEquals("./scripts/filter.av", config.getFilter().getScriptPath());
        
        // Verify Actions config
        assertNotNull(config.getActions().getWebhook());
        assertEquals("http://localhost:8080/webhook", config.getActions().getWebhook().getUrl());
        assertEquals(3000, config.getActions().getWebhook().getTimeoutMs());
        assertTrue(config.getActions().getDebug().isEnabled());
        
        // Verify Flink config
        assertEquals(4, config.getFlink().getParallelism());
        assertEquals(60000, config.getFlink().getCheckpointIntervalMs());
        assertEquals("rocksdb", config.getFlink().getStateBackend());
        assertEquals(10, config.getFlink().getStateTtlMinutes());
    }

    @Test
    void testLoadConfigFileNotFound() {
        assertThrows(IOException.class, () -> {
            ConfigLoader.loadConfig("nonexistent.yml");
        });
    }
}
