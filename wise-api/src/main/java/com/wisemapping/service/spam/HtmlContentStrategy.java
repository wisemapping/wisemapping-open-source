/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.service.spam;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Spam detection strategy that focuses on HTML content in mindmap notes.
 * This strategy detects spam patterns that use HTML to hide content from users,
 * obfuscate spam keywords, or create excessive formatting that indicates spam.
 * 
 * Note: Security validation (dangerous HTML patterns) is handled by HtmlContentValidator
 * at save time, not by this spam detection strategy.
 */
@Component
public class HtmlContentStrategy implements SpamDetectionStrategy {
    
    private final static Logger logger = LogManager.getLogger();
    private final SpamContentExtractor contentExtractor;
    
    @Value("${app.batch.spam-detection.html.max-elements:50}")
    private int maxHtmlElements;
    
    @Value("${app.batch.spam-detection.html.max-ratio:0.7}")
    private double maxHtmlToTextRatio;
    
    
    @Value("${app.mindmap.note.max-length:5000}")
    private int maxNoteLength;
    
    // Patterns that indicate HTML-based spam (not security threats)
    private static final List<Pattern> HTML_SPAM_PATTERNS = Arrays.asList(
        // Hidden text patterns (commonly used to hide spam from users but not from search engines)
        Pattern.compile("style\\s*=\\s*[\"']display\\s*:\\s*none[\"']", Pattern.CASE_INSENSITIVE),
        Pattern.compile("style\\s*=\\s*[\"']visibility\\s*:\\s*hidden[\"']", Pattern.CASE_INSENSITIVE),
        Pattern.compile("style\\s*=\\s*[\"']position\\s*:\\s*absolute[^\"']*left\\s*:\\s*-[0-9]+px[\"']", Pattern.CASE_INSENSITIVE),
        
        // Suspicious color schemes (white text on white background)
        Pattern.compile("color\\s*:\\s*white[^;]*background\\s*:\\s*white", Pattern.CASE_INSENSITIVE),
        Pattern.compile("color\\s*:\\s*#fff[^;]*background\\s*:#fff", Pattern.CASE_INSENSITIVE),
        
        // Hidden input fields (potential form spam)
        Pattern.compile("<input[^>]*type\\s*=\\s*[\"']hidden[\"'][^>]*>", Pattern.CASE_INSENSITIVE),
        
        // Meta refresh redirects (common in spam)
        Pattern.compile("<meta[^>]*http-equiv\\s*=\\s*[\"']refresh[\"'][^>]*>", Pattern.CASE_INSENSITIVE),
        
        // Excessive links (more than 10 links in a single note)
        Pattern.compile("(<a[^>]*href[^>]*>.*?</a>.*?){10,}", Pattern.CASE_INSENSITIVE | Pattern.DOTALL)
    );
    
    public HtmlContentStrategy(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }
    
