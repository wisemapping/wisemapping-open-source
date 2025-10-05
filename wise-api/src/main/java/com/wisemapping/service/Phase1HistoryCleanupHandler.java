package com.wisemapping.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Calendar;

/**
 * Phase 1 history cleanup handler: removes ALL history for mindmaps between 
 * lower and upper boundary years (e.g., 3-17 years old).
 */
@Component
public class Phase1HistoryCleanupHandler extends AbstractHistoryCleanupHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(Phase1HistoryCleanupHandler.class);
    
    private final int lowerBoundaryYears;
    private final int upperBoundaryYears;
    
    public Phase1HistoryCleanupHandler() {
        // Default values - should be injected via configuration
        this.lowerBoundaryYears = 3;
        this.upperBoundaryYears = 17;
    }
    
    public Phase1HistoryCleanupHandler(int lowerBoundaryYears, int upperBoundaryYears) {
        this.lowerBoundaryYears = lowerBoundaryYears;
        this.upperBoundaryYears = upperBoundaryYears;
    }
    
    @Override
    public boolean canHandle(int mindmapId, Calendar lastModificationTime) {
        if (lastModificationTime == null) {
            return false;
        }
        
        Calendar lowerBoundaryDate = Calendar.getInstance();
        lowerBoundaryDate.add(Calendar.YEAR, -lowerBoundaryYears);
        
        Calendar upperBoundaryDate = Calendar.getInstance();
        upperBoundaryDate.add(Calendar.YEAR, -upperBoundaryYears);
        
        // Phase 1: Maps between lower and upper boundary
        return lastModificationTime.before(lowerBoundaryDate) && lastModificationTime.after(upperBoundaryDate);
    }
    
    @Override
    public int processCleanup(int mindmapId, Calendar lastModificationTime) {
        Integer creatorId = getMindmapCreatorId(mindmapId);
        
        logger.info("Phase 1 - Cleaning ALL history for mindmap ID {} (creator ID: {}) - between {} and {} years old", 
                  mindmapId, creatorId, lowerBoundaryYears, upperBoundaryYears);
        
        return mindmapManager.removeHistoryByMindmapId(mindmapId);
    }
}
