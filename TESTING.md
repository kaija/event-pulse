# Testing Guide - Kafka Event Producer

This guide explains how to use the Kafka Event Producer testing tool to send test events to the Flink Event Trigger application.

## Overview

The Kafka Event Producer is a command-line tool that allows you to:
- Send individual test events to Kafka
- Send multiple events from a directory
- Control the timing between events
- Test the complete event processing pipeline

## Prerequisites

1. **Build the project**:
   ```bash
   mvn clean package
   ```

2. **Start the Docker Compose environment**:
   ```bash
   docker-compose up -d
   ```

3. **Verify Kafka is running**:
   ```bash
   docker ps | grep kafka
   ```

4. **Create the Kafka topic** (if not auto-created):
   ```bash
   ./create-topic.sh
   ```

## Quick Start

### Send a Single Event

**Linux/Mac**:
```bash
./send-test-event.sh test-data/sample-event-1.json
```

**Windows**:
```cmd
send-test-event.bat test-data\sample-event-1.json
```

### Send All Events from Directory

**Linux/Mac**:
```bash
# Send all events with 1 second delay between each
./send-test-event.sh test-data/ 1000
```

**Windows**:
```cmd
send-test-event.bat test-data\ 1000
```

## Usage

### Using the Shell Script (Recommended)

The shell script provides a convenient wrapper around the Java tool.

**Syntax**:
```bash
./send-test-event.sh <json-file-or-directory> [delay-ms]
```

**Parameters**:
- `json-file-or-directory`: Path to a JSON file or directory containing JSON files
- `delay-ms`: (Optional) Delay in milliseconds between sending events (default: 0)

**Environment Variables**:
- `KAFKA_BOOTSTRAP_SERVERS`: Kafka bootstrap servers (default: `localhost:9092`)
- `KAFKA_TOPIC`: Kafka topic name (default: `user-tracking-events`)

**Examples**:
```bash
# Send a single event
./send-test-event.sh test-data/sample-event-1.json

# Send all events with 2 second delay
./send-test-event.sh test-data/ 2000

# Use custom Kafka configuration
KAFKA_BOOTSTRAP_SERVERS=kafka:9092 KAFKA_TOPIC=my-topic ./send-test-event.sh test-data/sample-event-1.json
```

### Using Java Directly

You can also run the Java class directly for more control.

**Syntax**:
```bash
java -cp target/flink-event-trigger-1.0-SNAPSHOT.jar \
  com.example.flink.tools.KafkaEventProducer \
  <bootstrap-servers> <topic> [json-file-or-directory] [delay-ms]
```

**Examples**:
```bash
# Send a single event
java -cp target/flink-event-trigger-1.0-SNAPSHOT.jar \
  com.example.flink.tools.KafkaEventProducer \
  localhost:9092 user-tracking-events test-data/sample-event-1.json

# Send all events from directory with 1 second delay
java -cp target/flink-event-trigger-1.0-SNAPSHOT.jar \
  com.example.flink.tools.KafkaEventProducer \
  localhost:9092 user-tracking-events test-data/ 1000
```

## Test Scenarios

### Scenario 1: Test Single User Journey

Send events for a single user to test state management:

```bash
# Event 1: Page view
./send-test-event.sh test-data/sample-event-1.json
sleep 2

# Event 2: Add to cart
./send-test-event.sh test-data/sample-event-2.json
sleep 2

# Event 3: Purchase
./send-test-event.sh test-data/sample-event-purchase.json
```

This tests:
- User checkpoint creation on first event
- State persistence across events
- Event enrichment with user data

### Scenario 2: Test Multiple Users

Send events for different users to test parallel processing:

```bash
# Send all events at once (different users)
./send-test-event.sh test-data/ 0
```

This tests:
- Multiple user state management
- Kafka partitioning by user ID
- Parallel event processing

### Scenario 3: Test State TTL

Send events with delays to test state expiration:

```bash
# Send first event
./send-test-event.sh test-data/sample-event-1.json

# Wait 11 minutes (longer than 10 minute TTL)
sleep 660

# Send another event for the same user
./send-test-event.sh test-data/sample-event-2.json
```

This tests:
- State TTL expiration after 10 minutes
- Profile API re-fetch after state expiration

### Scenario 4: Test Event Filtering

