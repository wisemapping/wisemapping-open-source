# Telemetry and Metrics

This document describes the telemetry and metrics implementation in the WiseMapping API using OpenTelemetry and Spring Boot Actuator.

## Overview

The application now tracks mindmap creation metrics using OpenTelemetry standards and exposes them through Spring Boot Actuator endpoints. This allows for monitoring and observability of the application's usage patterns.

## Metrics Tracked

### Mindmap Creation Metrics

The application tracks the following mindmap creation events:

- **New Mindmaps**: Created via the REST API (`POST /api/restful/maps`)
- **Duplicate Mindmaps**: Created by duplicating existing mindmaps (`POST /api/restful/maps/{id}`)
- **Tutorial Mindmaps**: Automatically created when new users register

All metrics use the counter name `mindmaps.created` with different tags to distinguish between types:

```
mindmaps.created{type="new"}        # New mindmaps created
mindmaps.created{type="duplicate"}  # Mindmaps created by duplication
mindmaps.created{type="tutorial"}   # Tutorial mindmaps for new users
```

### User Authentication Metrics

The application tracks user login events:

- **User Logins**: Tracked whenever a user successfully authenticates

```
user.logins                        # Total number of user logins
```

### Mindmap Publishing Metrics

The application tracks mindmap publishing events:

- **Publish Attempts**: Total number of times users attempt to publish mindmaps
- **Spam Detection**: Number of maps marked as spam during publish attempts

```
mindmaps.publish.attempts          # Total number of publish attempts
mindmaps.publish.spam_detected     # Maps marked as spam during publish
```

## Configuration

### Dependencies

The following dependencies have been added to `pom.xml`:

```xml
<!-- Spring Boot Actuator for metrics and monitoring -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>

<!-- OpenTelemetry dependencies -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>

<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-api</artifactId>
    <version>1.40.0</version>
</dependency>

<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-sdk</artifactId>
    <version>1.40.0</version>
</dependency>

<dependency>
    <groupId>io.opentelemetry</groupId>
    <artifactId>opentelemetry-exporter-prometheus</artifactId>
    <version>1.40.0-alpha</version>
</dependency>
```

### Application Configuration

The following configuration has been added to `application.yml`:

```yaml
# Spring Boot Actuator configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: wisemapping-api
```

## Accessing Metrics

### Actuator Endpoints

The following endpoints are available for accessing metrics:

- **Health Check**: `GET /actuator/health`
- **Application Info**: `GET /actuator/info`
- **Metrics**: `GET /actuator/metrics`
- **Prometheus Metrics**: `GET /actuator/prometheus`

### Example Usage

#### View All Metrics
```bash
curl http://localhost:8080/actuator/metrics
```

#### View Mindmap Creation Metrics
```bash
curl http://localhost:8080/actuator/metrics/mindmaps.created
```

#### View User Login Metrics
```bash
curl http://localhost:8080/actuator/metrics/user.logins
```

#### View Mindmap Publishing Metrics
```bash
curl http://localhost:8080/actuator/metrics/mindmaps.publish.attempts
curl http://localhost:8080/actuator/metrics/mindmaps.publish.spam_detected
```

#### View Prometheus Format
```bash
curl http://localhost:8080/actuator/prometheus
```

### Prometheus Integration

The metrics are exposed in Prometheus format at `/actuator/prometheus`, making it easy to integrate with Prometheus monitoring systems.

Example Prometheus queries:

Get total mindmaps created:
```
sum(mindmaps.created_total)
```

Get mindmaps created by type:
```
sum by (type) (mindmaps.created_total)
```

Get total user logins:
```
sum(user_logins_total)
```

Get total publish attempts:
```
sum(mindmaps_publish_attempts_total)
```

Get total spam detections during publish:
```
sum(mindmaps_publish_spam_detected_total)
```

## Implementation Details

### Code Locations

The metrics tracking is implemented in the following locations:

1. **MindmapServiceImpl.addMindmap()**: Tracks new mindmap creation
2. **MindmapController.createDuplicate()**: Tracks mindmap duplication
3. **UserServiceImpl.createUser()**: Tracks tutorial mindmap creation
4. **UserServiceImpl.auditLogin()**: Tracks user logins
5. **MindmapController.updatePublishStateInternal()**: Tracks publish attempts and spam detection

### Metric Creation

Metrics are created using Micrometer's Counter builder:

```java
// Mindmap creation metrics
Counter.builder("mindmaps.created")
    .description("Total number of mindmaps created")
    .tag("type", "new")  // or "duplicate", "tutorial"
    .register(meterRegistry)
    .increment();

// User login metrics
Counter.builder("user.logins")
    .description("Total number of user logins")
    .register(meterRegistry)
    .increment();

// Publish attempt metrics
Counter.builder("mindmaps.publish.attempts")
    .description("Total number of publish attempts")
    .register(meterRegistry)
    .increment();

// Spam detection metrics
Counter.builder("mindmaps.publish.spam_detected")
    .description("Total number of maps marked as spam during publish")
    .register(meterRegistry)
    .increment();
```

## Monitoring and Alerting

### Grafana Dashboards

You can create Grafana dashboards to visualize application usage trends:

- Daily mindmap creation counts
- Mindmap creation by type
- User registration trends (via tutorial mindmap creation)
- User login activity and patterns
- Mindmap publishing activity
- Spam detection rates during publishing

### Alerting Rules

Example Prometheus alerting rules:

```yaml
groups:
  - name: wisemapping
    rules:
      - alert: HighMindmapCreationRate
        expr: rate(mindmaps.created_total[5m]) > 100
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High mindmap creation rate detected"
          description: "Mindmap creation rate is {{ $value }} per second"
      
      - alert: HighSpamDetectionRate
        expr: rate(mindmaps_publish_spam_detected_total[5m]) > 10
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "High spam detection rate during publishing"
          description: "Spam detection rate is {{ $value }} per second"
      
      - alert: UnusualLoginActivity
        expr: rate(user_logins_total[5m]) > 50
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Unusual login activity detected"
          description: "Login rate is {{ $value }} per second"
```

## Testing

A test class `TelemetryMetricsTest` has been created to verify the metrics functionality. Run the tests with:

```bash
mvn test -Dtest=TelemetryMetricsTest
```

## Future Enhancements

Potential future enhancements to the telemetry system:

1. **Additional Metrics**: Track mindmap updates, deletions, collaboration events, etc.
2. **Custom Tags**: Add user ID, organization, or other contextual tags
3. **Histograms**: Track mindmap size, creation time, login duration, etc.
4. **Distributed Tracing**: Add OpenTelemetry tracing for request flows
5. **Custom Dashboards**: Create pre-built Grafana dashboards
6. **Authentication Method Tags**: Track login metrics by authentication type (password, OAuth2, etc.)
7. **Geographic Tags**: Add location-based tags for login and usage patterns

## Security Considerations

- The actuator endpoints should be secured in production environments
- Consider using authentication for metrics endpoints
- Monitor access to sensitive metrics data
- Use network-level security to restrict access to monitoring systems
