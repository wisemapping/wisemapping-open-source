# Spam User Suspension by Type

This document shows how to suspend users who have public maps marked as spam by specific detection strategies.

## Overview

The system now supports suspending users based on the specific type of spam detected in their public maps. The `spam_type_code` field in the `MINDMAP_SPAM_INFO` table stores which strategy detected the spam.

## Available Spam Types

- **FewNodesWithContent**: Maps with few nodes that contain links or notes (too simple)
- **UserBehavior**: Maps created/modified in suspicious patterns (behavior detection)

## Usage Examples

### 1. Suspend users with "too simple" spam maps

```java
@Autowired
private SpamUserSuspensionService spamUserSuspensionService;

// Suspend users with public maps marked as "too simple"
String[] spamTypes = {"FewNodesWithContent"};
int suspendedCount = spamUserSuspensionService.suspendUsersWithPublicSpamMapsByType(
    spamTypes, 
    3, // months back
    SuspensionReason.ABUSE
);
```

### 2. Suspend users with "behavior detection" spam maps

```java
// Suspend users with public maps marked as "behavior detection"
String[] behaviorSpamTypes = {"UserBehavior"};
int behaviorSuspendedCount = spamUserSuspensionService.suspendUsersWithPublicSpamMapsByType(
    behaviorSpamTypes, 
    3, // months back
    SuspensionReason.ABUSE
);
```

### 3. Suspend users with either type of spam

```java
// Suspend users with either type of spam
String[] allSpamTypes = {"FewNodesWithContent", "UserBehavior"};
int allSuspendedCount = spamUserSuspensionService.suspendUsersWithPublicSpamMapsByType(
    allSpamTypes, 
    3, // months back
    SuspensionReason.ABUSE
);
```

## How It Works

1. **Spam Detection**: When spam is detected, the `spam_type_code` field is set to the strategy name (e.g., "FewNodesWithContent", "UserBehavior")

2. **User Finding**: The system queries for users who have public maps with specific spam types

3. **Suspension**: Users are suspended with the specified reason (e.g., `SuspensionReason.ABUSE`)

4. **Batch Processing**: The operation is performed in batches to avoid memory issues

## Database Queries

The system uses these queries to find users:

```sql
-- Find users with specific spam types
SELECT m.creator, COUNT(m.id) as spamCount 
FROM MINDMAP m 
JOIN MINDMAP_SPAM_INFO s ON m.id = s.mindmap_id
WHERE s.spam_detected = true 
  AND m.public = true 
  AND m.creator.creation_date >= :cutoffDate 
  AND s.spam_type_code IN ('FewNodesWithContent', 'UserBehavior')
GROUP BY m.creator 
ORDER BY m.creator.id
```

## Configuration

The suspension service respects these configuration properties:

- `app.batch.spam-user-suspension.enabled`: Enable/disable the service
- `app.batch.spam-user-suspension.batch-size`: Number of users to process per batch
- `app.batch.spam-user-suspension.months-back`: How many months back to look for account creation

## Logging

The service provides detailed logging:

- Info level: Overall progress and counts
- Warn level: Individual user suspensions
- Debug level: Batch processing details
- Error level: Any errors during processing
