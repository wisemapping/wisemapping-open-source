package com.wisemapping.service;

import com.wisemapping.dao.MindmapManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;

/**
 * Phase 1 history cleanup handler: removes ALL history for mindmaps between 
 * lower and upper boundary years (e.g., 3-17 years old).
 */
public class Phase1HistoryCleanupHandler extends AbstractHistoryCleanupHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(Phase1HistoryCleanupHandler.class);
    
    private final int lowerBoundaryYears;
    private final int upperBoundaryYears;
    
    public Phase1HistoryCleanupHandler(MindmapManager mindmapManager, int lowerBoundaryYears, int upperBoundaryYears) {
        this.mindmapManager = mindmapManager;
        this.lowerBoundaryYears = lowerBoundaryYears;
        this.upperBoundaryYears = upperBoundaryYears;
    }
    
    @Override
    public boolean canHandle(int mindmapId, Calendar lastModificationTime) {
        if (lastModificationTime == null) {
            logger.debug("Phase 1 - Mindmap {} has null lastModificationTime, skipping", mindmapId);
            return false;
        }
        
        Calendar lowerBoundaryDate = Calendar.getInstance();
        lowerBoundaryDate.add(Calendar.YEAR, -lowerBoundaryYears);
        
        Calendar upperBoundaryDate = Calendar.getInstance();
        upperBoundaryDate.add(Calendar.YEAR, -upperBoundaryYears);
        
        // Log the date ranges for debugging
        logger.info("Phase 1 - Mindmap {} check: lastMod={}, lowerBoundary={}, upperBoundary={}", 
                    mindmapId, 
                    lastModificationTime.getTime(), 
                    lowerBoundaryDate.getTime(), 
                    upperBoundaryDate.getTime());
        
        boolean canHandle = lastModificationTime.before(lowerBoundaryDate) && lastModificationTime.after(upperBoundaryDate);
        logger.info("Phase 1 - Mindmap {} canHandle result: {} (before lowerBoundary: {}, after upperBoundary: {})", 
                    mindmapId, canHandle, 
                    lastModificationTime.before(lowerBoundaryDate),
                    lastModificationTime.after(upperBoundaryDate));
        
        // Phase 1: Maps between lower and upper boundary
        return canHandle;
    }
    
    @Override
    public int processCleanup(int mindmapId, Calendar lastModificationTime) {
        Integer creatorId = getMindmapCreatorId(mindmapId);
        
        logger.info("Phase 1 - Cleaning ALL history for mindmap ID {} (creator ID: {}) - between {} and {} years old", 
                  mindmapId, creatorId, lowerBoundaryYears, upperBoundaryYears);
        
        return mindmapManager.removeHistoryByMindmapId(mindmapId);
    }
}
