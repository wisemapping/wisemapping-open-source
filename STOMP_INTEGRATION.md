# STOMP WebSocket Integration for WiseMapping

This document describes the STOMP (Simple Text Oriented Messaging Protocol) WebSocket integration added to the WiseMapping backend for real-time collaborative mind map editing.

## Overview

STOMP integration enables real-time communication between multiple users editing the same mind map simultaneously. This includes:

- **Real-time updates**: Changes made by one user are instantly visible to others
- **User presence**: See who else is currently editing the mind map
- **Cursor tracking**: Visual indicators showing where other users are working
- **Typing indicators**: See when other users are making changes
- **Private messaging**: Direct communication between users

## Architecture

### Components

1. **WebSocketConfig**: Configures STOMP endpoints and message brokers
2. **StompController**: Handles incoming STOMP messages
3. **StompSessionService**: Manages user sessions and presence
4. **StompEventListener**: Handles connection/disconnection events
5. **StompInfoController**: REST endpoints for session information
6. **WebSocketSecurityConfig**: Security configuration for WebSocket connections

### Message Types

#### MindmapUpdateMessage
```json
{
  "userId": "user123",
  "mindmapId": "map456",
  "operation": "update",
  "elementId": "topic789",
  "elementType": "topic",
  "data": "{\"text\": \"Updated topic text\"}",
  "timestamp": 1640995200000,
  "version": "1.2.3"
}
```

#### UserPresenceMessage
```json
{
  "userId": "user123",
  "action": "join",
  "mindmapId": "map456",
  "userDisplayName": "John Doe",
  "userColor": "#FF5733",
  "timestamp": 1640995200000,
  "sessionId": "session789"
}
```

## WebSocket Endpoints

### Connection Endpoints

- **SockJS**: `/ws` - Main WebSocket endpoint with SockJS fallback
- **Native**: `/ws-native` - Native WebSocket endpoint

### Message Destinations

#### Topics (Broadcast to all subscribers)
- `/topic/mindmap/{mindmapId}/updates` - Mind map updates
- `/topic/mindmap/{mindmapId}/presence` - User presence changes
- `/topic/mindmap/{mindmapId}/cursor` - Cursor position updates
- `/topic/mindmap/{mindmapId}/typing` - Typing indicators

#### Queues (User-specific messages)
- `/user/queue/private` - Private messages to specific users

#### Application Destinations (Client sends to)
- `/app/mindmap/{mindmapId}/command` - Send typed commands
- `/app/mindmap/{mindmapId}/presence` - Send presence updates
- `/app/mindmap/{mindmapId}/cursor` - Send cursor updates
- `/app/mindmap/{mindmapId}/typing` - Send typing indicators
- `/app/private` - Send private messages

#### Topics (Broadcast to all subscribers)
- `/topic/mindmap/{mindmapId}/commands` - Typed command broadcasts
- `/topic/mindmap/{mindmapId}/presence` - User presence changes
- `/topic/mindmap/{mindmapId}/cursor` - Cursor position updates
- `/topic/mindmap/{mindmapId}/typing` - Typing indicators

## Typed Command System

The STOMP integration uses a typed command system that maps directly to frontend mind map operations, providing better type safety and validation compared to untyped messages.

### Command Structure

All commands inherit from a base `StompCommand` class with common fields:
- `mindmapId` - The mind map being edited
- `userId` - The user performing the action
- `commandId` - Unique identifier for the command
- `timestamp` - When the command was created
- `version` - Optional version information

### Available Command Types

#### 1. ADD_TOPIC
Creates a new topic in the mind map.
```json
{
  "type": "ADD_TOPIC",
  "text": "New Topic",
  "positionX": 100,
  "positionY": 100,
  "fontSize": "14px",
  "fontColor": "#000000",
  "bold": false,
  "italic": false,
  "parentTopicId": 1
}
```

#### 2. CHANGE_FEATURE
Modifies features of an existing topic (font, color, style, etc.).
```json
{
  "type": "CHANGE_FEATURE",
  "topicId": 1,
  "featureId": 1,
  "featureType": "font",
  "newValue": "16px",
  "oldValue": "14px"
}
```

#### 3. DELETE
Removes topics or relationships from the mind map.
```json
{
  "type": "DELETE",
  "topicIds": [1, 2],
  "relationshipIds": []
}
```

#### 4. DRAG_TOPIC
Moves a topic to a new position.
```json
{
  "type": "DRAG_TOPIC",
  "topicId": 1,
  "newPositionX": 150,
  "newPositionY": 150,
  "oldPositionX": 100,
  "oldPositionY": 100
}
```

#### 5. ADD_RELATIONSHIP
Creates a connection between two topics.
```json
{
  "type": "ADD_RELATIONSHIP",
  "fromTopicId": 1,
  "toTopicId": 2,
  "relationshipType": "line",
  "color": "#000000",
  "lineWidth": 2.0
}
```

#### 6. ADD_FEATURE
Adds features to a topic (icons, links, notes, etc.).
```json
{
  "type": "ADD_FEATURE",
  "topicId": 1,
  "featureType": "icon",
  "featureValue": "star",
  "featureAttributes": {}
}
```

#### 7. REMOVE_FEATURE
Removes features from a topic.
```json
{
  "type": "REMOVE_FEATURE",
  "topicId": 1,
  "featureId": 1,
  "featureType": "icon"
}
```

#### 8. MOVE_CONTROL_POINT
Adjusts control points of relationships.
```json
{
  "type": "MOVE_CONTROL_POINT",
  "relationshipId": 1,
  "controlPointId": 1,
  "newPositionX": 120,
  "newPositionY": 120,
  "oldPositionX": 100,
  "oldPositionY": 100
}
```

