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
        Pattern.compile("\\+?[1-9]\\d{1,3}[-.\\s]?\\d{3,4}[-.\\s]?\\d{3,4}[-.\\s]?\\d{3,4}", Pattern.CASE_INSENSITIVE), // International format with spaces/dashes (e.g., +61 1300 650 773)
        Pattern.compile("\\+?[1-9]\\d{9,14}", Pattern.CASE_INSENSITIVE), // International format without spaces
        Pattern.compile("\\(?[0-9]{3}\\)?[-.\\s]?[0-9]{3}[-.\\s]?[0-9]{4}", Pattern.CASE_INSENSITIVE), // US format
        Pattern.compile("[0-9]{3}[-.\\s]?[0-9]{3}[-.\\s]?[0-9]{4}", Pattern.CASE_INSENSITIVE), // US format without parentheses
        Pattern.compile("0\\d{2,4}[-.\\s]?\\d{3,4}[-.\\s]?\\d{3,4}", Pattern.CASE_INSENSITIVE), // UK format (starts with 0, e.g., 01273 782 734, 01754 768120)
        Pattern.compile("\\+44[-.\\s]?\\d{2,4}[-.\\s]?\\d{3,4}[-.\\s]?\\d{3,4}", Pattern.CASE_INSENSITIVE), // UK international format (+44)
        // Swiss phone format: +41 44 499 00 75 or 044 499 00 75 (supports 2-digit groups)
        Pattern.compile("\\+41[-.\\s]?\\d{2}[-.\\s]?\\d{3}[-.\\s]?\\d{2}[-.\\s]?\\d{2}", Pattern.CASE_INSENSITIVE), // Swiss international format (+41)
        Pattern.compile("0\\d{2}[-.\\s]?\\d{3}[-.\\s]?\\d{2}[-.\\s]?\\d{2}", Pattern.CASE_INSENSITIVE), // Swiss landline format (starts with 0)
        // More flexible international format supporting 2-4 digit groups
        Pattern.compile("\\+[1-9]\\d{1,3}[-.\\s]?\\d{2,4}(?:[-.\\s]?\\d{2,4}){2,3}", Pattern.CASE_INSENSITIVE), // Flexible international format
        
        // Address patterns (street addresses, postal codes, city/state/country combinations)
        Pattern.compile("\\d+\\s+[a-zA-Z0-9\\s,.-]+\\s+(?:street|st|avenue|ave|road|rd|boulevard|blvd|lane|ln|drive|dr|court|ct|place|pl|way|blvd)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\d{5}(?:-\\d{4})?", Pattern.CASE_INSENSITIVE), // US ZIP codes
        Pattern.compile("[a-zA-Z]\\d[a-zA-Z]\\s?\\d[a-zA-Z]\\d", Pattern.CASE_INSENSITIVE), // Canadian postal codes
        Pattern.compile("\\b\\d{4}\\s?[a-zA-Z]{2}\\b", Pattern.CASE_INSENSITIVE), // Dutch postal codes
        // Swiss postal codes: 4 digits followed by city name (e.g., "8001 Zürich")
        Pattern.compile("\\b\\d{4}\\s+[A-ZÄÖÜ][a-zäöüß]+(?:\\s+[A-ZÄÖÜ][a-zäöüß]+)*\\b", Pattern.CASE_INSENSITIVE), // Swiss postal codes
        Pattern.compile("\\b[a-zA-Z]+\\s+(?:road|rd|street|st|avenue|ave|boulevard|blvd|lane|ln|drive|dr)\\b", Pattern.CASE_INSENSITIVE), // Road names without numbers (e.g., "Spintex Road")
        Pattern.compile("\\b(?:[A-Z][a-z]+\\s+){1,3}(?:[A-Z][a-z]+\\s+)*(?:Canada|USA|United States|Ghana|UK|United Kingdom|Australia|New Zealand|South Africa|India|Brazil|Mexico|Germany|France|Italy|Spain|Netherlands|Belgium|Switzerland|Schweiz|Suisse|Svizzera|Austria|Sweden|Norway|Denmark|Finland|Poland|Portugal|Greece|Ireland|Japan|China|South Korea|Singapore|Malaysia|Thailand|Philippines|Indonesia|Vietnam|Taiwan|Hong Kong|UAE|Saudi Arabia|Israel|Turkey|Egypt|Kenya|Nigeria|Argentina|Chile|Colombia|Peru|Venezuela|Ecuador|Uruguay|Paraguay|Bolivia)\\b", Pattern.CASE_INSENSITIVE), // City/State/Country combinations (e.g., "Winnipeg Manitoba Canada", "Accra Greater Accra Region Ghana", "Zürich Schweiz")
        Pattern.compile("\\b(?:Canada|USA|United States|Ghana|UK|United Kingdom|Australia|New Zealand|South Africa|India|Brazil|Mexico|Germany|France|Italy|Spain|Netherlands|Belgium|Switzerland|Schweiz|Suisse|Svizzera|Austria|Sweden|Norway|Denmark|Finland|Poland|Portugal|Greece|Ireland|Japan|China|South Korea|Singapore|Malaysia|Thailand|Philippines|Indonesia|Vietnam|Taiwan|Hong Kong|UAE|Saudi Arabia|Israel|Turkey|Egypt|Kenya|Nigeria|Argentina|Chile|Colombia|Peru|Venezuela|Ecuador|Uruguay|Paraguay|Bolivia)\\b", Pattern.CASE_INSENSITIVE), // Country names alone (e.g., "Canada and US", "Schweiz")
        
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
    public SpamDetectionResult detectSpam(SpamDetectionContext context) {
        if (context == null || context.getMindmap() == null || context.getMapModel() == null) {
            return SpamDetectionResult.notSpam();
        }

        try {
            MapModel mapModel = context.getMapModel();
            
            // Check if marketing-heavy - if so, don't exempt based on node count
            String content = contentExtractor.extractTextContent(mapModel, 
                                                                  context.getTitle(), 
                                                                  context.getDescription());
            final boolean marketingHeavy = contentExtractor.countSpamKeywords(content.toLowerCase()) >= MARKETING_KEYWORD_THRESHOLD;
            
            // Count nodes from the parsed model
            int topicCount = mapModel.getTotalTopicCount();
            
            // Extract and normalize content early to check for contact info before exemption
            if (content.trim().isEmpty()) {
                return SpamDetectionResult.notSpam();
            }

            String normalizedContent = contentExtractor.normalizeForPatternMatching(content);
            if (normalizedContent.isEmpty()) {
                return SpamDetectionResult.notSpam();
            }

            // Detect contact information early
            // Website patterns: indices 0-1
            boolean hasWebsite = CONTACT_PATTERNS.get(0).matcher(normalizedContent).find() || 
                               CONTACT_PATTERNS.get(1).matcher(normalizedContent).find();
            
            // Phone patterns: indices 2-10 (9 patterns including Swiss and flexible international)
            boolean hasPhone = CONTACT_PATTERNS.get(2).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(3).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(4).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(5).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(6).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(7).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(8).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(9).matcher(normalizedContent).find() ||
                              CONTACT_PATTERNS.get(10).matcher(normalizedContent).find();
            
            // Address patterns: indices 11-17 (7 patterns including Swiss postal codes)
            boolean hasAddress = CONTACT_PATTERNS.get(11).matcher(normalizedContent).find() ||
                                CONTACT_PATTERNS.get(12).matcher(normalizedContent).find() ||
                                CONTACT_PATTERNS.get(13).matcher(normalizedContent).find() ||
                                CONTACT_PATTERNS.get(14).matcher(normalizedContent).find() ||
                                CONTACT_PATTERNS.get(15).matcher(normalizedContent).find() ||
                                CONTACT_PATTERNS.get(16).matcher(normalizedContent).find() ||
                                CONTACT_PATTERNS.get(17).matcher(normalizedContent).find();
            
            // Email patterns: index 18
            boolean hasEmail = CONTACT_PATTERNS.get(18).matcher(normalizedContent).find();
            
            // If map has complete contact info (address + phone + website/email), it's likely spam
            // even with higher node counts, unless it's a very large legitimate map (> 30 topics)
            boolean hasCompleteContactInfo = hasAddress && hasPhone && (hasWebsite || hasEmail);
            
            // Exempt high node count maps unless they're marketing-heavy OR have complete contact info
            // Complete contact info is a strong spam signal, so we check it even for higher node counts
            if (topicCount > minNodesExemption && !marketingHeavy && !hasCompleteContactInfo) {
                return SpamDetectionResult.notSpam();
            }

            String lowerContent = normalizedContent.toLowerCase();

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
                                contactPatternMatches, contentExtractor.countSpamKeywords(content.toLowerCase()), topicCount),
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

            // Rule 5: Address + Website/Email + multiple locations (even without phone)
            // This catches spam maps that have address and contact info but omit phone number
            if (hasAddress && (hasWebsite || hasEmail) && locationVariants >= 5 && topicCount <= 5) {
                return SpamDetectionResult.spam(
                    "Service directory spam detected - address with contact info and multiple locations",
                    String.format("HasAddress: %s, HasWebsite: %s, HasEmail: %s, LocationVariants: %d, TopicCount: %d",
                                hasAddress, hasWebsite, hasEmail, locationVariants, topicCount),
                    getType()
                );
            }

            // Rule 6: Address + Website + low node count (catches business listings even without phone)
            // This catches spam maps like 1901495 and 1924052 that have address + website but no phone
            if (hasAddress && hasWebsite && topicCount <= 3) {
                return SpamDetectionResult.spam(
                    "Service directory spam detected - address with website and low node count",
                    String.format("HasAddress: %s, HasWebsite: %s, TopicCount: %d",
                                hasAddress, hasWebsite, topicCount),
                    getType()
                );
            }

            // Rule 7: Complete contact info (address + phone + website/email) with moderate node count
            // This catches spam maps like 1922448 that have complete contact info but more than 5 topics
            // We allow up to 10 topics to catch service directory spam that includes some additional content
            if ((hasAddress && hasPhone && (hasWebsite || hasEmail)) && topicCount <= 10) {
                return SpamDetectionResult.spam(
                    "Service directory spam detected - complete contact information with moderate node count",
                    String.format("HasAddress: %s, HasPhone: %s, HasWebsite: %s, HasEmail: %s, TopicCount: %d",
                                hasAddress, hasPhone, hasWebsite, hasEmail, topicCount),
                    getType()
                );
            }

            // Rule 9: Complete contact info with higher node count (catches spam maps with tutorial content)
            // This catches spam maps like 1764490 that have complete contact info but include tutorial content
            // We allow up to 30 topics to catch spam that mixes contact info with other content
            if (hasCompleteContactInfo && topicCount <= 30) {
                return SpamDetectionResult.spam(
                    "Service directory spam detected - complete contact information with higher node count",
                    String.format("HasAddress: %s, HasPhone: %s, HasWebsite: %s, HasEmail: %s, TopicCount: %d",
                                hasAddress, hasPhone, hasWebsite, hasEmail, topicCount),
                    getType()
                );
            }

            // Rule 8: Address + Website/Email + moderate node count (catches business listings without phone)
            // This catches spam maps that have address and contact info but no phone, with more than 3 topics
            if ((hasAddress && (hasWebsite || hasEmail)) && topicCount <= 10 && topicCount > 3) {
                return SpamDetectionResult.spam(
                    "Service directory spam detected - address with contact info and moderate node count",
                    String.format("HasAddress: %s, HasWebsite: %s, HasEmail: %s, TopicCount: %d",
                                hasAddress, hasWebsite, hasEmail, topicCount),
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

