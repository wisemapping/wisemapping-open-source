package com.wisemapping.service;

import com.wisemapping.model.Mindmap;
import com.wisemapping.service.spam.SpamDetectionResult;
import com.wisemapping.service.spam.SpamDetectionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class SpamDetectionService {
    private final static Logger logger = LogManager.getLogger();
    
    private final List<SpamDetectionStrategy> strategies;

    public SpamDetectionService(List<SpamDetectionStrategy> strategies) {
        this.strategies = strategies;
    }

    public boolean isSpamContent(Mindmap mindmap) {
        if (mindmap == null) {
            return false;
        }
        
        // Apply all spam detection strategies
        for (SpamDetectionStrategy strategy : strategies) {
            SpamDetectionResult result = strategy.detectSpam(mindmap);
            if (result.isSpam()) {
                try {
                    logger.warn("Spam detected by strategy '{}' in mindmap '{}' - Title: '{}', Description: '{}', XML: '{}', " +
                               "Reason: '{}', Details: '{}'",
                               strategy.getStrategyName(), mindmap.getId(), mindmap.getTitle(), 
                               mindmap.getDescription(), mindmap.getXmlStr(), result.getReason(), result.getDetails());
                } catch (UnsupportedEncodingException e) {
                    logger.warn("Spam detected by strategy '{}' in mindmap '{}' - Title: '{}', Description: '{}', " +
                               "Reason: '{}', Details: '{}'",
                               strategy.getStrategyName(), mindmap.getId(), mindmap.getTitle(), 
                               mindmap.getDescription(), result.getReason(), result.getDetails());
                }
                return true;
            }
        }
        
        return false;
    }
}