package com.example.flink.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Kafka Event Producer Testing Tool
 * 
 * This tool sends test events to Kafka for testing the Flink Event Trigger application.
 * It can send events from JSON files or generate sample events programmatically.
 * 
 * Usage:
 *   java -cp target/flink-event-trigger-1.0-SNAPSHOT.jar \
 *     com.example.flink.tools.KafkaEventProducer \
 *     [bootstrap-servers] [topic] [json-file-path]
 * 
 * Example:
 *   java -cp target/flink-event-trigger-1.0-SNAPSHOT.jar \
 *     com.example.flink.tools.KafkaEventProducer \
 *     localhost:9092 user-tracking-events test-data/sample-event.json
 * 
 * Requirements: 1.1, 1.2
 */
public class KafkaEventProducer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private final KafkaProducer<String, String> producer;
    private final String topic;
    
    public KafkaEventProducer(String bootstrapServers, String topic) {
        this.topic = topic;
        
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        props.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        
        this.producer = new KafkaProducer<>(props);
        System.out.println("[INFO] Kafka producer initialized with bootstrap servers: " + 
                          bootstrapServers + ", topic: " + topic);
    }
    
    /**
     * Send a JSON event to Kafka
     * 
     * @param jsonEvent JSON string representing the event
     * @param userId User ID to use as the Kafka message key (for partitioning)
     * @return RecordMetadata containing information about the sent message
     * @throws ExecutionException if the send operation fails
     * @throws InterruptedException if the send operation is interrupted
     */
    public RecordMetadata sendEvent(String jsonEvent, String userId) 
            throws ExecutionException, InterruptedException {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, userId, jsonEvent);
        
        Future<RecordMetadata> future = producer.send(record, (metadata, exception) -> {
            if (exception != null) {
                System.err.println("[ERROR] Failed to send event for userId: " + userId);
                exception.printStackTrace();
            } else {
                System.out.println("[INFO] Event sent successfully - Topic: " + metadata.topic() + 
                                 ", Partition: " + metadata.partition() + 
                                 ", Offset: " + metadata.offset() + 
                                 ", UserId: " + userId);
            }
        });
        
        return future.get();
    }
    
    /**
     * Send an event from a JSON file
     * 
     * @param jsonFilePath Path to the JSON file
     * @return RecordMetadata containing information about the sent message
     * @throws IOException if the file cannot be read
     * @throws ExecutionException if the send operation fails
     * @throws InterruptedException if the send operation is interrupted
     */
    public RecordMetadata sendEventFromFile(String jsonFilePath) 
            throws IOException, ExecutionException, InterruptedException {
        String jsonContent = new String(Files.readAllBytes(Paths.get(jsonFilePath)));
        
        // Parse JSON to extract userId for the key
        Object jsonObject = objectMapper.readValue(jsonContent, Object.class);
        String userId = extractUserId(jsonContent);
        
        System.out.println("[INFO] Sending event from file: " + jsonFilePath);
        return sendEvent(jsonContent, userId);
    }
    
    /**
     * Extract userId from JSON string
     */
    private String extractUserId(String jsonContent) {
        try {
            var node = objectMapper.readTree(jsonContent);
            if (node.has("user_id")) {
                return node.get("user_id").asText();
            }
            return "unknown";
        } catch (Exception e) {
            System.err.println("[WARN] Failed to extract userId from JSON, using 'unknown'");
            return "unknown";
        }
    }
    
    /**
     * Send multiple events from a directory containing JSON files
     * 
     * @param directoryPath Path to the directory containing JSON files
     * @param delayMs Delay in milliseconds between sending events
     * @return Number of events sent successfully
     */
    public int sendEventsFromDirectory(String directoryPath, long delayMs) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            System.err.println("[ERROR] Directory does not exist: " + directoryPath);
            return 0;
        }
        
        File[] jsonFiles = directory.listFiles((dir, name) -> name.endsWith(".json"));
        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("[WARN] No JSON files found in directory: " + directoryPath);
            return 0;
        }
        
        int successCount = 0;
        for (File file : jsonFiles) {
            try {
                sendEventFromFile(file.getAbsolutePath());
                successCount++;
                
                if (delayMs > 0) {
                    Thread.sleep(delayMs);
                }
            } catch (Exception e) {
                System.err.println("[ERROR] Failed to send event from file: " + file.getName());
                e.printStackTrace();
            }
        }
        
        System.out.println("[INFO] Sent " + successCount + " out of " + jsonFiles.length + 
                         " events from directory: " + directoryPath);
        return successCount;
    }
    
    /**
     * Close the producer and release resources
     */
    public void close() {
        if (producer != null) {
            producer.flush();
            producer.close();
            System.out.println("[INFO] Kafka producer closed");
        }
    }
    
    /**
     * Main method for command-line usage
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: KafkaEventProducer <bootstrap-servers> <topic> [json-file-or-directory] [delay-ms]");
            System.err.println("Example: KafkaEventProducer localhost:9092 user-tracking-events test-data/sample-event.json");
            System.err.println("Example: KafkaEventProducer localhost:9092 user-tracking-events test-data/ 1000");
            System.exit(1);
        }
        
        String bootstrapServers = args[0];
        String topic = args[1];
        String path = args.length > 2 ? args[2] : null;
        long delayMs = args.length > 3 ? Long.parseLong(args[3]) : 0;
        
        KafkaEventProducer producer = new KafkaEventProducer(bootstrapServers, topic);
        
        try {
            if (path != null) {
                File file = new File(path);
                if (file.isDirectory()) {
                    int count = producer.sendEventsFromDirectory(path, delayMs);
                    System.out.println("Successfully sent " + count + " events");
                } else if (file.isFile()) {
                    RecordMetadata metadata = producer.sendEventFromFile(path);
                    System.out.println("Event sent successfully to partition " + 
                                     metadata.partition() + " at offset " + metadata.offset());
                } else {
                    System.err.println("Path does not exist: " + path);
                    System.exit(1);
                }
            } else {
                System.out.println("No file or directory specified. Producer initialized successfully.");
                System.out.println("You can use this class programmatically to send events.");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] Error sending events: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            producer.close();
        }
    }
}
