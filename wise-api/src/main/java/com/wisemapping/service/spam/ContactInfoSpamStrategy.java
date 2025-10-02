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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class ContactInfoSpamStrategy implements SpamDetectionStrategy {

    private final SpamContentExtractor contentExtractor;
    
    @Value("${app.batch.spam-detection.min-nodes-exemption:15}")
    private int minNodesExemption;

    // Patterns for detecting contact information spam
    private static final List<Pattern> CONTACT_PATTERNS = Arrays.asList(
        // Website/URL patterns
        Pattern.compile("https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE),
        Pattern.compile("www\\.[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE),
        Pattern.compile("[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/[^\\s]*)?", Pattern.CASE_INSENSITIVE),
        
        // Phone number patterns (various international formats)
        Pattern.compile("\\+?[1-9]\\d{9,14}", Pattern.CASE_INSENSITIVE), // International format (10-15 digits)
        Pattern.compile("\\(?[0-9]{3}\\)?[-.\\s]?[0-9]{3}[-.\\s]?[0-9]{4}", Pattern.CASE_INSENSITIVE), // US format
        Pattern.compile("[0-9]{3}[-.\\s]?[0-9]{3}[-.\\s]?[0-9]{4}", Pattern.CASE_INSENSITIVE), // US format without parentheses
        Pattern.compile("\\+?[0-9]{2,4}[-.\\s]?[0-9]{2,4}[-.\\s]?[0-9]{2,4}[-.\\s]?[0-9]{2,4}", Pattern.CASE_INSENSITIVE), // General format (8+ digits total)
        
        // Address patterns (street addresses, postal codes)
        Pattern.compile("\\d+\\s+[a-zA-Z0-9\\s,.-]+\\s+(?:street|st|avenue|ave|road|rd|boulevard|blvd|lane|ln|drive|dr|court|ct|place|pl)\\b", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\d{5}(?:-\\d{4})?", Pattern.CASE_INSENSITIVE), // US ZIP codes
        Pattern.compile("[a-zA-Z]\\d[a-zA-Z]\\s?\\d[a-zA-Z]\\d", Pattern.CASE_INSENSITIVE), // Canadian postal codes
        Pattern.compile("\\b\\d{4}\\s?[a-zA-Z]{2}\\b", Pattern.CASE_INSENSITIVE), // Dutch postal codes
        
        // Email patterns
        Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}", Pattern.CASE_INSENSITIVE),
        
        // Social media handles
        Pattern.compile("@[a-zA-Z0-9_]+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:facebook|twitter|instagram|linkedin|youtube)\\.com/[a-zA-Z0-9._-]+", Pattern.CASE_INSENSITIVE)
    );

    // Keywords that often accompany contact information spam
    private static final List<String> CONTACT_SPAM_KEYWORDS = Arrays.asList(
        "contact us", "get in touch", "reach out", "call now", "visit us", "find us",
        "our location", "business hours", "office hours", "appointment", "booking",
        "reservation", "inquiry", "quote", "estimate", "consultation", "free consultation",
        "call today", "visit today", "open now", "walk in", "drop by", "stop by"
    );

    public ContactInfoSpamStrategy(SpamContentExtractor contentExtractor) {
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

            // Check node count first - any mindmap with more than the configured threshold is considered legitimate content (not spam)
            int topicCount = (int) contentExtractor.countOccurrences(xml, "<topic");
            if (topicCount > minNodesExemption) {
                return SpamDetectionResult.notSpam();
            }

            // Extract all text content from the mindmap
            String content = contentExtractor.extractTextContent(mindmap);
            if (content.trim().isEmpty()) {
                return SpamDetectionResult.notSpam();
            }

            String lowerContent = content.toLowerCase();

            // Count contact information patterns
            long contactPatternMatches = CONTACT_PATTERNS.stream()
                .mapToLong(pattern -> pattern.matcher(content).results().count())
                .sum();

            // Count contact spam keywords
            long contactKeywordMatches = CONTACT_SPAM_KEYWORDS.stream()
                .mapToLong(keyword -> contentExtractor.countOccurrences(lowerContent, keyword))
                .sum();

            // Check for specific combinations that indicate contact info spam
            boolean hasWebsite = CONTACT_PATTERNS.get(0).matcher(content).find() || 
                               CONTACT_PATTERNS.get(1).matcher(content).find() ||
                               CONTACT_PATTERNS.get(2).matcher(content).find();
            
            boolean hasPhone = CONTACT_PATTERNS.get(3).matcher(content).find() ||
                              CONTACT_PATTERNS.get(4).matcher(content).find() ||
                              CONTACT_PATTERNS.get(5).matcher(content).find() ||
                              CONTACT_PATTERNS.get(6).matcher(content).find();
            
            boolean hasAddress = CONTACT_PATTERNS.get(7).matcher(content).find() ||
                                CONTACT_PATTERNS.get(8).matcher(content).find() ||
                                CONTACT_PATTERNS.get(9).matcher(content).find() ||
                                CONTACT_PATTERNS.get(10).matcher(content).find();

            // Determine if this is contact info spam
            boolean isContactInfoSpam = false;
            String reason = "";
            String details = "";

            // PRIMARY RULE: Contact info spam MUST contain Address, Website, and Phone
            if (hasWebsite && hasPhone && hasAddress) {
                isContactInfoSpam = true;
                reason = "Contact info spam detected - contains address, website, and phone";
                details = String.format("HasWebsite: %s, HasPhone: %s, HasAddress: %s", 
                                      hasWebsite, hasPhone, hasAddress);
            }
            // Rule 2: Has website + phone with contact keywords (secondary detection)
            else if (hasWebsite && hasPhone && contactKeywordMatches > 0) {
                isContactInfoSpam = true;
                reason = "Website and phone with contact keywords detected";
                details = String.format("HasWebsite: %s, HasPhone: %s, ContactKeywords: %d", 
                                      hasWebsite, hasPhone, contactKeywordMatches);
            }
            // Rule 3: Multiple contact patterns with contact keywords (secondary detection)
            else if (contactPatternMatches >= 3 && contactKeywordMatches >= 2) {
                isContactInfoSpam = true;
                reason = "Multiple contact patterns with contact keywords detected";
                details = String.format("ContactPatterns: %d, ContactKeywords: %d", 
                                      contactPatternMatches, contactKeywordMatches);
            }
            // Rule 4: Very high contact pattern count (likely spam)
            else if (contactPatternMatches >= 5) {
                isContactInfoSpam = true;
                reason = "Excessive contact information patterns detected";
                details = String.format("Content: '%s', ContactPatterns: %d", content, contactPatternMatches);
            }
            // Rule 5: High contact keyword count (likely spam)
            else if (contactKeywordMatches >= 4) {
                isContactInfoSpam = true;
                reason = "Excessive contact keywords detected";
                details = String.format("Content: '%s', ContactKeywords: %d", content, contactKeywordMatches);
            }

            if (isContactInfoSpam) {
                return SpamDetectionResult.spam(reason, details, getType());
            }

        } catch (UnsupportedEncodingException e) {
            // If we can't process the content, don't flag as spam
            return SpamDetectionResult.notSpam();
        }

        return SpamDetectionResult.notSpam();
    }

    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.CONTACT_INFO;
    }
}
