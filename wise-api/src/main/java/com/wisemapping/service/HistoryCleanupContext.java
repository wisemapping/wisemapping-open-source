package com.wisemapping.service;

import java.util.Calendar;

/**
 * Context object that holds configuration and state for history cleanup operations.
 */
public class HistoryCleanupContext {
    
    private final int lowerBoundaryYears;
    private final int upperBoundaryYears;
    private final int phase2StartingPointYears;
    private final int phase2MaxEntries;
    private final int batchSize;
    
    private int totalDeleted = 0;
    private int totalProcessed = 0;
    private int totalSkipped = 0;
    private int phase1Processed = 0;
    private int phase2Processed = 0;
    
    public HistoryCleanupContext(int lowerBoundaryYears, int upperBoundaryYears, 
                               int phase2StartingPointYears, int phase2MaxEntries, int batchSize) {
        this.lowerBoundaryYears = lowerBoundaryYears;
        this.upperBoundaryYears = upperBoundaryYears;
        this.phase2StartingPointYears = phase2StartingPointYears;
        this.phase2MaxEntries = phase2MaxEntries;
        this.batchSize = batchSize;
    }
    
    // Getters for configuration
    public int getLowerBoundaryYears() { return lowerBoundaryYears; }
    public int getUpperBoundaryYears() { return upperBoundaryYears; }
    public int getPhase2StartingPointYears() { return phase2StartingPointYears; }
    public int getPhase2MaxEntries() { return phase2MaxEntries; }
    public int getBatchSize() { return batchSize; }
    
    // Getters and setters for statistics
    public int getTotalDeleted() { return totalDeleted; }
    public void addDeleted(int count) { this.totalDeleted += count; }
    
    public int getTotalProcessed() { return totalProcessed; }
    public void incrementProcessed() { this.totalProcessed++; }
    
    public int getTotalSkipped() { return totalSkipped; }
    public void incrementSkipped() { this.totalSkipped++; }
    
    public int getPhase1Processed() { return phase1Processed; }
    public void incrementPhase1Processed() { this.phase1Processed++; }
    
    public int getPhase2Processed() { return phase2Processed; }
    public void incrementPhase2Processed() { this.phase2Processed++; }
}
