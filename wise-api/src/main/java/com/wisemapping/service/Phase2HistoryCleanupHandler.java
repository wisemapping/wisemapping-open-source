package com.wisemapping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * Phase 2 history cleanup handler: keeps only a limited number of recent entries
 * for mindmaps newer than the phase 2 starting point (e.g., newer than 1 year).
 */
@Component
public class Phase2HistoryCleanupHandler extends AbstractHistoryCleanupHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(Phase2HistoryCleanupHandler.class);
    
    private final int phase2StartingPointYears;
    private final int maxEntries;
    
    public Phase2HistoryCleanupHandler() {
        // Default values - should be injected via configuration
        this.phase2StartingPointYears = 1;
        this.maxEntries = 4;
    }
    
    public Phase2HistoryCleanupHandler(int phase2StartingPointYears, int maxEntries) {
        this.phase2StartingPointYears = phase2StartingPointYears;
        this.maxEntries = maxEntries;
    }
    
    @Override
    public boolean canHandle(int mindmapId, Calendar lastModificationTime) {
        if (lastModificationTime == null) {
            return false;
        }
        
        Calendar phase2StartingPointDate = Calendar.getInstance();
        phase2StartingPointDate.add(Calendar.YEAR, -phase2StartingPointYears);
        
        // Phase 2: Maps newer than phase 2 starting point
        return lastModificationTime.after(phase2StartingPointDate);
    }
    
    @Override
    public int processCleanup(int mindmapId, Calendar lastModificationTime) {
        Integer creatorId = getMindmapCreatorId(mindmapId);
        
        logger.info("Phase 2 - Cleaning excess history for mindmap ID {} (creator ID: {}) - keeping max {} entries", 
                  mindmapId, creatorId, maxEntries);
        
        return mindmapManager.removeExcessHistoryByMindmapId(mindmapId, maxEntries);
    }
}
