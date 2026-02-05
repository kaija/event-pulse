# AviatorScript Event Filter Examples

## Overview

This document provides examples and documentation for the AviatorScript event filter used in the Flink Event Trigger system. The filter script (`filter.av`) determines which events should pass through the system and trigger actions.

## How It Works

The filter script is evaluated for each enriched event. The script should return:
- `true` - to allow the event to pass through and trigger actions
- `false` - to filter out the event

## Available Variables

The filter script has access to the following variables:

### 1. `event` - UserEvent Object

The current event being processed.

**Available Fields:**
- `eventId` (String) - Unique event identifier
- `userId` (String) - User identifier
- `domain` (String) - Domain where event occurred
- `eventName` (String) - Name of the event (e.g., "purchase", "page_view")
- `eventType` (String) - Type of event (e.g., "page_view", "click")
- `timestamp` (long) - Event timestamp in milliseconds
- `duration` (long) - Event duration in milliseconds
- `platform` (String) - Platform (e.g., "web", "mobile")
- `app` (String) - Application identifier
- `integration` (String) - Integration source
- `isFirstInVisit` (boolean) - First event in visit
- `isLastInVisit` (boolean) - Last event in visit
- `isFirstEvent` (boolean) - First event ever for user
- `isCurrent` (boolean) - Is current event
- `isHttps` (boolean) - Event occurred over HTTPS
- `triggerable` (boolean) - Event is triggerable
- `parameters` (EventParameters) - Event parameters (see below)

### 2. `user` - UserProfile Object

User profile information.

**Available Fields:**
- `userId` (String) - User identifier
- `country` (String) - User's country (e.g., "US", "UK")
- `city` (String) - User's city
- `language` (String) - User's language (e.g., "en", "es")
- `continent` (String) - User's continent (e.g., "North America", "Europe")
- `timezone` (String) - User's timezone (e.g., "America/New_York")

### 3. `visit` - Visit Object

Current visit/session information.

**Available Fields:**
- `uuid` (String) - Visit unique identifier
- `timestamp` (long) - Visit start timestamp
- `os` (String) - Operating system (e.g., "iOS", "Windows")
- `browser` (String) - Browser name (e.g., "Chrome", "Firefox")
- `device` (String) - Device type (e.g., "mobile", "desktop", "tablet")
- `landingPage` (String) - Landing page URL
- `referrerType` (String) - Referrer type (e.g., "search", "direct", "social")
- `referrerUrl` (String) - Referrer URL
- `referrerQuery` (String) - Referrer query string
- `duration` (long) - Visit duration in milliseconds
- `actions` (int) - Number of actions in visit
- `isFirstVisit` (boolean) - Is user's first visit
- `screen` (String) - Screen resolution
- `ip` (String) - IP address

### 4. `history` - List<EventHistory>

List of user's previous events.

**Each EventHistory has:**
- `eventId` (String) - Event identifier
- `eventName` (String) - Event name
- `eventType` (String) - Event type
- `timestamp` (long) - Event timestamp
- `parameters` (EventParameters) - Event parameters

### 5. Event Parameters

Accessed via `event.parameters` or `history[i].parameters`

**Available Fields:**
- `customParams` (Map<String, Object>) - Custom parameters
- `utmSource` (String) - UTM source parameter
- `utmCampaign` (String) - UTM campaign parameter
- `utmContent` (String) - UTM content parameter
- `utmMedium` (String) - UTM medium parameter
- `utmTerm` (String) - UTM term parameter
- `utmId` (String) - UTM ID parameter

## Filter Examples

### Basic Examples

#### Example 1: Pass All Events (Default)
```aviator
true
```

#### Example 2: Filter by Event Type
```aviator
event.eventType == "page_view"
```

#### Example 3: Filter by User Country
```aviator
user.country == "US"
```

#### Example 4: Filter by Event Name
```aviator
event.eventName == "purchase"
```

### Combining Conditions

#### Example 5: Event Name AND User Language
```aviator
event.eventName == "purchase" && user.language == "en"
```

#### Example 6: Multiple Countries
```aviator
user.country == "US" || user.country == "CA" || user.country == "UK"
```

#### Example 7: Complex Multi-Condition Filter
```aviator
user.country == "US" && 
visit != nil && visit.device == "mobile" && 
event.eventType == "page_view"
```

### Visit-Based Filters

#### Example 8: Filter by Device Type
```aviator
visit != nil && visit.device == "mobile"
```

