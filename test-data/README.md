# Test Data - Sample Events

This directory contains sample event JSON files for testing the Flink Event Trigger application.

## Sample Events

### 1. sample-event-1.json
- **Event Type**: Page View
- **User ID**: user_123
- **Description**: First page view event for a user arriving from Google search
- **Features**: 
  - First event in visit
  - Contains UTM parameters
  - Web platform

### 2. sample-event-2.json
- **Event Type**: Button Click (Add to Cart)
- **User ID**: user_123
- **Description**: User clicks "Add to Cart" button for running shoes
- **Features**:
  - Contains product information
  - Same user as event 1 (continuation of visit)
  - Custom parameters with product details

### 3. sample-event-3.json
- **Event Type**: Page View
- **User ID**: user_456
- **Description**: Mobile app user viewing product details
- **Features**:
  - Different user (user_456)
  - Mobile platform (iOS)
  - Facebook campaign tracking

### 4. sample-event-4.json
- **Event Type**: Form Submit
- **User ID**: user_789
- **Description**: Contact form submission
- **Features**:
  - New user (user_789)
  - Both first and last event in visit
  - No UTM parameters (direct traffic)

### 5. sample-event-purchase.json
- **Event Type**: Purchase/Transaction
- **User ID**: user_123
- **Description**: Completed purchase with order details
- **Features**:
  - Same user as events 1 and 2 (conversion)
  - Last event in visit
  - Contains order and payment information
  - Multiple items in cart

## Event Structure

All events follow the UserEvent model structure:

```json
{
  "event_id": "unique_event_identifier",
  "user_id": "user_identifier",
  "domain": "example.com",
  "is_first_in_visit": boolean,
  "is_last_in_visit": boolean,
  "is_first_event": boolean,
  "is_current": boolean,
  "event_name": "event_name",
  "event_displayname": "Display Name",
  "integration": "web|mobile",
  "app": "app_name",
  "platform": "web|ios|android",
  "is_https": boolean,
  "event_type": "pageview|click|form|transaction",
  "duration": milliseconds,
  "timestamp": unix_timestamp_ms,
  "triggerable": boolean,
  "parameters": {
    "custom_params": {
      // Custom event-specific parameters
    },
    "utm_source": "traffic_source",
    "utm_campaign": "campaign_name",
    "utm_content": "ad_content",
    "utm_medium": "medium_type",
    "utm_term": "search_term",
    "utm_id": "campaign_id"
  }
}
```

## Usage Scenarios

### Scenario 1: User Journey (user_123)
1. **sample-event-1.json**: User arrives from Google search
2. **sample-event-2.json**: User adds product to cart
3. **sample-event-purchase.json**: User completes purchase

This simulates a complete conversion funnel.

### Scenario 2: Mobile User (user_456)
- **sample-event-3.json**: Mobile app user viewing product

This tests mobile platform handling.

### Scenario 3: Quick Interaction (user_789)
- **sample-event-4.json**: User submits form and leaves

This tests single-event visits.

## Sending Events to Kafka

Use the KafkaEventProducer tool to send these events:

```bash
# Send a single event
java -cp target/flink-event-trigger-1.0-SNAPSHOT.jar \
  com.example.flink.tools.KafkaEventProducer \
  localhost:9092 user-tracking-events test-data/sample-event-1.json

# Send all events from directory with 1 second delay between events
java -cp target/flink-event-trigger-1.0-SNAPSHOT.jar \
  com.example.flink.tools.KafkaEventProducer \
  localhost:9092 user-tracking-events test-data/ 1000
```

Or use the convenience script:

```bash
# Send a single event
./send-test-event.sh test-data/sample-event-1.json

# Send all events
./send-test-event.sh test-data/
```

## Creating Custom Events

To create your own test events:

1. Copy one of the sample files
2. Modify the fields as needed
3. Ensure all required fields are present
4. Use valid JSON format
5. Save with `.json` extension in this directory

## Testing Tips

- Use different `user_id` values to test state management for multiple users
- Set `is_first_event: true` to trigger Profile API calls
- Vary `event_type` to test different filter conditions
- Use `triggerable: false` to test event filtering
- Add custom parameters to test AviatorScript filter logic
