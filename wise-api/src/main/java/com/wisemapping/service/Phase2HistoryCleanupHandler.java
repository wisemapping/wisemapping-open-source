package com.wisemapping.service;

import com.wisemapping.dao.MindmapManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * Phase 2 history cleanup handler: keeps only a limited number of recent entries
 * for mindmaps between lower and upper boundary (e.g., between 6 months and 1 year old).
 */
public class Phase2HistoryCleanupHandler extends AbstractHistoryCleanupHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(Phase2HistoryCleanupHandler.class);
    
    private final int phase2LowerBoundaryYears;
    private final double phase2UpperBoundaryYears;
    private final int maxEntries;
    
    public Phase2HistoryCleanupHandler(MindmapManager mindmapManager, int phase2LowerBoundaryYears, double phase2UpperBoundaryYears, int maxEntries) {
        this.mindmapManager = mindmapManager;
        this.phase2LowerBoundaryYears = phase2LowerBoundaryYears;
        this.phase2UpperBoundaryYears = phase2UpperBoundaryYears;
        this.maxEntries = maxEntries;
    }
    
    @Override
    public boolean canHandle(int mindmapId, Calendar lastModificationTime) {
        if (lastModificationTime == null) {
            logger.debug("Phase 2 - Mindmap {} has null lastModificationTime, skipping", mindmapId);
            return false;
        }
        
        Calendar lowerBoundaryDate = Calendar.getInstance();
        lowerBoundaryDate.add(Calendar.YEAR, -phase2LowerBoundaryYears);
        
        Calendar upperBoundaryDate = Calendar.getInstance();
        upperBoundaryDate.add(Calendar.MONTH, -(int)(phase2UpperBoundaryYears * 12)); // Convert years to months
        
        // Log the date ranges for debugging
        logger.info("Phase 2 - Mindmap {} check: lastMod={}, lowerBoundary={}, upperBoundary={}", 
                    mindmapId, 
                    lastModificationTime.getTime(), 
                    lowerBoundaryDate.getTime(), 
                    upperBoundaryDate.getTime());
        
        boolean canHandle = lastModificationTime.before(lowerBoundaryDate) && lastModificationTime.after(upperBoundaryDate);
        logger.info("Phase 2 - Mindmap {} canHandle result: {} (before lowerBoundary: {}, after upperBoundary: {})", 
                    mindmapId, canHandle, 
                    lastModificationTime.before(lowerBoundaryDate),
                    lastModificationTime.after(upperBoundaryDate));
        
        // Phase 2: Maps between lower and upper boundary (e.g., 6 months to 1 year old)
        return canHandle;
    }
    
    @Override
    public int processCleanup(int mindmapId, Calendar lastModificationTime) {
        Integer creatorId = getMindmapCreatorId(mindmapId);
        
        logger.info("Phase 2 - Cleaning excess history for mindmap ID {} (creator ID: {}) - between {} and {} years old, keeping max {} entries", 
                  mindmapId, creatorId, phase2UpperBoundaryYears, phase2LowerBoundaryYears, maxEntries);
        
        return mindmapManager.removeExcessHistoryByMindmapId(mindmapId, maxEntries);
    }
}
