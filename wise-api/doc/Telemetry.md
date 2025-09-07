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

#### View Prometheus Format
```bash
curl http://localhost:8080/actuator/prometheus
```

### Prometheus Integration

The metrics are exposed in Prometheus format at `/actuator/prometheus`, making it easy to integrate with Prometheus monitoring systems.

Example Prometheus query to get total mindmaps created:
```
sum(mindmaps.created_total)
```

Example query to get mindmaps created by type:
```
sum by (type) (mindmaps.created_total)
```

## Implementation Details

### Code Locations

The metrics tracking is implemented in the following locations:

1. **MindmapServiceImpl.addMindmap()**: Tracks new mindmap creation
2. **MindmapController.createDuplicate()**: Tracks mindmap duplication
3. **UserServiceImpl.createUser()**: Tracks tutorial mindmap creation

### Metric Creation

Metrics are created using Micrometer's Counter builder:

```java
Counter.builder("mindmaps.created")
    .description("Total number of mindmaps created")
    .tag("type", "new")  // or "duplicate", "tutorial"
    .register(meterRegistry)
    .increment();
```

## Monitoring and Alerting

### Grafana Dashboards

You can create Grafana dashboards to visualize mindmap creation trends:

- Daily mindmap creation counts
- Mindmap creation by type
- User registration trends (via tutorial mindmap creation)

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
```

## Testing

A test class `TelemetryMetricsTest` has been created to verify the metrics functionality. Run the tests with:

```bash
mvn test -Dtest=TelemetryMetricsTest
```

## Future Enhancements

Potential future enhancements to the telemetry system:

1. **Additional Metrics**: Track mindmap updates, deletions, user logins, etc.
2. **Custom Tags**: Add user ID, organization, or other contextual tags
3. **Histograms**: Track mindmap size, creation time, etc.
4. **Distributed Tracing**: Add OpenTelemetry tracing for request flows
5. **Custom Dashboards**: Create pre-built Grafana dashboards

## Security Considerations

- The actuator endpoints should be secured in production environments
- Consider using authentication for metrics endpoints
- Monitor access to sensitive metrics data
- Use network-level security to restrict access to monitoring systems
