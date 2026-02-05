# Using Mock Profile API Server with Flink Event Trigger

This guide shows how to use the Mock Profile API Server for testing the complete Flink Event Trigger system.

## Overview

The Mock Profile API Server simulates an external Profile API that provides:
- User profile data (country, city, language, timezone)
- Current visit information (device, browser, referrer)
- Event history (past events for the user)

This allows you to test the complete event processing pipeline without needing a real external API.

## Step-by-Step Testing Guide

### 1. Start the Mock Profile API Server

Open a terminal and start the mock server:

```bash
./start-mock-api.sh
```

You should see:
```
Mock Profile API Server started on port 8080
Available endpoints:
  GET http://localhost:8080/users/{userId}/profile
  GET http://localhost:8080/users/{userId}/visit
  GET http://localhost:8080/users/{userId}/history

Press Ctrl+C to stop the server
```

**Keep this terminal open** - the server needs to keep running.

### 2. Verify the Mock Server is Working

Open a new terminal and test the endpoints:

```bash
# Test user profile endpoint
curl http://localhost:8080/users/user123/profile

# Test visit endpoint
curl http://localhost:8080/users/user123/visit

# Test history endpoint
curl http://localhost:8080/users/user123/history
```

You should see JSON responses with sample data.

### 3. Start Docker Compose Environment

In the second terminal, start Kafka and Flink:

```bash
./start.sh
```

Wait for all services to start (about 30 seconds).

### 4. Compile and Submit Flink Job

```bash
# Compile the project
mvn clean package

# Copy JAR to Flink JobManager
docker cp target/flink-event-trigger-1.0-SNAPSHOT.jar flink-jobmanager:/opt/flink/usrlib/

# Submit the job
docker exec flink-jobmanager flink run /opt/flink/usrlib/flink-event-trigger-1.0-SNAPSHOT.jar
```

### 5. Send Test Events

Now send some test events to Kafka:

```bash
# Send a single event
./send-test-event.sh test-data/sample-event-1.json

# Wait a moment, then send another
./send-test-event.sh test-data/sample-event-2.json
```

### 6. Observe the Results

#### Check Flink Logs

Watch the Flink TaskManager logs to see event processing:

```bash
docker-compose logs -f flink-taskmanager
```

You should see:
1. **Profile API calls**: When a new user's first event arrives
2. **Event filtering**: AviatorScript evaluation
3. **Debug output**: Enriched events with user data

#### Check Mock API Server Logs

In the first terminal (where the mock server is running), you'll see HTTP requests:

```
GET /users/user123/profile - 200 OK
GET /users/user123/visit - 200 OK
GET /users/user123/history - 200 OK
```

This confirms that Flink is calling the Profile API to load user data.

### 7. Test Different Scenarios

#### Scenario A: New User (First Event)

Send an event for a new user:

```bash
./send-test-event.sh test-data/sample-event-1.json
```

**Expected behavior:**
- Flink calls all three Profile API endpoints
- User checkpoint is created
- Event is enriched with user data
- Event passes through filter (if conditions match)

#### Scenario B: Existing User (Subsequent Events)

Send another event for the same user:

```bash
./send-test-event.sh test-data/sample-event-2.json
```

**Expected behavior:**
- Flink loads checkpoint from state (no API calls)
- Event is enriched with cached user data
- Event passes through filter

#### Scenario C: Multiple Users

Send events for different users:

```bash
# User alice
echo '{"user_id":"alice","event_type":"pageview","event_name":"page_view","timestamp":'$(date +%s000)'}' | \
  docker exec -i kafka kafka-console-producer.sh \
    --bootstrap-server localhost:9092 \
    --topic user-tracking-events

# User bob
echo '{"user_id":"bob","event_type":"pageview","event_name":"page_view","timestamp":'$(date +%s000)'}' | \
  docker exec -i kafka kafka-console-producer.sh \
    --bootstrap-server localhost:9092 \
    --topic user-tracking-events
```

**Expected behavior:**
- Each user gets different profile data (based on userId hash)
- Separate checkpoints are maintained for each user
- You can see different countries/cities in the logs

### 8. Test State TTL (10 Minutes)

The system automatically cleans up user state after 10 minutes of inactivity.

To test this (in a real scenario):
1. Send an event for a user
2. Wait 10+ minutes
3. Send another event for the same user
4. The system should call the Profile API again (checkpoint expired)

**For quick testing**, you can modify the TTL in `application.yml`:

```yaml
flink:
  state-ttl-minutes: 1  # Change to 1 minute for testing
```

Then rebuild and resubmit the job.

## Troubleshooting

### Mock Server Not Responding

**Problem:** Flink can't connect to the mock server.

**Solution:**
1. Check if the mock server is running: `curl http://localhost:8080/users/test/profile`
2. Verify the port in `application.yml` matches (default: 8080)
3. Check firewall settings

### No Profile API Calls in Logs

**Problem:** You don't see Profile API calls in the mock server logs.

**Possible causes:**
1. User checkpoint already exists (send event for a new user)
2. Flink job not running (check `http://localhost:8081`)
3. Events not reaching Kafka (check Kafka consumer)

### Events Not Passing Filter

**Problem:** Events are filtered out and not processed.

**Solution:**
1. Check the filter script in `scripts/filter.av`
2. Modify the filter to be more permissive: `true` (allows all events)
3. Check Flink logs for filter evaluation errors

## Cleanup

When you're done testing:

1. Stop the Mock Profile API Server: Press `Ctrl+C` in the first terminal
2. Stop Docker Compose: `docker-compose down`
3. (Optional) Clean up volumes: `docker-compose down -v`

## Next Steps

- Modify the filter script to test different filtering logic
- Create custom test events with different user IDs
- Monitor Flink metrics in the Web UI
- Test webhook actions by setting up a webhook receiver

For more information:
- Mock API details: [`MOCK_API.md`](../MOCK_API.md)
- Filter examples: [`scripts/FILTER_EXAMPLES.md`](../scripts/FILTER_EXAMPLES.md)
- Testing guide: [`TESTING.md`](../TESTING.md)
