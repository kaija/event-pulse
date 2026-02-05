#!/bin/bash

# Send Test Event Script
# This script sends test events to Kafka using the KafkaEventProducer tool
#
# Usage:
#   ./send-test-event.sh [json-file-or-directory] [delay-ms]
#
# Examples:
#   ./send-test-event.sh test-data/sample-event-1.json
#   ./send-test-event.sh test-data/ 1000

set -e

# Default configuration
BOOTSTRAP_SERVERS="${KAFKA_BOOTSTRAP_SERVERS:-localhost:9092}"
TOPIC="${KAFKA_TOPIC:-user-tracking-events}"
JAR_FILE="target/flink-event-trigger-1.0-SNAPSHOT.jar"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check if JAR file exists
if [ ! -f "$JAR_FILE" ]; then
    echo -e "${RED}Error: JAR file not found: $JAR_FILE${NC}"
    echo "Please build the project first: mvn clean package"
    exit 1
fi

# Check arguments
if [ $# -lt 1 ]; then
    echo "Usage: $0 <json-file-or-directory> [delay-ms]"
    echo ""
    echo "Examples:"
    echo "  $0 test-data/sample-event-1.json"
    echo "  $0 test-data/ 1000"
    echo ""
    echo "Environment variables:"
    echo "  KAFKA_BOOTSTRAP_SERVERS (default: localhost:9092)"
    echo "  KAFKA_TOPIC (default: user-tracking-events)"
    exit 1
fi

PATH_ARG="$1"
DELAY_MS="${2:-0}"

# Check if path exists
if [ ! -e "$PATH_ARG" ]; then
    echo -e "${RED}Error: Path does not exist: $PATH_ARG${NC}"
    exit 1
fi

echo -e "${GREEN}=== Kafka Event Producer ===${NC}"
echo "Bootstrap Servers: $BOOTSTRAP_SERVERS"
echo "Topic: $TOPIC"
echo "Path: $PATH_ARG"
if [ "$DELAY_MS" -gt 0 ]; then
    echo "Delay: ${DELAY_MS}ms between events"
fi
echo ""

# Run the producer
echo -e "${YELLOW}Sending events...${NC}"
java -cp "$JAR_FILE" \
    com.example.flink.tools.KafkaEventProducer \
    "$BOOTSTRAP_SERVERS" \
    "$TOPIC" \
    "$PATH_ARG" \
    "$DELAY_MS"

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✓ Events sent successfully${NC}"
else
    echo -e "${RED}✗ Failed to send events${NC}"
    exit 1
fi
