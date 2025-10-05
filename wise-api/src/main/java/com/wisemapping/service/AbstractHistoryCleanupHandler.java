package com.wisemapping.service;

import com.wisemapping.dao.MindmapManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Abstract base class for history cleanup handlers that provides common functionality
 * and implements the chain of responsibility pattern.
 */
public abstract class AbstractHistoryCleanupHandler implements HistoryCleanupHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(AbstractHistoryCleanupHandler.class);
    
    @Autowired
    protected MindmapManager mindmapManager;
    
    protected HistoryCleanupHandler nextHandler;
    
    /**
     * Get the next handler in the chain.
     * @return the next handler
     */
    public HistoryCleanupHandler getNext() {
        return nextHandler;
    }
    
    @Override
    public void setNext(HistoryCleanupHandler nextHandler) {
        this.nextHandler = nextHandler;
    }
    
    /**
     * Get the creator ID for a mindmap for logging purposes.
     * 
     * @param mindmapId the mindmap ID
     * @return the creator ID, or null if not found
     */
    protected Integer getMindmapCreatorId(int mindmapId) {
        try {
            // For now, use mindmapId as a placeholder for creator ID
            // In a real implementation, you might want to add a method to MindmapManager
            // to get creator ID, or cache this information
            return mindmapId;
        } catch (Exception e) {
            logger.warn("Could not get creator ID for mindmap {}: {}", mindmapId, e.getMessage());
            return null;
        }
    }
}