    @Override
    public SpamDetectionResult detectSpam(SpamDetectionContext context) {
        if (context == null || context.getMindmap() == null || context.getMapModel() == null) {
            return SpamDetectionResult.notSpam();
        }
        
        MapModel mapModel = context.getMapModel();
        
        // Check if mindmap contains HTML content
        if (!contentExtractor.hasHtmlContent(mapModel)) {
            return SpamDetectionResult.notSpam();
        }
        
        try {
            // Extract all HTML content from notes using the parsed model
            String htmlContent = extractAllHtmlContent(mapModel);
            if (htmlContent.trim().isEmpty()) {
                return SpamDetectionResult.notSpam();
            }
            
            // Check for suspicious HTML patterns
            SpamDetectionResult patternResult = checkHtmlPatterns(htmlContent);
            if (patternResult.isSpam()) {
                return patternResult;
            }
            
            // Check HTML element count
            SpamDetectionResult elementCountResult = checkHtmlElementCount(mapModel);
            if (elementCountResult.isSpam()) {
                return elementCountResult;
            }
            
            // Check HTML to text ratio
            SpamDetectionResult ratioResult = checkHtmlToTextRatio(htmlContent);
            if (ratioResult.isSpam()) {
                return ratioResult;
            }
            
            // Note: Security-related tag checking is handled by HtmlContentValidator at save time
            
            // Check note content length limits
            SpamDetectionResult noteLengthResult = checkNoteContentLength(context.getMindmap());
            if (noteLengthResult.isSpam()) {
                return noteLengthResult;
            }
            
            return SpamDetectionResult.notSpam();
            
        } catch (Exception e) {
            logger.warn("Error during HTML content spam detection for mindmap {}: {}", 
                       context.getMindmap().getId(), e.getMessage());
            return SpamDetectionResult.notSpam();
        }
    }
    
    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.HTML_CONTENT;
    }
    
    /**
     * Extracts all HTML content from notes in the parsed model.
     */
    private String extractAllHtmlContent(MapModel mapModel) {
        StringBuilder htmlContent = new StringBuilder();
        
        // Extract HTML content from all topics' notes
        for (Topic topic : mapModel.getAllTopics()) {
            String note = topic.getNote();
            if (note != null && !note.trim().isEmpty() && contentExtractor.isHtmlContent(note)) {
                htmlContent.append(note).append(" ");
            }
        }
        
        return htmlContent.toString();
    }
    
    /**
     * Checks for suspicious HTML patterns that indicate spam.
     */
    private SpamDetectionResult checkHtmlPatterns(String htmlContent) {
        long suspiciousPatterns = HTML_SPAM_PATTERNS.stream()
            .mapToLong(pattern -> {
                java.util.regex.Matcher matcher = pattern.matcher(htmlContent);
                long count = 0;
                while (matcher.find()) {
                    count++;
                }
                return count;
            })
            .sum();
        
        if (suspiciousPatterns > 0) {
            return new SpamDetectionResult(true, 
                "Suspicious HTML patterns detected", 
                String.format("Found %d suspicious HTML patterns", suspiciousPatterns),
                SpamStrategyType.HTML_CONTENT);
        }
        
        return SpamDetectionResult.notSpam();
    }
    
    /**
     * Checks if the HTML element count exceeds the threshold.
     */
    private SpamDetectionResult checkHtmlElementCount(MapModel mapModel) {
        long htmlElementCount = countHtmlElements(mapModel);
        
        if (htmlElementCount > maxHtmlElements) {
            return new SpamDetectionResult(true, 
                "Excessive HTML elements detected", 
                String.format("Found %d HTML elements, threshold: %d", htmlElementCount, maxHtmlElements),
                SpamStrategyType.HTML_CONTENT);
        }
        
        return SpamDetectionResult.notSpam();
    }
    
    /**
     * Counts HTML elements in the mindmap model.
     */
    private long countHtmlElements(MapModel mapModel) {
        long count = 0;
        
        // Count HTML tags in all notes
        for (Topic topic : mapModel.getAllTopics()) {
            String note = topic.getNote();
            if (note != null && !note.trim().isEmpty() && contentExtractor.isHtmlContent(note)) {
                // Count HTML tags in the content
                count += contentExtractor.countOccurrences(note, "<");
            }
        }
        
        return count;
    }
    
    /**
     * Checks if the HTML to text ratio is suspiciously high.
     */
    private SpamDetectionResult checkHtmlToTextRatio(String htmlContent) {
        // Extract plain text from HTML
        String plainText = contentExtractor.sanitizeHtmlContent(htmlContent);
        
        if (plainText.trim().isEmpty()) {
            // If there's HTML but no extractable text, it's suspicious
            return new SpamDetectionResult(true, 
                "HTML content with no extractable text", 
                "HTML content contains no readable text",
                SpamStrategyType.HTML_CONTENT);
        }
        
        // Calculate ratio of HTML tags to text
        long htmlTagCount = contentExtractor.countOccurrences(htmlContent, "<");
        int textLength = plainText.length();
        
        if (textLength == 0) {
            return new SpamDetectionResult(true, 
                "HTML content with zero text length", 
                "HTML content has no readable text after sanitization",
                SpamStrategyType.HTML_CONTENT);
        }
        
        double htmlToTextRatio = (double) htmlTagCount / textLength;
        
        if (htmlToTextRatio > maxHtmlToTextRatio) {
            return new SpamDetectionResult(true, 
                "High HTML to text ratio", 
                String.format("HTML to text ratio: %.2f, threshold: %.2f", htmlToTextRatio, maxHtmlToTextRatio),
                SpamStrategyType.HTML_CONTENT);
        }
        
        return SpamDetectionResult.notSpam();
    }
    
    
    /**
     * Checks if any note content exceeds the maximum allowed length.
     * This prevents spam by limiting the size of individual notes.
     */
    private SpamDetectionResult checkNoteContentLength(Mindmap mindmap) {
        com.wisemapping.mindmap.utils.MindmapUtils.NoteValidationResult validationResult = 
            contentExtractor.validateNoteContentLength(mindmap, maxNoteLength);
        
        if (!validationResult.isValid()) {
            return new SpamDetectionResult(true, 
                "Note content exceeds maximum length", 
                String.format("Found %d oversized notes out of %d total notes. %s", 
                    validationResult.getOversizedNotes(), 
                    validationResult.getTotalNotes(),
                    validationResult.getViolationDetails()),
                SpamStrategyType.HTML_CONTENT);
        }
        
        return SpamDetectionResult.notSpam();
    }
}
