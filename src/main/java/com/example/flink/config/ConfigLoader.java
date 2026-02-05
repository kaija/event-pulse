package com.example.flink.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Utility class to load application configuration from YAML file
 */
public class ConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    /**
     * Load configuration from application.yml file
     * 
     * @param configPath Path to the configuration file
     * @return AppConfig object
     * @throws IOException if file cannot be read or parsed
     */
    public static AppConfig loadConfig(String configPath) throws IOException {
        logger.info("Loading configuration from: {}", configPath);
        
        File configFile = new File(configPath);
        if (configFile.exists()) {
            AppConfig config = yamlMapper.readValue(configFile, AppConfig.class);
            logger.info("Configuration loaded successfully from file: {}", configPath);
            return config;
        }
        
        // Try to load from classpath
        InputStream inputStream = ConfigLoader.class.getClassLoader().getResourceAsStream(configPath);
        if (inputStream != null) {
            AppConfig config = yamlMapper.readValue(inputStream, AppConfig.class);
            logger.info("Configuration loaded successfully from classpath: {}", configPath);
            return config;
        }
        
        throw new IOException("Configuration file not found: " + configPath);
    }

    /**
     * Load configuration from default location (application.yml)
     * 
     * @return AppConfig object
     * @throws IOException if file cannot be read or parsed
     */
    public static AppConfig loadConfig() throws IOException {
        return loadConfig("application.yml");
    }
}
