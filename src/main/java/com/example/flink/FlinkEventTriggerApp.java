package com.example.flink;

import com.example.flink.action.ActionHandler;
import com.example.flink.action.DebugPrintActionHandler;
import com.example.flink.action.WebhookActionHandler;
import com.example.flink.api.ProfileApiClient;
import com.example.flink.api.ProfileApiClientImpl;
import com.example.flink.config.AppConfig;
import com.example.flink.config.ConfigLoader;
import com.example.flink.config.FlinkConfig;
import com.example.flink.deserializer.UserEventDeserializer;
import com.example.flink.filter.EventFilterFunction;
import com.example.flink.model.EnrichedEvent;
import com.example.flink.model.UserEvent;
import com.example.flink.processor.UserStateManager;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.connector.kafka.source.enumerator.initializer.OffsetsInitializer;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.runtime.state.hashmap.HashMapStateBackend;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * FlinkEventTriggerApp is the main entry point for the Flink Event Trigger Framework.
 * 
 * This class is responsible for:
 * - Creating and configuring the StreamExecutionEnvironment
 * - Setting up Flink parameters (parallelism, checkpoint interval)
 * - Configuring the State backend (RocksDB or HashMap)
 * - Loading application configuration
 * 
 * Requirements: 1.1
 */
public class FlinkEventTriggerApp {
    private static final Logger logger = LoggerFactory.getLogger(FlinkEventTriggerApp.class);
    
    /**
     * Main entry point for the Flink application.
     * 
     * @param args Command line arguments (optional: config file path)
     * @throws Exception if the application fails to start or execute
     */
    public static void main(String[] args) throws Exception {
        logger.info("Starting Flink Event Trigger Application...");
        
        // Load application configuration
        AppConfig config = loadConfiguration(args);
        logger.info("Configuration loaded: {}", config);
        
        // Create and configure Flink execution environment
        StreamExecutionEnvironment env = createExecutionEnvironment(config);
        
        // Build the complete data pipeline (Task 8.2)
        buildDataPipeline(env, config);
        
        logger.info("Flink environment configured successfully");
        logger.info("Starting Flink job execution...");
        
        // Execute the Flink job
        env.execute("Flink Event Trigger Application");
    }
    
    /**
     * Build the complete data flow pipeline.
     * 
     * Pipeline flow:
     * 1. Create Kafka Source to consume events
     * 2. Apply keyBy(userId) to partition events by user
     * 3. Connect UserStateManager to enrich events with user data
     * 4. Connect EventFilterFunction to filter events based on script
     * 5. Connect ActionHandler to execute actions (Webhook or Debug Print)
     * 
     * Requirements: 1.1, 2.1, 3.3, 4.3, 4.6
     * 
     * @param env StreamExecutionEnvironment
     * @param config Application configuration
     * @throws IOException if filter script cannot be loaded
     */
    private static void buildDataPipeline(StreamExecutionEnvironment env, AppConfig config) 
            throws IOException {
        logger.info("Building data pipeline...");
        
        // Step 1: Create Kafka Source (Requirement 1.1)
        logger.info("Creating Kafka Source...");
        KafkaSource<UserEvent> kafkaSource = KafkaSource.<UserEvent>builder()
            .setBootstrapServers(config.getKafka().getBootstrapServers())
            .setTopics(config.getKafka().getTopic())
            .setGroupId(config.getKafka().getConsumerGroupId())
            .setValueOnlyDeserializer(new UserEventDeserializer())
            .setStartingOffsets(OffsetsInitializer.latest())
            .build();
        
        DataStream<UserEvent> eventStream = env.fromSource(
            kafkaSource, 
            WatermarkStrategy.noWatermarks(), 
            "Kafka Source"
        );
        logger.info("Kafka Source created: topic={}, bootstrap-servers={}", 
            config.getKafka().getTopic(), 
            config.getKafka().getBootstrapServers());
        
        // Step 2: Apply keyBy(userId) to partition events by user (Requirement 2.1)
        logger.info("Applying keyBy(userId) partitioning...");
        
        // Step 3: Connect UserStateManager to enrich events with user data (Requirement 2.1)
        logger.info("Creating ProfileApiClient and UserStateManager...");
        ProfileApiClient profileApiClient = new ProfileApiClientImpl(
            config.getProfileApi().getBaseUrl(),
            config.getProfileApi().getTimeoutMs()
        );
        
        UserStateManager stateManager = new UserStateManager(profileApiClient);
        
        DataStream<EnrichedEvent> enrichedStream = eventStream
            .keyBy(UserEvent::getUserId)
            .process(stateManager)
            .name("User State Manager");
        logger.info("UserStateManager connected with Profile API: {}", 
            config.getProfileApi().getBaseUrl());
        
        // Step 4: Connect EventFilterFunction to filter events (Requirement 3.3)
        logger.info("Loading filter script and creating EventFilterFunction...");
        String filterScript = loadFilterScript(config.getFilter().getScriptPath());
        EventFilterFunction filterFunction = new EventFilterFunction(filterScript);
        
        DataStream<EnrichedEvent> filteredStream = enrichedStream
            .filter(filterFunction)
            .name("Event Filter");
        logger.info("EventFilterFunction connected with script: {}", 
            config.getFilter().getScriptPath());
        
        // Step 5: Connect ActionHandler based on configuration (Requirements 4.3, 4.6)
        logger.info("Creating ActionHandler...");
        ActionHandler actionHandler = createActionHandler(config);
        
        filteredStream
            .map(event -> {
                actionHandler.execute(event);
                return event;
            })
            .name("Action Handler");
        
        logger.info("Data pipeline built successfully");
    }
    
