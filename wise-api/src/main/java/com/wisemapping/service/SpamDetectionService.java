package com.wisemapping.service;

import com.wisemapping.model.Mindmap;
import com.wisemapping.service.spam.SpamDetectionResult;
import com.wisemapping.service.spam.SpamDetectionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Service
public class SpamDetectionService {
    private final static Logger logger = LogManager.getLogger();
    
    private final List<SpamDetectionStrategy> strategies;

    @Autowired
    private MetricsService metricsService;

    public SpamDetectionService(@NotNull List<SpamDetectionStrategy> strategies) {
        this.strategies = strategies;
    }

    public SpamDetectionResult detectSpam(Mindmap mindmap) {
        return detectSpam(mindmap, "unknown");
    }

    public SpamDetectionResult detectSpam(Mindmap mindmap, String context) {
        if (mindmap == null) {
            return SpamDetectionResult.notSpam();
        }
        
        // Apply all spam detection strategies
        for (SpamDetectionStrategy strategy : strategies) {
            final SpamDetectionResult result = strategy.detectSpam(mindmap);
            if (result.isSpam()) {
                try {
                    logger.info("Spam detected by strategy '{}' in mindmap '{}' - Title: '{}', Description: '{}', XML: '{}', " +
                               "Reason: '{}', Details: '{}'",
                               strategy.getType().getStrategyName(), mindmap.getId(), mindmap.getTitle(), 
                               mindmap.getDescription(), mindmap.getXmlStr(), result.getReason(), result.getDetails());
                } catch (UnsupportedEncodingException e) {
                    logger.info("Spam detected by strategy '{}' in mindmap '{}' - Title: '{}', Description: '{}', " +
                               "Reason: '{}', Details: '{}'",
                               strategy.getType().getStrategyName(), mindmap.getId(), mindmap.getTitle(), 
                               mindmap.getDescription(), result.getReason(), result.getDetails());
                }
                SpamDetectionResult finalResult = new SpamDetectionResult(true, result.getReason(), 
                    String.format("Strategy: %s, Details: %s", strategy.getType().getStrategyName(), result.getDetails()),
                    strategy.getType());
                
                // Track spam analysis
                metricsService.trackSpamAnalysis(mindmap, finalResult, context);
                return finalResult;
            }
        }
        
        SpamDetectionResult cleanResult = SpamDetectionResult.notSpam();
        // Track spam analysis for clean result
        metricsService.trackSpamAnalysis(mindmap, cleanResult, context);
        return cleanResult;
    }

    public boolean isSpamContent(@NotNull Mindmap mindmap) {
        return detectSpam(mindmap).isSpam();
    }
}