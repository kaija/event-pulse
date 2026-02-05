# Mock Profile API Server

This is a simple HTTP server that simulates the Profile API for testing purposes. It returns sample user data, visit information, and event history.

## Features

- **Lightweight**: Uses Java's built-in `HttpServer` - no external dependencies
- **Deterministic**: Generates consistent data based on userId hash
- **Variety**: Returns different data for different user IDs
- **Easy to use**: Simple command-line interface

## Endpoints

The mock server provides three endpoints:

### 1. Get User Profile
```
GET /users/{userId}/profile
```

Returns user profile information including:
- Country, city, language
- Continent and timezone

**Example Response:**
```json
{
  "user_id": "user123",
  "country": "US",
  "city": "New York",
  "language": "en",
  "continent": "NA",
  "timezone": "America/New_York"
}
```

### 2. Get Current Visit
```
GET /users/{userId}/visit
```

Returns current visit information including:
- Device, OS, browser details
- Landing page and referrer information
- Visit duration and action count

**Example Response:**
```json
{
  "uuid": "550e8400-e29b-41d4-a716-446655440000",
  "timestamp": 1704067200000,
  "os": "Windows 10",
  "browser": "Chrome",
  "device": "Desktop",
  "landing_page": "/home",
  "referrer_type": "search",
  "referrer_url": "https://www.google.com",
  "referrer_query": "search query",
  "duration": 120000,
  "actions": 5,
  "is_first_visit": false,
  "screen": "1920x1080",
  "ip": "192.168.1.100"
}
```

### 3. Get Event History
```
GET /users/{userId}/history
```

Returns a list of historical events (1-5 events per user).

**Example Response:**
```json
[
  {
    "event_id": "evt-123",
    "event_name": "page_view",
    "event_type": "pageview",
    "timestamp": 1704067200000,
    "parameters": {
      "custom_params": {
        "page": "/page0",
        "value": 10
      },
      "utm_source": "google",
      "utm_campaign": "summer_sale",
      "utm_medium": "cpc",
      "utm_term": "shoes",
      "utm_id": "campaign123"
    }
  }
]
```

## Usage

### Starting the Server

**Linux/Mac:**
```bash
./start-mock-api.sh [port]
```

**Windows:**
```cmd
start-mock-api.bat [port]
```

**Default port:** 8080

**Example with custom port:**
```bash
./start-mock-api.sh 9090
```

### Using Maven Directly

You can also run the server directly with Maven:

```bash
mvn compile exec:java -Dexec.mainClass="com.example.flink.tools.MockProfileApiServer" -Dexec.args="8080"
```

### Testing the Endpoints

Once the server is running, you can test it with curl:

```bash
# Get user profile
curl http://localhost:8080/users/user123/profile

# Get current visit
curl http://localhost:8080/users/user123/visit

# Get event history
curl http://localhost:8080/users/user123/history
```

Or open in your browser:
- http://localhost:8080/users/user123/profile
- http://localhost:8080/users/user123/visit
- http://localhost:8080/users/user123/history

## Configuration

To use this mock server with the Flink application, update your `application.yml`:

```yaml
profile-api:
  base-url: http://localhost:8080
  timeout-ms: 5000
  retry-attempts: 3
```

## Data Generation

The mock server generates deterministic data based on the userId hash:
- Same userId always returns the same data
- Different userIds return varied data for realistic testing
- Event history contains 1-5 events per user
- Visit information includes realistic device/browser combinations

## Stopping the Server

Press `Ctrl+C` to stop the server gracefully.

## Implementation Details

- **Technology**: Java HttpServer (built-in, no external dependencies)
- **Port**: Configurable (default: 8080)
- **Thread Model**: Uses default executor (multi-threaded)
- **Response Format**: JSON
- **Error Handling**: Returns appropriate HTTP status codes

## Requirements

This mock server satisfies **Requirement 2.2**: Profile API integration for testing purposes.