Send events with different characteristics to test filter logic:

```bash
# Send triggerable event
./send-test-event.sh test-data/sample-event-1.json

# Modify an event to set triggerable: false and send it
# (Create a custom event file first)
```

This tests:
- AviatorScript filter execution
- Event filtering based on conditions

## Monitoring Events

### View Kafka Messages

Check if events are being sent to Kafka:

```bash
# Consume messages from Kafka topic
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-tracking-events \
  --from-beginning
```

### View Flink Logs

Check Flink processing logs:

```bash
# JobManager logs
docker logs flink-jobmanager

# TaskManager logs
docker logs flink-taskmanager
```

### View Application Output

If using Debug Print action, check the logs for event output:

```bash
docker logs flink-taskmanager | grep "DEBUG EVENT"
```

## Troubleshooting

### Error: JAR file not found

**Problem**: The compiled JAR file doesn't exist.

**Solution**:
```bash
mvn clean package
```

### Error: Connection refused to Kafka

**Problem**: Kafka is not running or not accessible.

**Solution**:
```bash
# Check if Kafka is running
docker ps | grep kafka

# Start Docker Compose if not running
docker-compose up -d

# Wait for Kafka to be ready (about 30 seconds)
sleep 30
```

### Error: Topic does not exist

**Problem**: The Kafka topic hasn't been created.

**Solution**:
```bash
# Create the topic manually
./create-topic.sh

# Or enable auto-create in Kafka (already configured in docker-compose.yml)
```

### Events not being processed

**Problem**: Events are sent but not processed by Flink.

**Solution**:
1. Check if Flink job is running:
   ```bash
   docker exec -it flink-jobmanager flink list
   ```

2. Check Flink logs for errors:
   ```bash
   docker logs flink-jobmanager
   docker logs flink-taskmanager
   ```

3. Verify the event format matches the UserEvent model

## Advanced Usage

### Programmatic Usage

You can use the KafkaEventProducer class in your own Java code:

```java
import com.example.flink.tools.KafkaEventProducer;

public class MyTest {
    public static void main(String[] args) throws Exception {
        KafkaEventProducer producer = new KafkaEventProducer(
            "localhost:9092", 
            "user-tracking-events"
        );
        
        try {
            // Send a single event
            String jsonEvent = "{\"user_id\":\"test_user\",\"event_type\":\"test\"}";
            producer.sendEvent(jsonEvent, "test_user");
            
            // Send from file
            producer.sendEventFromFile("test-data/sample-event-1.json");
            
            // Send from directory
            producer.sendEventsFromDirectory("test-data/", 1000);
        } finally {
            producer.close();
        }
    }
}
```

### Custom Event Generation

Create custom events programmatically:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.flink.model.UserEvent;
import com.example.flink.model.EventParameters;

ObjectMapper mapper = new ObjectMapper();
UserEvent event = new UserEvent();
event.setEventId("custom_001");
event.setUserId("user_999");
event.setEventType("custom_event");
event.setTimestamp(System.currentTimeMillis());
// ... set other fields

String jsonEvent = mapper.writeValueAsString(event);
producer.sendEvent(jsonEvent, event.getUserId());
```

## Sample Events

The `test-data/` directory contains several pre-built sample events:

- `sample-event-1.json`: Page view (first event for user_123)
- `sample-event-2.json`: Button click (add to cart)
- `sample-event-3.json`: Mobile page view (different user)
- `sample-event-4.json`: Form submission (single event visit)
- `sample-event-purchase.json`: Purchase completion

See `test-data/README.md` for detailed descriptions of each event.

## Best Practices

1. **Start with single events**: Test one event at a time before sending batches
2. **Use delays**: Add delays between events to simulate realistic user behavior
3. **Monitor logs**: Always check Flink logs to verify event processing
4. **Test edge cases**: Create custom events to test error handling
5. **Clean state**: Restart Flink between test runs to clear state if needed

## Related Documentation

- [README.md](README.md) - Main project documentation
- [PIPELINE.md](PIPELINE.md) - Pipeline architecture and flow
- [test-data/README.md](test-data/README.md) - Sample event descriptions
- [scripts/FILTER_EXAMPLES.md](scripts/FILTER_EXAMPLES.md) - Filter script examples
