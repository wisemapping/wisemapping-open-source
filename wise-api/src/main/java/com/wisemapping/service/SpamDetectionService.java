package com.wisemapping.service;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.parser.MindmapParser;
import com.wisemapping.mindmap.utils.MindmapValidationException;
import com.wisemapping.model.Mindmap;
import com.wisemapping.service.spam.SpamContentExtractor;
import com.wisemapping.service.spam.SpamDetectionContext;
import com.wisemapping.service.spam.SpamDetectionResult;
import com.wisemapping.service.spam.SpamDetectionStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpamDetectionService {
    private final static Logger logger = LogManager.getLogger();
    
    private final List<SpamDetectionStrategy> strategies;

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private SpamContentExtractor spamContentExtractor;

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
        
        // Exempt maps if the creator's email belongs to an educational institution
        // Educational institutions are trusted and owners with these email domains are very unlikely to be spammers
        // Supports international educational domains (.edu, .edu.xx, .ac.xx, .sch.xx, .university, .school)
        if (spamContentExtractor.hasEduOrOrgEmail(mindmap)) {
            logger.debug("Mindmap {} exempted from spam detection - creator email belongs to educational institution", mindmap.getId());
            return SpamDetectionResult.notSpam();
        }
        
        // Parse the mindmap XML once into a MapModel
        MapModel mapModel;
        try {
            String xmlContent = mindmap.getXmlStr();
            if (xmlContent == null || xmlContent.trim().isEmpty()) {
                logger.debug("Mindmap {} has no XML content, skipping spam detection", mindmap.getId());
                return SpamDetectionResult.notSpam();
            }
            
            mapModel = MindmapParser.parseXml(xmlContent);
            
            // Set title and description from entity if not in model
            if (mapModel.getTitle() == null && mindmap.getTitle() != null) {
                mapModel.setTitle(mindmap.getTitle());
            }
            if (mapModel.getDescription() == null && mindmap.getDescription() != null) {
                mapModel.setDescription(mindmap.getDescription());
            }
        } catch (MindmapValidationException e) {
            logger.warn("Failed to parse mindmap XML for spam detection. Mindmap ID: {}, Error: {}", 
                       mindmap.getId(), e.getMessage());
            return SpamDetectionResult.notSpam();
        } catch (Exception e) {
            logger.warn("Unexpected error parsing mindmap XML for spam detection. Mindmap ID: {}, Error: {}", 
                       mindmap.getId(), e.getMessage());
            return SpamDetectionResult.notSpam();
        }
        
        // Create context with parsed model
        SpamDetectionContext detectionContext = new SpamDetectionContext(mindmap, mapModel);
        
        // Apply all spam detection strategies
        for (SpamDetectionStrategy strategy : strategies) {
            final SpamDetectionResult result = strategy.detectSpam(detectionContext);
            if (result.isSpam()) {
                logger.info("Spam detected by strategy '{}' in mindmap '{}' - Title: '{}', Description: '{}', " +
                           "Reason: '{}', Details: '{}'",
                           strategy.getType().getStrategyName(), mindmap.getId(), mindmap.getTitle(), 
                           mindmap.getDescription(), result.getReason(), result.getDetails());
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