#### 9. GENERIC_FUNCTION
Handles custom operations that don't fit other command types.
```json
{
  "type": "GENERIC_FUNCTION",
  "functionName": "customOperation",
  "parameters": ["param1", "param2"],
  "namedParameters": {
    "option1": "value1",
    "option2": "value2"
  }
}
```

## REST API Endpoints

### Session Information
- `GET /api/stomp/mindmap/{mindmapId}/active-users` - Get active users for a mind map
- `GET /api/stomp/info` - Get WebSocket connection information
- `GET /api/stomp/stats` - Get session statistics (admin only)
- `POST /api/stomp/mindmap/{mindmapId}/test-message` - Send test message

## Client Integration

### JavaScript Example

```javascript
// Connect to WebSocket
const socket = new SockJS('/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Subscribe to mind map updates
    stompClient.subscribe('/topic/mindmap/' + mindmapId + '/updates', function(message) {
        const update = JSON.parse(message.body);
        handleMindmapUpdate(update);
    });
    
    // Subscribe to presence updates
    stompClient.subscribe('/topic/mindmap/' + mindmapId + '/presence', function(message) {
        const presence = JSON.parse(message.body);
        handleUserPresence(presence);
    });
});

// Send mind map update
function sendUpdate(operation, elementId, data) {
    const message = {
        operation: operation,
        elementId: elementId,
        elementType: 'topic',
        data: JSON.stringify(data)
    };
    
    stompClient.send('/app/mindmap/' + mindmapId + '/update', {}, JSON.stringify(message));
}

// Send presence update
function sendPresence(action) {
    const message = {
        action: action,
        mindmapId: mindmapId,
        userId: userId,
        userDisplayName: 'User Name',
        userColor: '#FF5733'
    };
    
    stompClient.send('/app/mindmap/' + mindmapId + '/presence', {}, JSON.stringify(message));
}
```

## Security

### Authentication
- WebSocket connections require authentication
- User information is extracted from Spring Security context
- All message destinations are protected by security rules

### CORS Configuration
- Currently configured to allow all origins (`*`)
- **Important**: Restrict CORS in production environment
- Configure specific allowed origins based on your deployment

### Message Validation
- All incoming messages should be validated
- Implement proper sanitization for user input
- Consider rate limiting for message sending

## Testing

### Test Page
A test page is available at `/stomp-test.html` for testing the STOMP integration:

1. Start the application
2. Navigate to `http://localhost:8080/stomp-test.html`
3. Enter a mind map ID and user ID
4. Click "Connect" to establish WebSocket connection
5. Use the interface to send test messages

### Manual Testing
```bash
# Test WebSocket endpoint
curl -i -N -H "Connection: Upgrade" -H "Upgrade: websocket" -H "Sec-WebSocket-Key: test" -H "Sec-WebSocket-Version: 13" http://localhost:8080/ws-native

# Test REST endpoints
curl http://localhost:8080/api/stomp/info
curl http://localhost:8080/api/stomp/mindmap/test-mindmap/active-users
```

## Configuration

### Application Properties
```properties
# WebSocket configuration
spring.websocket.sockjs.enabled=true
spring.websocket.sockjs.heartbeat-time=25000
spring.websocket.sockjs.disconnect-delay=5000

# Security configuration
spring.security.websocket.same-origin-disabled=false
```

### Production Considerations

1. **Message Broker**: Consider using RabbitMQ or ActiveMQ for production scalability
2. **Load Balancing**: Configure sticky sessions for WebSocket connections
3. **CORS**: Restrict CORS to specific domains
4. **Rate Limiting**: Implement rate limiting for message sending
5. **Monitoring**: Add metrics and monitoring for WebSocket connections
6. **Error Handling**: Implement proper error handling and reconnection logic

## Dependencies

The following dependencies were added to `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

## File Structure

```
src/main/java/com/wisemapping/
├── config/
│   ├── WebSocketConfig.java           # STOMP configuration
│   └── WebSocketSecurityConfig.java   # Security configuration
├── rest/
│   ├── StompController.java           # STOMP message handlers
│   ├── StompInfoController.java       # REST endpoints for STOMP info
│   └── model/
│       ├── MindmapUpdateMessage.java  # Update message model
│       └── UserPresenceMessage.java   # Presence message model
├── service/
│   └── StompSessionService.java       # Session management
└── listener/
    └── StompEventListener.java        # Connection event handlers

src/main/resources/static/
└── stomp-test.html                    # Test page
```

## Troubleshooting

### Common Issues

1. **Connection Refused**: Check if WebSocket is enabled and CORS is configured
2. **Authentication Failed**: Ensure user is properly authenticated
3. **Messages Not Received**: Verify subscription destinations match exactly
4. **CORS Errors**: Configure CORS properly for your domain

### Debugging

Enable debug logging:
```properties
logging.level.org.springframework.web.socket=DEBUG
logging.level.org.springframework.messaging=DEBUG
```

### Performance Monitoring

Monitor WebSocket connections:
```bash
# Check active connections
curl http://localhost:8080/api/stomp/stats

# Monitor specific mind map
curl http://localhost:8080/api/stomp/mindmap/{mindmapId}/active-users
```

## Future Enhancements

1. **Conflict Resolution**: Implement operational transformation for concurrent edits
2. **Message Persistence**: Store messages for offline users
3. **Push Notifications**: Integrate with browser push notifications
4. **File Sharing**: Real-time file sharing in mind map sessions
5. **Voice/Video**: Add voice and video collaboration features
6. **Analytics**: Track collaboration patterns and user engagement
