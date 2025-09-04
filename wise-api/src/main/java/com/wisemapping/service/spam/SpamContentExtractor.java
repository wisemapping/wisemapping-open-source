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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SpamContentExtractor {
    private final static Logger logger = LogManager.getLogger();

    @Value("classpath:spam-keywords.properties")
    private Resource spamKeywordsResource;

    private List<String> spamKeywords;
    
    @PostConstruct
    public void loadSpamKeywords() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(spamKeywordsResource.getInputStream()))) {
            spamKeywords = reader.lines()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .collect(Collectors.toList());
        } catch (IOException e) {
            logger.error("File could not be loaded.", e);
        }
    }

    public String extractTextContent(Mindmap mindmap) {
        StringBuilder content = new StringBuilder();
        
        if (mindmap.getTitle() != null) {
            content.append(mindmap.getTitle()).append(" ");
        }
        
        if (mindmap.getDescription() != null) {
            content.append(mindmap.getDescription()).append(" ");
        }
        
        try {
            content.append(extractTextFromXml(mindmap.getXmlStr()));
        } catch (UnsupportedEncodingException e) {
            // Skip XML content if encoding error
        }
        
        return content.toString();
    }
    
    public String extractTextFromXml(String xml) {
        if (xml == null) return "";
        
        StringBuilder text = new StringBuilder();
        
        Pattern textPattern = Pattern.compile("text=\"([^\"]*?)\"", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = textPattern.matcher(xml);
        while (matcher.find()) {
            text.append(matcher.group(1)).append(" ");
        }
        
        String xmlWithoutTags = xml.replaceAll("<[^>]*>", " ")
                                  .replaceAll("\\s+", " ")
                                  .trim();
        text.append(xmlWithoutTags);
        
        return text.toString().trim();
    }
    
    public long countOccurrences(String text, String substring) {
        if (text == null || substring == null) return 0;
        
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    public long countKeywordMatches(String lowerContent) {
        return spamKeywords != null ? spamKeywords.stream()
            .mapToLong(keyword -> countOccurrences(lowerContent, keyword))
            .sum() : 0;
    }
    
    public long countUniqueKeywordTypes(String lowerContent) {
        return spamKeywords != null ? spamKeywords.stream()
            .filter(keyword -> lowerContent.contains(keyword))
            .count() : 0;
    }
    
    public boolean hasSpamKeywords(String lowerContent) {
        return spamKeywords != null && spamKeywords.stream()
            .anyMatch(keyword -> lowerContent.contains(keyword));
    }
}