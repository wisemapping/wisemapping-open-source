package com.wisemapping.service;

import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
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

    public SpamDetectionResult detectSpam(Mindmap mindmap) {
        if (mindmap == null) {
            return SpamDetectionResult.notSpam();
        }
        
        // Apply all spam detection strategies
        for (SpamDetectionStrategy strategy : strategies) {
            SpamDetectionResult result = strategy.detectSpam(mindmap);
            if (result.isSpam()) {
                try {
                    logger.warn("Spam detected by strategy '{}' in mindmap '{}' - Title: '{}', Description: '{}', XML: '{}', " +
                               "Reason: '{}', Details: '{}'",
                               strategy.getType().getStrategyName(), mindmap.getId(), mindmap.getTitle(), 
                               mindmap.getDescription(), mindmap.getXmlStr(), result.getReason(), result.getDetails());
                } catch (UnsupportedEncodingException e) {
                    logger.warn("Spam detected by strategy '{}' in mindmap '{}' - Title: '{}', Description: '{}', " +
                               "Reason: '{}', Details: '{}'",
                               strategy.getType().getStrategyName(), mindmap.getId(), mindmap.getTitle(), 
                               mindmap.getDescription(), result.getReason(), result.getDetails());
                }
                return new SpamDetectionResult(true, result.getReason(), 
                    String.format("Strategy: %s, Details: %s", strategy.getType().getStrategyName(), result.getDetails()),
                    strategy.getType());
            }
        }
        
        return SpamDetectionResult.notSpam();
    }

    public boolean isSpamContent(Mindmap mindmap) {
        return detectSpam(mindmap).isSpam();
    }
}