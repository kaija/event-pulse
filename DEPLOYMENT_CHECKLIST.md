# Deployment Checklist - Flink Event Trigger Framework

Use this checklist to ensure the system is properly deployed and configured.

## Pre-Deployment Verification

### 1. Build and Test ✅
- [x] All unit tests pass (`mvn test`)
- [x] Application builds successfully (`mvn clean package`)
- [x] JAR file created (71MB shaded JAR)
- [x] No compilation errors or critical warnings

### 2. Configuration Files ✅
- [x] `application.yml` configured with correct values
- [x] `docker-compose.yml` reviewed and validated
- [x] `scripts/filter.av` contains appropriate filter logic
- [x] Log4j configuration present (`log4j2.xml`)

### 3. Documentation ✅
- [x] `README.md` - Main documentation
- [x] `QUICK_START.md` - Quick start guide
- [x] `TESTING.md` - Testing procedures
- [x] `PIPELINE.md` - Architecture documentation
- [x] `INTEGRATION_TEST_REPORT.md` - Test results
- [x] `MOCK_API.md` - Mock API documentation
- [x] `scripts/FILTER_EXAMPLES.md` - Filter examples
- [x] `test-data/README.md` - Test data documentation

### 4. Scripts and Tools ✅
- [x] `start.sh` - Docker Compose startup script
- [x] `create-topic.sh` - Kafka topic creation
- [x] `send-test-event.sh` - Event producer tool
- [x] `start-mock-api.sh` - Mock API server
- [x] All scripts are executable (chmod +x)

## Deployment Steps

### Step 1: Environment Setup
- [ ] Docker installed and running
- [ ] Docker Compose installed
- [ ] Java 11+ installed
- [ ] Maven 3.6+ installed
- [ ] Network ports available:
  - [ ] 8080 (Mock API)
  - [ ] 8081 (Flink Web UI)
  - [ ] 9092 (Kafka)
  - [ ] 9093 (Kafka Controller)

### Step 2: Build Application
```bash
mvn clean package
```
- [ ] Build completes successfully
- [ ] JAR file exists: `target/flink-event-trigger-1.0-SNAPSHOT.jar`
- [ ] JAR size is approximately 71MB

### Step 3: Start Docker Environment
```bash
./start.sh
```
- [ ] Kafka container starts successfully
- [ ] Flink JobManager starts successfully
- [ ] Flink TaskManager starts successfully
- [ ] All health checks pass
- [ ] No error messages in logs

### Step 4: Verify Services
```bash
docker ps
```
- [ ] 3 containers running: kafka, flink-jobmanager, flink-taskmanager
- [ ] All containers show "healthy" status
- [ ] Flink Web UI accessible at http://localhost:8081
- [ ] Kafka responding on port 9092

### Step 5: Create Kafka Topic (if needed)
```bash
./create-topic.sh
```
- [ ] Topic `user-tracking-events` created
- [ ] Topic has 4 partitions
- [ ] Topic visible in Kafka

### Step 6: Start Mock API (Optional)
```bash
./start-mock-api.sh
```
- [ ] Mock API starts on port 8080
- [ ] API endpoints respond correctly:
  - [ ] GET /users/{userId}/profile
  - [ ] GET /users/{userId}/visit
  - [ ] GET /users/{userId}/history

### Step 7: Submit Flink Job
```bash
# Copy JAR to container
docker cp target/flink-event-trigger-1.0-SNAPSHOT.jar flink-jobmanager:/opt/flink/usrlib/

# Submit job
docker exec flink-jobmanager flink run /opt/flink/usrlib/flink-event-trigger-1.0-SNAPSHOT.jar
```
- [ ] Job submission succeeds
- [ ] Job appears in Flink Web UI
- [ ] Job status is "RUNNING"
- [ ] No errors in JobManager logs
- [ ] No errors in TaskManager logs

### Step 8: Send Test Events
```bash
./send-test-event.sh test-data/sample-event-1.json
```
- [ ] Event sent successfully
- [ ] Event appears in Kafka topic
- [ ] Event processed by Flink
- [ ] Output visible in TaskManager logs

### Step 9: Verify Event Processing
```bash
docker logs flink-taskmanager | grep "DEBUG EVENT"
```
- [ ] Events are being processed
- [ ] User checkpoints created
- [ ] Filter function executing
- [ ] Actions being triggered
- [ ] No error messages

## Post-Deployment Verification

### Functional Tests
- [ ] **Test 1**: Send single event, verify processing
- [ ] **Test 2**: Send multiple events for same user, verify state reuse
- [ ] **Test 3**: Send events for different users, verify parallel processing
- [ ] **Test 4**: Verify filter script works correctly
- [ ] **Test 5**: Verify webhook action (if configured)
- [ ] **Test 6**: Verify debug print action

