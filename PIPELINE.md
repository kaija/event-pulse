# Flink Event Trigger Pipeline

## Overview

This document describes the complete data flow pipeline implemented in the Flink Event Trigger Framework.

## Pipeline Architecture

The pipeline consists of 5 main stages:

```
Kafka → Deserialize → KeyBy → State Manager → Filter → Action Handler
```

### 1. Kafka Source (Event Ingestion)

**Component**: `KafkaSource<UserEvent>`  
**Configuration**: 
- Bootstrap servers: `kafka.bootstrap-servers`
- Topic: `kafka.topic`
- Consumer group: `kafka.consumer-group-id`

**Functionality**:
- Consumes events from Kafka topic
- Uses `UserEventDeserializer` to convert JSON messages to `UserEvent` objects
- Starts from latest offset
- Invalid messages are logged and filtered out (return null)

**Requirements**: 1.1, 1.2, 1.3

### 2. KeyBy Partitioning

**Component**: `keyBy(UserEvent::getUserId)`

**Functionality**:
- Partitions events by user ID
- Ensures all events for the same user go to the same parallel instance
- Enables stateful processing per user

**Requirements**: 2.1

### 3. User State Manager (Event Enrichment)

**Component**: `UserStateManager` (KeyedProcessFunction)  
**State**: Flink Keyed State with 10-minute TTL

**Functionality**:
- Maintains user checkpoint in Flink Keyed State
- On first event or after TTL expiration:
  - Calls Profile API to fetch user profile, visit, and event history
  - Creates new checkpoint and stores in state
- On subsequent events:
  - Loads checkpoint from state
  - Updates last activity time (resets TTL)
- Enriches events with user data
- Outputs `EnrichedEvent` objects

**Requirements**: 2.1, 2.2, 2.3, 2.5, 5.1, 5.2, 5.3, 5.4, 6.1, 6.2, 6.3

### 4. Event Filter Function

**Component**: `EventFilterFunction` (RichFilterFunction)  
**Script Engine**: AviatorScript

**Functionality**:
- Loads filter script from configured path
- Compiles script on initialization
- Evaluates each enriched event against the script
- Provides script context with:
  - `event`: UserEvent object
  - `user`: UserProfile object
  - `visit`: Visit object
  - `history`: List of EventHistory
- Passes events that evaluate to `true`
- Filters out events that evaluate to `false`
- Rejects events on script execution errors (fail-safe)

**Requirements**: 3.1, 3.2, 3.3, 3.4, 3.5, 3.6

### 5. Action Handler (Event Action)

**Component**: `ActionHandler` (abstract class)  
**Implementations**:
- `DebugPrintActionHandler`: Logs events as formatted JSON
- `WebhookActionHandler`: Sends HTTP POST to webhook URL

**Functionality**:
- Executes configured action for each filtered event
- Selection based on configuration:
  - If `actions.debug.enabled = true`: uses DebugPrintActionHandler
  - Otherwise: uses WebhookActionHandler
- Errors are logged but don't interrupt processing

**Requirements**: 4.1, 4.2, 4.3, 4.4, 4.5, 4.6, 4.7

## Configuration

The pipeline is configured via `application.yml`:

```yaml
kafka:
  bootstrap-servers: localhost:9092
  topic: user-tracking-events
  consumer-group-id: flink-event-trigger

profile-api:
  base-url: http://localhost:8080
  timeout-ms: 5000

filter:
  script-path: ./scripts/filter.av

actions:
  webhook:
    url: http://localhost:8080/webhook
    timeout-ms: 3000
  debug:
    enabled: true

flink:
  parallelism: 4
  checkpoint-interval-ms: 60000
  state-backend: rocksdb
  state-ttl-minutes: 10
```

## Data Flow Example

1. **Event arrives** from Kafka:
   ```json
   {
     "event_id": "evt_123",
     "user_id": "user_456",
     "event_type": "page_view",
     "event_name": "home_page",
     ...
   }
   ```

2. **Deserialized** to `UserEvent` object

3. **Partitioned** by `user_id` = "user_456"

4. **Enriched** with user data:
   - Checks Flink State for user checkpoint
   - If not found, calls Profile API
   - Creates `EnrichedEvent` with user profile, visit, and history

5. **Filtered** by AviatorScript:
   ```javascript
   event.eventType == "page_view" && user.country == "US"
   ```

6. **Action executed**:
   - Debug mode: Logs enriched event as JSON
   - Webhook mode: Sends POST to webhook URL

## Error Handling

The pipeline implements "log and continue" error handling:

- **Deserialization errors**: Logged, event skipped
- **Profile API errors**: Logged, empty checkpoint created
- **Filter script errors**: Logged, event rejected
- **Webhook errors**: Logged, processing continues
- **State errors**: Handled by Flink's checkpoint mechanism

## State Management

- **Backend**: RocksDB (configurable to HashMap)
- **TTL**: 10 minutes of inactivity
- **Cleanup**: Automatic via Flink's TTL mechanism
- **Checkpointing**: Every 60 seconds (configurable)
- **Recovery**: Automatic from last successful checkpoint

## Performance Considerations

- **Parallelism**: Configurable (default: 4)
- **Partitioning**: By user ID ensures even distribution
- **State Backend**: RocksDB for large state, HashMap for small state
- **Checkpointing**: Balance between recovery time and overhead
- **API Calls**: Only on first event or after TTL expiration

## Testing

The pipeline includes comprehensive tests:

- **Unit tests**: 98 tests covering all components
- **Integration tests**: Pipeline assembly and configuration
- **Mock tests**: Profile API, Webhook, and State management

Run tests with:
```bash
mvn test
```

## Running the Application

1. Start Kafka (via Docker Compose):
   ```bash
   docker-compose up -d
   ```

2. Build the application:
   ```bash
   mvn clean package
   ```

3. Run the Flink job:
   ```bash
   java -jar target/flink-event-trigger-1.0-SNAPSHOT.jar
   ```

Or submit to Flink cluster:
```bash
flink run target/flink-event-trigger-1.0-SNAPSHOT.jar
```

## Monitoring

Key metrics to monitor:

- **Kafka lag**: Consumer group lag
- **Checkpoint duration**: Time to complete checkpoints
- **State size**: Size of user checkpoints
- **API latency**: Profile API response times
- **Filter pass rate**: Percentage of events passing filter
- **Action success rate**: Webhook success/failure rate
- **Error rates**: Deserialization, API, filter, webhook errors

## Future Enhancements

Potential improvements:

1. **Multiple action handlers**: Support multiple actions per event
2. **Dynamic filter updates**: Reload filter script without restart
3. **Metrics collection**: Expose Flink metrics for monitoring
4. **Backpressure handling**: Implement rate limiting for API calls
5. **Event replay**: Support replaying events from specific offset
