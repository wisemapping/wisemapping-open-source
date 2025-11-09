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

import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Detects service directory spam - maps that function as business listings
 * with contact information and marketing content.
 * 
 * This strategy identifies patterns common to spam maps:
 * - Contact information combinations (address + phone + website/email)
 * - Low node count with marketing content
 * - Keyword stuffing (repetitive location/service keywords)
 * - Service directory structure (business name + contact info)
 */
@Component
public class ServiceDirectorySpamStrategy implements SpamDetectionStrategy {

    private final SpamContentExtractor contentExtractor;
    
    @Value("${app.batch.spam-detection.min-nodes-exemption:15}")
    private int minNodesExemption;
    
    @Value("${app.batch.spam-detection.service-directory.keyword-stuffing-separators:25}")
    private int keywordStuffingSeparatorThreshold;
    
    @Value("${app.batch.spam-detection.service-directory.location-variants:8}")
    private int locationVariantsThreshold;
    
    @Value("${app.batch.spam-detection.service-directory.near-me-repetitions:10}")
    private int nearMeRepetitionsThreshold;
    
    private static final int MARKETING_KEYWORD_THRESHOLD = 3;

    // Patterns for detecting contact information
    private static final List<Pattern> CONTACT_PATTERNS = Arrays.asList(
        // Website/URL patterns
        Pattern.compile("https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE),
        Pattern.compile("www\\.[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE),
        
        // Phone number patterns
        Pattern.compile("\\+?[1-9]\\d{9,14}", Pattern.CASE_INSENSITIVE), // International format
        Pattern.compile("\\(?[0-9]{3}\\)?[-.\\s]?[0-9]{3}[-.\\s]?[0-9]{4}", Pattern.CASE_INSENSITIVE), // US format
        Pattern.compile("[0-9]{3}[-.\\s]?[0-9]{3}[-.\\s]?[0-9]{4}", Pattern.CASE_INSENSITIVE), // US format without parentheses
        
        // Address patterns (street addresses, postal codes)
        Pattern.compile("\\d+\\s+[a-zA-Z0-9\\s,.-]+\\s+(?:street|st|avenue|ave|road|rd|boulevard|blvd|lane|ln|drive|dr|court|ct|place|pl|way|blvd)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\d{5}(?:-\\d{4})?", Pattern.CASE_INSENSITIVE), // US ZIP codes
        Pattern.compile("[a-zA-Z]\\d[a-zA-Z]\\s?\\d[a-zA-Z]\\d", Pattern.CASE_INSENSITIVE), // Canadian postal codes
        Pattern.compile("\\b\\d{4}\\s?[a-zA-Z]{2}\\b", Pattern.CASE_INSENSITIVE), // Dutch postal codes
        
        // Email patterns
        Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE)
    );

    // Keywords that indicate keyword stuffing for SEO
    private static final List<String> KEYWORD_STUFFING_PATTERNS = Arrays.asList(
        "near me", "service near me", "best", "top", "location", "locations",
        "service", "services", "repair", "repairs", "clinic", "clinic near me"
    );

    // Common location keywords that appear in keyword stuffing
    private static final Pattern LOCATION_KEYWORD_PATTERN = Pattern.compile(
        "\\b(?:near me|in [A-Z][a-z]+|at [A-Z][a-z]+|[A-Z][a-z]+ (?:NY|CA|TX|FL|ON|BC|AB|QC|MB|SK|NS|NB|NL|PE|YT|NT|NU)|Upper [A-Z][a-z]+|Lower [A-Z][a-z]+|[A-Z][a-z]+ (?:Street|Avenue|Road|Boulevard|Drive|Lane|Way|Court|Place))\\b",
        Pattern.CASE_INSENSITIVE
    );

    public ServiceDirectorySpamStrategy(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }

    @Override
    public SpamDetectionResult detectSpam(Mindmap mindmap) {
        if (mindmap == null) {
            return SpamDetectionResult.notSpam();
        }

        try {
            String xml = mindmap.getXmlStr();
            if (xml == null || xml.trim().isEmpty()) {
                return SpamDetectionResult.notSpam();
            }

            // Check if marketing-heavy - if so, don't exempt based on node count
            final boolean marketingHeavy = contentExtractor.hasMarketingIndicators(mindmap, MARKETING_KEYWORD_THRESHOLD);
            
            // Count nodes
            int topicCount = (int) contentExtractor.countOccurrences(xml, "<topic");
            
            // Exempt high node count maps unless they're marketing-heavy
            if (topicCount > minNodesExemption && !marketingHeavy) {
                return SpamDetectionResult.notSpam();
            }

            // Extract and normalize content
            String content = contentExtractor.extractTextContent(mindmap);
            if (content.trim().isEmpty()) {
                return SpamDetectionResult.notSpam();
            }

            String normalizedContent = contentExtractor.normalizeForPatternMatching(content);
            if (normalizedContent.isEmpty()) {
                return SpamDetectionResult.notSpam();
            }

            String lowerContent = normalizedContent.toLowerCase();

            // Detect contact information
            boolean hasWebsite = CONTACT_PATTERNS.get(0).matcher(normalizedContent).find() || 
                               CONTACT_PATTERNS.get(1).matcher(normalizedContent).find();
            
            boolean hasPhone = CONTACT_PATTERNS.get(2).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(3).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(4).matcher(normalizedContent).find();
            
            boolean hasAddress = CONTACT_PATTERNS.get(5).matcher(normalizedContent).find() ||
                                CONTACT_PATTERNS.get(6).matcher(normalizedContent).find() ||
                                CONTACT_PATTERNS.get(7).matcher(normalizedContent).find() ||
                                CONTACT_PATTERNS.get(8).matcher(normalizedContent).find();
            
            boolean hasEmail = CONTACT_PATTERNS.get(9).matcher(normalizedContent).find();

            // Count contact pattern matches
            long contactPatternMatches = CONTACT_PATTERNS.stream()
                .mapToLong(pattern -> pattern.matcher(normalizedContent).results().count())
                .sum();

            // Detect keyword stuffing indicators
            int separatorCount = countSeparators(normalizedContent);
            int nearMeCount = countNearMeRepetitions(lowerContent);
            int locationVariants = countLocationVariants(normalizedContent);

            // Decision logic: Multiple signals combine for spam detection
            
            // Rule 1: Complete contact info combo (address + phone + website/email) with low node count
            if ((hasAddress && hasPhone && (hasWebsite || hasEmail)) && topicCount <= 5) {
                return SpamDetectionResult.spam(
                    "Service directory spam detected - complete contact information with low node count",
                    String.format("HasAddress: %s, HasPhone: %s, HasWebsite: %s, HasEmail: %s, TopicCount: %d",
                                hasAddress, hasPhone, hasWebsite, hasEmail, topicCount),
                    getType()
                );
            }

            // Rule 2: Contact info + keyword stuffing
            if ((hasPhone || hasWebsite || hasEmail) && 
                (separatorCount >= keywordStuffingSeparatorThreshold || 
                 nearMeCount >= nearMeRepetitionsThreshold ||
                 locationVariants >= locationVariantsThreshold)) {
                return SpamDetectionResult.spam(
                    "Service directory spam detected - contact info with keyword stuffing",
                    String.format("ContactPatterns: %d, Separators: %d, NearMeRepetitions: %d, LocationVariants: %d",
                                contactPatternMatches, separatorCount, nearMeCount, locationVariants),
                    getType()
                );
            }

            // Rule 3: High contact pattern count with marketing content
            if (contactPatternMatches >= 4 && marketingHeavy && topicCount <= 5) {
                return SpamDetectionResult.spam(
                    "Service directory spam detected - excessive contact patterns with marketing content",
                    String.format("ContactPatterns: %d, MarketingKeywords: %d, TopicCount: %d",
                                contactPatternMatches, contentExtractor.countSpamKeywords(mindmap), topicCount),
                    getType()
                );
            }

            // Rule 4: Keyword stuffing alone (very high thresholds to avoid false positives)
            if (separatorCount >= keywordStuffingSeparatorThreshold * 2 && 
                locationVariants >= locationVariantsThreshold * 2) {
                return SpamDetectionResult.spam(
                    "Service directory spam detected - excessive keyword stuffing",
                    String.format("Separators: %d, LocationVariants: %d",
                                separatorCount, locationVariants),
                    getType()
                );
            }

        } catch (Exception e) {
            // If we can't process the content, don't flag as spam
            return SpamDetectionResult.notSpam();
        }

        return SpamDetectionResult.notSpam();
    }

    /**
     * Counts separator characters (pipes, commas) that indicate keyword stuffing.
     */
    private int countSeparators(String content) {
        int pipeCount = (int) contentExtractor.countOccurrences(content, "|");
        int commaCount = (int) contentExtractor.countOccurrences(content, ",");
        // Weight pipes more heavily as they're more indicative of keyword stuffing
        return pipeCount * 2 + commaCount;
    }

    /**
     * Counts repetitions of "near me" and similar location-based phrases.
     */
    private int countNearMeRepetitions(String lowerContent) {
        int count = 0;
        for (String pattern : KEYWORD_STUFFING_PATTERNS) {
            count += contentExtractor.countOccurrences(lowerContent, pattern);
        }
        return count;
    }

    /**
     * Counts distinct location variants (cities, neighborhoods, etc.).
     */
    private int countLocationVariants(String content) {
        return (int) LOCATION_KEYWORD_PATTERN.matcher(content).results().count();
    }

    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.SERVICE_DIRECTORY;
    }
}

