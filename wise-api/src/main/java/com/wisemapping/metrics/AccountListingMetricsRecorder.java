package com.wisemapping.metrics;

import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class AccountListingMetricsRecorder {

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
            int returnedUsers,
            long totalUsers,
            int page,
            int pageSize,
            @Nullable String search,
            @Nullable Boolean filterActive,
            @Nullable Boolean filterSuspended,
            @Nullable String filterAuthType,
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
                final int returnedUsers,
                final long totalUsers,
                final int page,
                final int pageSize,
                final @Nullable String search,
                final @Nullable Boolean filterActive,
                final @Nullable Boolean filterSuspended,
                final @Nullable String filterAuthType,
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
            this.returnedUsers = returnedUsers;
            this.totalUsers = totalUsers;
            this.page = page;
            this.pageSize = pageSize;
            this.search = search;
            this.filterActive = filterActive;
            this.filterSuspended = filterSuspended;
            this.filterAuthType = filterAuthType;
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