### Performance Tests
- [ ] Send 100 events, verify all processed
- [ ] Check memory usage (docker stats)
- [ ] Check CPU usage
- [ ] Verify no memory leaks
- [ ] Check checkpoint completion times

### State Management Tests
- [ ] Verify user checkpoint creation
- [ ] Verify checkpoint persistence
- [ ] Verify TTL expiration (wait 11 minutes, send event)
- [ ] Verify state recovery after restart

### Error Handling Tests
- [ ] Send invalid JSON, verify error handling
- [ ] Stop Mock API, verify graceful degradation
- [ ] Send event with missing fields, verify handling
- [ ] Verify all errors are logged

## Configuration Checklist

### application.yml
- [ ] `kafka.bootstrap-servers` points to correct Kafka
- [ ] `kafka.topic` is correct topic name
- [ ] `profile-api.base-url` points to correct API
- [ ] `filter.script-path` points to correct script
- [ ] `actions.webhook.url` configured (if using webhooks)
- [ ] `flink.parallelism` set appropriately
- [ ] `flink.state-ttl-minutes` set to desired value

### docker-compose.yml
- [ ] Kafka image version correct (apache/kafka:4.0.0)
- [ ] Flink image version correct (flink:1.20-java11)
- [ ] Port mappings correct
- [ ] Volume mappings correct
- [ ] Network configuration correct
- [ ] Health checks configured

### Filter Script (scripts/filter.av)
- [ ] Script syntax is valid
- [ ] Script logic matches requirements
- [ ] Script tested with sample events
- [ ] Script handles edge cases

## Monitoring Setup

### Metrics to Monitor
- [ ] Event throughput (events/second)
- [ ] Processing latency
- [ ] Checkpoint duration
- [ ] State size
- [ ] Memory usage
- [ ] CPU usage
- [ ] Error rate
- [ ] API call success rate

### Logging
- [ ] Log level configured appropriately
- [ ] Logs being collected/aggregated
- [ ] Error logs being monitored
- [ ] Log rotation configured

### Alerts (if applicable)
- [ ] High error rate alert
- [ ] High latency alert
- [ ] Job failure alert
- [ ] Resource usage alert

## Rollback Plan

### If Deployment Fails
1. Stop Flink job:
   ```bash
   docker exec flink-jobmanager flink cancel <job-id>
   ```

2. Stop Docker services:
   ```bash
   docker-compose down
   ```

3. Review logs:
   ```bash
   docker logs kafka
   docker logs flink-jobmanager
   docker logs flink-taskmanager
   ```

4. Fix issues and redeploy

### If Job Fails
1. Check Flink Web UI for error details
2. Review TaskManager logs
3. Verify configuration
4. Cancel job and resubmit

## Production Considerations

### Security
- [ ] Kafka authentication configured (if needed)
- [ ] Flink security enabled (if needed)
- [ ] API authentication configured
- [ ] Network security rules in place
- [ ] Secrets management configured

### High Availability
- [ ] Multiple Kafka brokers (production)
- [ ] Flink HA mode configured (production)
- [ ] Checkpoint storage on distributed filesystem
- [ ] Backup and recovery procedures documented

### Scalability
- [ ] Kafka partitions match parallelism
- [ ] Flink parallelism set appropriately
- [ ] TaskManager resources adequate
- [ ] State backend configured for scale (RocksDB)

### Maintenance
- [ ] Backup procedures documented
- [ ] Upgrade procedures documented
- [ ] Monitoring dashboards created
- [ ] Runbook for common issues
- [ ] On-call procedures defined

## Sign-Off

### Development Team
- [ ] Code reviewed and approved
- [ ] Tests passed
- [ ] Documentation complete
- [ ] Signed off by: _________________ Date: _______

### Operations Team
- [ ] Infrastructure ready
- [ ] Monitoring configured
- [ ] Deployment tested
- [ ] Signed off by: _________________ Date: _______

### Product Team
- [ ] Requirements met
- [ ] Acceptance tests passed
- [ ] Ready for production
- [ ] Signed off by: _________________ Date: _______

## Notes

### Known Issues
- Property-based tests not implemented (optional, not critical)
- Docker environment testing pending (requires Docker running)

### Future Enhancements
- Implement property-based tests
- Add more integration tests
- Performance tuning for production
- Add metrics and monitoring

### Support Contacts
- Development Team: [contact info]
- Operations Team: [contact info]
- On-Call: [contact info]

---

**Deployment Date**: _________________  
**Deployed By**: _________________  
**Environment**: Development / Staging / Production  
**Version**: 1.0-SNAPSHOT