#### Example 9: Filter First-Time Visitors
```aviator
visit != nil && visit.isFirstVisit == true
```

#### Example 10: Filter by Browser
```aviator
visit != nil && (visit.browser == "Chrome" || visit.browser == "Firefox")
```

#### Example 11: Filter by Referrer Type
```aviator
visit != nil && visit.referrerType == "search"
```

#### Example 12: Filter by Visit Actions Count
```aviator
visit != nil && visit.actions >= 3
```

### Parameter-Based Filters

#### Example 13: Filter by UTM Source
```aviator
event.parameters != nil && event.parameters.utmSource == "google"
```

#### Example 14: Filter by UTM Campaign
```aviator
event.parameters != nil && event.parameters.utmCampaign == "summer_sale"
```

#### Example 15: Filter by Custom Parameter
```aviator
event.parameters != nil && 
event.parameters.customParams != nil &&
event.parameters.customParams.get("category") == "electronics"
```

### History-Based Filters

#### Example 16: Filter Users with Event History
```aviator
history != nil && count(history) > 0
```

#### Example 17: Filter Users with Sufficient History
```aviator
history != nil && count(history) >= 5
```

#### Example 18: Filter Users with Recent Activity
```aviator
history != nil && count(history) > 0 && 
(event.timestamp - history[0].timestamp) < 3600000
```

### Event Property Filters

#### Example 19: Filter by Event Duration
```aviator
event.duration > 5000
```

#### Example 20: Filter HTTPS Events Only
```aviator
event.isHttps == true
```

#### Example 21: Filter by Platform
```aviator
event.platform == "web"
```

#### Example 22: Filter Triggerable Events
```aviator
event.triggerable == true
```

### Geographic Filters

#### Example 23: Filter by Timezone
```aviator
user.timezone != nil && string.contains(user.timezone, "America")
```

#### Example 24: Filter by Continent
```aviator
user.continent == "Europe" || user.continent == "Asia"
```

#### Example 25: Filter by City
```aviator
user.city == "New York" || user.city == "Los Angeles"
```

### Advanced Examples

#### Example 26: Filter by Time of Day
```aviator
let hour = event.timestamp / 3600000 % 24;
hour >= 9 && hour <= 17
```

#### Example 27: Filter Weekend Events
```aviator
let dayOfWeek = (event.timestamp / 86400000 + 4) % 7;
dayOfWeek == 0 || dayOfWeek == 6
```

#### Example 28: Filter High-Value Mobile Users
```aviator
user.country == "US" &&
visit != nil && visit.device == "mobile" &&
history != nil && count(history) >= 10 &&
event.eventName == "purchase"
```

#### Example 29: Filter Returning Users from Search
```aviator
visit != nil && 
visit.isFirstVisit == false &&
visit.referrerType == "search" &&
event.eventType == "page_view"
```

#### Example 30: Filter Events with Specific Pattern
```aviator
event.eventName != nil && 
(string.contains(event.eventName, "click") || 
 string.contains(event.eventName, "view"))
```

## AviatorScript Functions

AviatorScript provides several built-in functions:

- `count(list)` - Returns the size of a list
- `string.contains(str, substr)` - Checks if string contains substring
- `string.startsWith(str, prefix)` - Checks if string starts with prefix
- `string.endsWith(str, suffix)` - Checks if string ends with suffix
- `string.length(str)` - Returns string length
- `math.abs(n)` - Absolute value
- `math.sqrt(n)` - Square root
- `math.pow(base, exp)` - Power function

## Null Safety

Always check for `nil` (null) before accessing nested properties:

```aviator
visit != nil && visit.device == "mobile"
```

```aviator
event.parameters != nil && event.parameters.utmSource == "google"
```

## Modifying the Filter

To change the filter behavior:

1. Edit `scripts/filter.av`
2. Replace the content with your desired filter expression
3. Restart the Flink application to load the new filter

**Example:**
```bash
echo 'user.country == "US" && event.eventType == "purchase"' > scripts/filter.av
```

## Testing Filters

You can test filter expressions using the `FilterScriptIntegrationTest` class:

```bash
mvn test -Dtest=FilterScriptIntegrationTest
```

## Requirements

This filter implementation satisfies:
- **Requirement 3.2**: Filter function loads AviatorScript filter script
- **Requirement 3.3**: Filter function executes AviatorScript to evaluate events

## Additional Resources

- [AviatorScript Documentation](https://github.com/killme2008/aviatorscript)
- [AviatorScript Language Guide](https://github.com/killme2008/aviatorscript/wiki)
