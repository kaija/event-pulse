#!/bin/bash

# Deploy Flink Event Trigger Job to Flink Cluster
# This script waits for Flink JobManager to be ready and then submits the job

set -e

echo "=== Flink Job Deployment Script ==="

# Configuration
FLINK_JOBMANAGER_HOST="${FLINK_JOBMANAGER_HOST:-flink-jobmanager}"
FLINK_JOBMANAGER_PORT="${FLINK_JOBMANAGER_PORT:-8081}"
JAR_PATH="${JAR_PATH:-/opt/flink/usrlib/flink-event-trigger.jar}"
MAX_RETRIES=30
RETRY_INTERVAL=5

echo "Flink JobManager: http://${FLINK_JOBMANAGER_HOST}:${FLINK_JOBMANAGER_PORT}"
echo "JAR Path: ${JAR_PATH}"

# Wait for Flink JobManager to be ready
echo "Waiting for Flink JobManager to be ready..."
for i in $(seq 1 $MAX_RETRIES); do
    if curl -s "http://${FLINK_JOBMANAGER_HOST}:${FLINK_JOBMANAGER_PORT}/overview" > /dev/null 2>&1; then
        echo "Flink JobManager is ready!"
        break
    fi
    
    if [ $i -eq $MAX_RETRIES ]; then
        echo "ERROR: Flink JobManager did not become ready after ${MAX_RETRIES} attempts"
        exit 1
    fi
    
    echo "Attempt $i/${MAX_RETRIES}: Flink JobManager not ready yet, waiting ${RETRY_INTERVAL}s..."
    sleep $RETRY_INTERVAL
done

# Wait a bit more to ensure TaskManager is also registered
echo "Waiting for TaskManager to register..."
sleep 10

# Check if TaskManager is registered
TASKMANAGERS=$(curl -s "http://${FLINK_JOBMANAGER_HOST}:${FLINK_JOBMANAGER_PORT}/taskmanagers" | grep -o '"taskmanagers":\[' | wc -l)
if [ "$TASKMANAGERS" -eq 0 ]; then
    echo "WARNING: No TaskManagers registered yet, waiting more..."
    sleep 10
fi

# Wait for Kafka to be ready (simple wait since auto-create is enabled)
echo "Waiting for Kafka to be ready..."
sleep 10
echo "✓ Kafka should be ready (topic will be auto-created on first use)"

# Check if job is already running
echo "Checking for existing jobs..."
RUNNING_JOBS=$(curl -s "http://${FLINK_JOBMANAGER_HOST}:${FLINK_JOBMANAGER_PORT}/jobs" | grep -o '"jobs":\[' | wc -l)
if [ "$RUNNING_JOBS" -gt 0 ]; then
    echo "WARNING: There are already running jobs. Checking if our job is running..."
    # You might want to add logic here to check if the specific job is running
fi

# Submit the Flink job
echo "Submitting Flink job..."
if [ ! -f "$JAR_PATH" ]; then
    echo "ERROR: JAR file not found at ${JAR_PATH}"
    exit 1
fi

# Use flink run command to submit the job with explicit JobManager address
flink run -m ${FLINK_JOBMANAGER_HOST}:${FLINK_JOBMANAGER_PORT} -d "$JAR_PATH"

if [ $? -eq 0 ]; then
    echo "✓ Flink job submitted successfully!"
    echo "You can monitor the job at: http://localhost:${FLINK_JOBMANAGER_PORT}"
else
    echo "✗ Failed to submit Flink job"
    exit 1
fi

echo "=== Deployment Complete ==="
