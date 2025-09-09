/*
 *    Copyright [2022] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       http://www.wisemapping.org/license
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
// import org.springframework.stereotype.Component; // Disabled - strategy not active

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

// @Component // Disabled - KeywordPattern strategy is disabled
public class KeywordPatternStrategy implements SpamDetectionStrategy {
    
    private final SpamContentExtractor contentExtractor;
    
    @Value("${app.batch.spam-detection.min-nodes-exemption:15}")
    private int minNodesExemption;

    private static final List<Pattern> SPAM_PATTERNS = Arrays.asList(
        // Money and financial patterns
        Pattern.compile("\\$\\d+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\d+%.*profit", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\d+k.*month", Pattern.CASE_INSENSITIVE),
        Pattern.compile("€\\d+|£\\d+|¥\\d+", Pattern.CASE_INSENSITIVE),
        
        // URL patterns (common spam domains)
        Pattern.compile("https?://[a-zA-Z0-9.-]+\\.(com|net|org|biz|info)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("www\\.[a-zA-Z0-9.-]+\\.(com|net|org|biz|info)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("bit\\.ly/|tinyurl\\.com/|t\\.co/", Pattern.CASE_INSENSITIVE),
        
        // Contact patterns
        Pattern.compile("whatsapp.*\\+\\d+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("telegram.*@\\w+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("email.*@[a-zA-Z0-9.-]+\\.[a-z]{2,}", Pattern.CASE_INSENSITIVE),
        Pattern.compile("call.*\\+?\\d{10,}", Pattern.CASE_INSENSITIVE),
        
        // Crypto patterns
        Pattern.compile("bitcoin|btc|ethereum|eth|crypto", Pattern.CASE_INSENSITIVE),
        Pattern.compile("wallet.*address", Pattern.CASE_INSENSITIVE),
        Pattern.compile("mining.*pool", Pattern.CASE_INSENSITIVE),
        
        // MLM/Business patterns
        Pattern.compile("join.*team", Pattern.CASE_INSENSITIVE),
        Pattern.compile("\\d+.*spots.*left", Pattern.CASE_INSENSITIVE),
        Pattern.compile("level.*\\d+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("downline|upline", Pattern.CASE_INSENSITIVE),
        
        // Urgency patterns
        Pattern.compile("(limited|only).*\\d+.*(days?|hours?|minutes?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("expires?.*\\d+", Pattern.CASE_INSENSITIVE),
        Pattern.compile("act.*now|hurry.*up", Pattern.CASE_INSENSITIVE),
        
        // Guarantee patterns
        Pattern.compile("\\d+%.*guaranteed", Pattern.CASE_INSENSITIVE),
        Pattern.compile("no.*risk", Pattern.CASE_INSENSITIVE),
        Pattern.compile("money.*back", Pattern.CASE_INSENSITIVE)
    );

    public KeywordPatternStrategy(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }

    @Override
    public SpamDetectionResult detectSpam(Mindmap mindmap) {
        final String content = contentExtractor.extractTextContent(mindmap);
        if (content.trim().isEmpty()) {
            return SpamDetectionResult.notSpam();
        }
        
        // Check node count first - any mindmap with more than the configured threshold is considered legitimate content (not spam)
        try {
            String xml = mindmap.getXmlStr();
            if (xml != null && !xml.trim().isEmpty()) {
                int topicCount = (int) contentExtractor.countOccurrences(xml, "<topic");
                if (topicCount > minNodesExemption) {
                    return SpamDetectionResult.notSpam();
                }
            }
        } catch (Exception e) {
            // If we can't count nodes, continue with other spam detection
        }
        
        final String lowerContent = content.toLowerCase();
        
        // Check for spam keywords
        long uniqueKeywordTypes = contentExtractor.countUniqueKeywordTypes(lowerContent);

        // Check for spam patterns
        long patternMatches = SPAM_PATTERNS.stream()
            .mapToLong(pattern -> pattern.matcher(content).results().count())
            .sum();

        // Consider spam if indicators are present
        boolean isSpam = uniqueKeywordTypes >= 2 || patternMatches >= 2 || 
                        (uniqueKeywordTypes >= 1 && patternMatches >= 1);
        
        if (isSpam) {
            String reason = getSpamReason(uniqueKeywordTypes, patternMatches);
            String details = String.format("Content: '%s', UniqueKeywords: %d, Patterns: %d", 
                                          content, uniqueKeywordTypes, patternMatches);
            return SpamDetectionResult.spam(reason, details);
        }
        
        return SpamDetectionResult.notSpam();
    }
    
    private String getSpamReason(long uniqueKeywordTypes, long patternMatches) {
        if (uniqueKeywordTypes >= 2) {
            return "Multiple spam keywords detected";
        } else if (patternMatches >= 2) {
            return "Multiple spam patterns detected";
        } else if (uniqueKeywordTypes >= 1 && patternMatches >= 1) {
            return "Mixed spam indicators detected";
        }
        return "Spam indicators detected";
    }

    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.KEYWORD_PATTERN;
    }
}