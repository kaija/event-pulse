#!/bin/bash

# Test script to send 3 page_view events in sequence
# This should trigger the debug print on the 3rd page_view

set -e

echo "=== Testing 3rd Page View Detection ==="
echo ""

KAFKA_CONTAINER="kafka"
TOPIC="user-tracking-events"

# Check if Kafka is running
if ! docker ps | grep -q $KAFKA_CONTAINER; then
    echo "ERROR: Kafka container is not running"
    echo "Please start the environment with: docker-compose up -d"
    exit 1
fi

echo "Sending page_view event 1/3..."
cat test-data/sample-event-pageview-1.json | docker exec -i $KAFKA_CONTAINER /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic $TOPIC
echo "✓ Sent page_view 1"
sleep 2

echo ""
echo "Sending page_view event 2/3..."
cat test-data/sample-event-pageview-2.json | docker exec -i $KAFKA_CONTAINER /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic $TOPIC
echo "✓ Sent page_view 2"
sleep 2

echo ""
echo "Sending page_view event 3/3 (should trigger debug print)..."
cat test-data/sample-event-pageview-3.json | docker exec -i $KAFKA_CONTAINER /opt/kafka/bin/kafka-console-producer.sh --bootstrap-server localhost:9092 --topic $TOPIC
echo "✓ Sent page_view 3"

echo ""
echo "=== All events sent! ==="
echo ""
echo "Check the Flink TaskManager logs for the debug output:"
echo "  docker logs flink-taskmanager -f"
echo ""
echo "You should see a log message indicating the 3rd page_view was detected."
