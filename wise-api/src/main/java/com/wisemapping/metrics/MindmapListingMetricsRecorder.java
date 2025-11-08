package com.wisemapping.metrics;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class MindmapListingMetricsRecorder {

    private final AtomicReference<Snapshot> lastSnapshot = new AtomicReference<>();

    public void record(final Snapshot snapshot) {
        lastSnapshot.set(snapshot);
    }

    public Optional<Snapshot> latest() {
        return Optional.ofNullable(lastSnapshot.get());
    }

    public record Snapshot(
            boolean loggingEnabled,
            Instant capturedAt,
            int mapCount,
            int collaborationCount,
            long totalTimeMillis,
            List<Segment> segments,
            Long executedStatements,
            Long entityFetches,
            Long collectionFetches,
            Long entityLoads,
            List<QueryStatistics> topQueries
    ) {
        public Snapshot(
                final boolean loggingEnabled,
                final Instant capturedAt,
                final int mapCount,
                final int collaborationCount,
                final long totalTimeMillis,
                final List<Segment> segments,
                final Long executedStatements,
                final Long entityFetches,
                final Long collectionFetches,
                final Long entityLoads,
                final List<QueryStatistics> topQueries
        ) {
            this.loggingEnabled = loggingEnabled;
            this.capturedAt = capturedAt;
            this.mapCount = mapCount;
            this.collaborationCount = collaborationCount;
            this.totalTimeMillis = totalTimeMillis;
            this.segments = Collections.unmodifiableList(new ArrayList<>(segments));
            this.executedStatements = executedStatements;
            this.entityFetches = entityFetches;
            this.collectionFetches = collectionFetches;
            this.entityLoads = entityLoads;
            this.topQueries = Collections.unmodifiableList(new ArrayList<>(topQueries));
        }
    }

    public record Segment(
            String name,
            long timeMillis,
            double ratio
    ) {
    }

    public record QueryStatistics(
            String sql,
            long executions
    ) {
    }
}

