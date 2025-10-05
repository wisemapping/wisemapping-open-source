package com.wisemapping.service;

import java.util.Calendar;

/**
 * Base interface for history cleanup handlers in the Chain of Responsibility pattern.
 * Each handler is responsible for a specific cleanup phase based on mindmap age criteria.
 */
public interface HistoryCleanupHandler {
    
    /**
     * Check if this handler can process the given mindmap based on its last modification time.
     * 
     * @param mindmapId the mindmap ID
     * @param lastModificationTime the last modification time of the mindmap
     * @return true if this handler can process the mindmap, false otherwise
     */
    boolean canHandle(int mindmapId, Calendar lastModificationTime);
    
    /**
     * Process the mindmap history cleanup for this handler's phase.
     * 
     * @param mindmapId the mindmap ID
     * @param lastModificationTime the last modification time of the mindmap
     * @return number of history entries deleted by this handler
     */
    int processCleanup(int mindmapId, Calendar lastModificationTime);
    
    /**
     * Set the next handler in the chain.
     * 
     * @param nextHandler the next handler in the chain
     */
    void setNext(HistoryCleanupHandler nextHandler);
}
