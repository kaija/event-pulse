package com.example.flink.deserializer;

import com.example.flink.model.UserEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.flink.api.common.serialization.DeserializationSchema;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Deserializer for UserEvent objects from Kafka messages.
 * Implements Flink's DeserializationSchema interface to convert JSON byte arrays
 * into UserEvent objects.
 * 
 * Error Handling:
 * - Invalid JSON messages are logged and return null (Flink filters out null values)
 * - Deserialization errors do not interrupt the stream processing
 * 
 * Requirements: 1.2, 1.3
 */
public class UserEventDeserializer implements DeserializationSchema<UserEvent> {
    private static final long serialVersionUID = 1L;
    private static final Logger logger = LoggerFactory.getLogger(UserEventDeserializer.class);
    
    private transient ObjectMapper objectMapper;

    /**
     * Initialize the ObjectMapper for JSON deserialization.
     * This method is called once when the deserializer is created.
     */
    @Override
    public void open(InitializationContext context) throws Exception {
        objectMapper = new ObjectMapper();
    }

    /**
     * Deserialize a Kafka message byte array into a UserEvent object.
     * 
     * @param message the raw message bytes from Kafka
     * @return the deserialized UserEvent, or null if deserialization fails
     * @throws IOException if an I/O error occurs during deserialization
     */
    @Override
    public UserEvent deserialize(byte[] message) throws IOException {
        if (message == null || message.length == 0) {
            logger.warn("Received null or empty message");
            return null;
        }

        try {
            // Lazy initialization for objectMapper if not initialized via open()
            if (objectMapper == null) {
                objectMapper = new ObjectMapper();
            }
            
            UserEvent event = objectMapper.readValue(message, UserEvent.class);
            
            // Validate that essential fields are present
            if (event.getUserId() == null || event.getUserId().isEmpty()) {
                logger.error("Deserialized event missing userId: {}", new String(message));
                return null;
            }
            
            if (event.getEventType() == null || event.getEventType().isEmpty()) {
                logger.error("Deserialized event missing eventType: {}", new String(message));
                return null;
            }
            
            return event;
        } catch (Exception e) {
            // Log error with the raw message content for debugging
            logger.error("Failed to deserialize event: {}", new String(message), e);
            // Return null - Flink will filter out null values automatically
            return null;
        }
    }

    /**
     * Indicates whether this deserializer produces null values on errors.
     * 
     * @return true, as this deserializer returns null for invalid messages
     */
    @Override
    public boolean isEndOfStream(UserEvent nextElement) {
        // Never signal end of stream - we process continuously
        return false;
    }

    /**
     * Get the type information for the produced type.
     * 
     * @return TypeInformation for UserEvent class
     */
    @Override
    public TypeInformation<UserEvent> getProducedType() {
        return TypeInformation.of(UserEvent.class);
    }
}
