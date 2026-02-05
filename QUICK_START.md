# Quick Start Guide - Flink Event Trigger Framework

This guide will help you get the Flink Event Trigger Framework up and running in 5 minutes.

## Prerequisites

- ✅ Java 11 or higher
- ✅ Maven 3.6 or higher
- ✅ Docker and Docker Compose

## Step-by-Step Setup

### Step 1: Verify Prerequisites

```bash
# Check Java version
java -version
# Should show Java 11 or higher

# Check Maven version
mvn -version
# Should show Maven 3.6 or higher

# Check Docker
docker --version
docker-compose --version
```

### Step 2: Build the Project

```bash
# Build and package the application
mvn clean package

# Verify the JAR file was created
ls -lh target/flink-event-trigger-1.0-SNAPSHOT.jar
# Should show a ~71MB file
```

### Step 3: Start Docker Environment

```bash
# Start all services (Kafka, Flink JobManager, Flink TaskManager)
./start.sh

# Wait for services to be ready (about 30-60 seconds)
# The script will show you when services are ready
```

**What this does**:
- Starts Kafka in KRaft mode (no Zookeeper needed)
- Starts Flink JobManager (Web UI at http://localhost:8081)
- Starts Flink TaskManager (4 task slots)

### Step 4: Start Mock Profile API (Optional but Recommended)

```bash
# Start the Mock Profile API server on port 8080
./start-mock-api.sh

# Or use a custom port
./start-mock-api.sh 9090
```

**What this does**:
- Provides mock user profiles, visit data, and event history
- Allows testing without a real Profile API
- See `MOCK_API.md` for details

### Step 5: Submit Flink Job

```bash
# Copy JAR to Flink JobManager container
docker cp target/flink-event-trigger-1.0-SNAPSHOT.jar flink-jobmanager:/opt/flink/usrlib/

# Submit the job
docker exec flink-jobmanager flink run /opt/flink/usrlib/flink-event-trigger-1.0-SNAPSHOT.jar
```

**Verify the job is running**:
- Open http://localhost:8081 in your browser
- You should see the "Flink Event Trigger" job running

### Step 6: Send Test Events

```bash
# Send a single test event
./send-test-event.sh test-data/sample-event-1.json

# Send all test events with 1 second delay between each
./send-test-event.sh test-data/ 1000
```

### Step 7: Verify Events Are Processed

```bash
# View Flink TaskManager logs (shows event processing)
docker logs flink-taskmanager

# View Debug Print output
docker logs flink-taskmanager | grep "DEBUG EVENT"

# View Kafka messages
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-tracking-events \
  --from-beginning
```

## What You Should See

### 1. Flink Web UI (http://localhost:8081)
- Job name: "Flink Event Trigger"
- Status: RUNNING
- Task Managers: 1
- Task Slots: 4

### 2. TaskManager Logs
```
INFO  UserStateManager - Created new checkpoint for userId: user_123
INFO  EventFilterFunction - Event passed filter: event_001
INFO  DebugPrintActionHandler - DEBUG EVENT:
{
  "event": {
    "eventId": "event_001",
    "userId": "user_123",
    ...
  },
  "userProfile": {
    "userId": "user_123",
    "country": "US",
    ...
  }
}
```

### 3. Mock API Logs
```
GET /users/user_123/profile - 200 OK
GET /users/user_123/visit - 200 OK
GET /users/user_123/history - 200 OK
```

## Common Commands

### View Service Status
```bash
docker ps
```

### View Logs
```bash
# Kafka logs
docker logs kafka

# Flink JobManager logs
docker logs flink-jobmanager

# Flink TaskManager logs
docker logs flink-taskmanager

# Follow logs in real-time
docker logs -f flink-taskmanager
```

### Stop Services
```bash
# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Restart Services
```bash
# Restart all services
docker-compose restart

# Restart specific service
docker-compose restart kafka
```

### Cancel Flink Job
```bash
# List running jobs
docker exec flink-jobmanager flink list

# Cancel a job (use job ID from list command)
docker exec flink-jobmanager flink cancel <job-id>
```

## Testing Scenarios

### Scenario 1: Single User Journey
Test state management by sending multiple events for the same user:

```bash
./send-test-event.sh test-data/sample-event-1.json
sleep 2
./send-test-event.sh test-data/sample-event-2.json
sleep 2
./send-test-event.sh test-data/sample-event-purchase.json
```

**Expected**: First event creates checkpoint, subsequent events reuse it.

### Scenario 2: Multiple Users
Test parallel processing:

```bash
./send-test-event.sh test-data/ 0
```

**Expected**: Events for different users processed in parallel.

### Scenario 3: Custom Filter
Edit `scripts/filter.av` to filter events:

```javascript
// Only allow page view events from Taiwan
event.eventType == "pageview" && user.country == "TW"
```

Then restart the Flink job and send events.

## Troubleshooting

### Problem: Docker services won't start
**Solution**:
```bash
# Clean up and restart
docker-compose down -v
./start.sh
```

### Problem: Flink job submission fails
**Solution**:
```bash
# Check if JobManager is running
docker ps | grep flink-jobmanager

# Check JobManager logs
docker logs flink-jobmanager

# Verify JAR file exists
ls -lh target/flink-event-trigger-1.0-SNAPSHOT.jar
```

### Problem: Events not being processed
**Solution**:
```bash
# 1. Check if job is running
docker exec flink-jobmanager flink list

# 2. Check TaskManager logs for errors
docker logs flink-taskmanager

# 3. Verify Kafka is receiving messages
docker exec -it kafka kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic user-tracking-events \
  --from-beginning
```

### Problem: Mock API not responding
**Solution**:
```bash
# Check if Mock API is running
ps aux | grep MockProfileApiServer

# Restart Mock API
./start-mock-api.sh
```

## Next Steps

1. **Read the Documentation**:
   - `README.md` - Complete project documentation
   - `TESTING.md` - Detailed testing guide
   - `PIPELINE.md` - Architecture and data flow
   - `scripts/FILTER_EXAMPLES.md` - Filter script examples

2. **Customize the Configuration**:
   - Edit `src/main/resources/application.yml`
   - Modify `scripts/filter.av` for custom filtering
   - Update Docker Compose settings if needed

3. **Create Custom Events**:
   - See `test-data/README.md` for event format
   - Create your own JSON event files
   - Send them with `./send-test-event.sh`

4. **Monitor and Debug**:
   - Use Flink Web UI for job monitoring
   - Check logs for debugging
   - Use Debug Print action for event inspection

## Support

For more detailed information, see:
- `README.md` - Main documentation
- `TESTING.md` - Testing guide
- `INTEGRATION_TEST_REPORT.md` - Test results
- `.kiro/specs/flink-event-trigger/` - Design specifications

## Summary

You now have a fully functional Flink Event Trigger Framework running! 🎉

The system is:
- ✅ Consuming events from Kafka
- ✅ Managing user state with Flink Keyed State
- ✅ Filtering events with AviatorScript
- ✅ Executing actions (Debug Print / Webhook)
- ✅ Automatically cleaning up idle user state (10 minute TTL)

Happy event processing! 🚀