    /**
     * Load the filter script from file or classpath.
     * 
     * @param scriptPath Path to the filter script
     * @return Filter script content
     * @throws IOException if script cannot be loaded
     */
    private static String loadFilterScript(String scriptPath) throws IOException {
        logger.info("Loading filter script from: {}", scriptPath);
        
        try {
            // Try to load from file system first
            if (Files.exists(Paths.get(scriptPath))) {
                String script = Files.readString(Paths.get(scriptPath));
                logger.info("Filter script loaded from file system");
                return script;
            }
            
            // Try to load from classpath
            try (var inputStream = FlinkEventTriggerApp.class.getClassLoader()
                    .getResourceAsStream(scriptPath)) {
                if (inputStream != null) {
                    String script = new String(inputStream.readAllBytes());
                    logger.info("Filter script loaded from classpath");
                    return script;
                }
            }
            
            // If script file doesn't exist, use a default pass-through script
            logger.warn("Filter script not found at: {}, using default pass-through script", scriptPath);
            return "true"; // Default script that passes all events
            
        } catch (IOException e) {
            logger.error("Failed to load filter script from: {}", scriptPath, e);
            throw e;
        }
    }
    
    /**
     * Create ActionHandler based on configuration.
     * 
     * Determines which action handler to use based on configuration:
     * - If debug is enabled: use DebugPrintActionHandler
     * - Otherwise: use WebhookActionHandler
     * 
     * Requirements: 4.3, 4.6
     * 
     * @param config Application configuration
     * @return ActionHandler instance
     */
    private static ActionHandler createActionHandler(AppConfig config) {
        // Check if debug mode is enabled
        if (config.getActions().getDebug() != null && 
            config.getActions().getDebug().isEnabled()) {
            logger.info("Creating DebugPrintActionHandler (debug mode enabled)");
            return new DebugPrintActionHandler();
        }
        
        // Otherwise use Webhook handler
        String webhookUrl = config.getActions().getWebhook().getUrl();
        int timeoutMs = config.getActions().getWebhook().getTimeoutMs();
        logger.info("Creating WebhookActionHandler: url={}, timeout={}ms", webhookUrl, timeoutMs);
        return new WebhookActionHandler(webhookUrl, timeoutMs);
    }
    
    /**
     * Load application configuration from file or classpath.
     * 
     * @param args Command line arguments (optional: config file path)
     * @return AppConfig object with loaded configuration
     * @throws IOException if configuration cannot be loaded
     */
    private static AppConfig loadConfiguration(String[] args) throws IOException {
        String configPath = "application.yml";
        
        // Check if custom config path is provided
        if (args.length > 0) {
            configPath = args[0];
            logger.info("Using custom configuration file: {}", configPath);
        } else {
            logger.info("Using default configuration file: {}", configPath);
        }
        
        return ConfigLoader.loadConfig(configPath);
    }
    
    /**
     * Create and configure the Flink StreamExecutionEnvironment.
     * 
     * This method:
     * 1. Creates the execution environment
     * 2. Sets the parallelism from configuration
     * 3. Enables checkpointing with configured interval
     * 4. Configures the state backend (RocksDB or HashMap)
     * 
     * @param config Application configuration
     * @return Configured StreamExecutionEnvironment
     * @throws IOException if state backend configuration fails
     */
    private static StreamExecutionEnvironment createExecutionEnvironment(AppConfig config) 
            throws IOException {
        logger.info("Creating Flink execution environment...");
        
        // Create the execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        FlinkConfig flinkConfig = config.getFlink();
        
        // Configure parallelism
        int parallelism = flinkConfig.getParallelism();
        env.setParallelism(parallelism);
        logger.info("Parallelism set to: {}", parallelism);
        
        // Enable checkpointing
        long checkpointInterval = flinkConfig.getCheckpointIntervalMs();
        env.enableCheckpointing(checkpointInterval);
        logger.info("Checkpointing enabled with interval: {} ms", checkpointInterval);
        
        // Configure state backend
        String stateBackend = flinkConfig.getStateBackend();
        configureStateBackend(env, stateBackend);
        
        return env;
    }
    
    /**
     * Configure the Flink state backend.
     * 
     * Supports:
     * - "rocksdb": EmbeddedRocksDBStateBackend for large state and incremental checkpoints
     * - "hashmap": HashMapStateBackend for smaller state stored in memory
     * 
     * @param env StreamExecutionEnvironment to configure
     * @param stateBackend State backend type ("rocksdb" or "hashmap")
     * @throws IOException if state backend configuration fails
     */
    private static void configureStateBackend(StreamExecutionEnvironment env, String stateBackend) 
            throws IOException {
        logger.info("Configuring state backend: {}", stateBackend);
        
        if ("rocksdb".equalsIgnoreCase(stateBackend)) {
            // Use RocksDB state backend for large state
            // RocksDB stores state on disk and supports incremental checkpoints
            // Pass 'true' to constructor to enable incremental checkpointing
            EmbeddedRocksDBStateBackend rocksDBBackend = new EmbeddedRocksDBStateBackend(true);
            
            env.setStateBackend(rocksDBBackend);
            logger.info("RocksDB state backend configured with incremental checkpointing enabled");
            
        } else if ("hashmap".equalsIgnoreCase(stateBackend)) {
            // Use HashMap state backend for smaller state
            // HashMap stores state in memory (on heap or off heap)
            HashMapStateBackend hashMapBackend = new HashMapStateBackend();
            
            env.setStateBackend(hashMapBackend);
            logger.info("HashMap state backend configured");
            
        } else {
            logger.warn("Unknown state backend: {}. Using default (HashMap)", stateBackend);
            HashMapStateBackend defaultBackend = new HashMapStateBackend();
            env.setStateBackend(defaultBackend);
        }
    }
}
