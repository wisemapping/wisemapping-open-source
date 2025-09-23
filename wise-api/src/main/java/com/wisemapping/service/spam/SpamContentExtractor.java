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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
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
        
        // Extract text from text attributes
        Pattern textPattern = Pattern.compile("text=\"([^\"]*?)\"", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = textPattern.matcher(xml);
        while (matcher.find()) {
            text.append(matcher.group(1)).append(" ");
        }
        
        // Extract content from note tags (including CDATA and HTML content)
        Pattern notePattern = Pattern.compile("<note[^>]*>\\s*<!\\[CDATA\\[([^\\]]*?)\\]\\]>\\s*</note>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        matcher = notePattern.matcher(xml);
        while (matcher.find()) {
            String noteContent = matcher.group(1);
            // Sanitize HTML content for spam detection
            String sanitizedContent = sanitizeHtmlContent(noteContent);
            text.append(sanitizedContent).append(" ");
        }
        
        // Also extract content from note tags without CDATA (direct HTML content)
        Pattern noteDirectPattern = Pattern.compile("<note[^>]*>([^<]*(?:<[^/][^>]*>[^<]*)*)</note>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        matcher = noteDirectPattern.matcher(xml);
        while (matcher.find()) {
            String noteContent = matcher.group(1);
            // Sanitize HTML content for spam detection
            String sanitizedContent = sanitizeHtmlContent(noteContent);
            text.append(sanitizedContent).append(" ");
        }
        
        // Extract URLs from link attributes
        Pattern linkPattern = Pattern.compile("url=\"([^\"]*?)\"", Pattern.CASE_INSENSITIVE);
        matcher = linkPattern.matcher(xml);
        while (matcher.find()) {
            text.append(matcher.group(1)).append(" ");
        }
        
        // Extract any remaining text content after removing XML tags
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
    
    /**
     * Sanitizes HTML content by removing dangerous elements and attributes while preserving text content.
     * This method handles both plain text and HTML content safely for spam detection.
     * 
     * @param content The content to sanitize (may be plain text or HTML)
     * @return Sanitized text content safe for spam detection
     */
    public String sanitizeHtmlContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return "";
        }
        
        try {
            // Check if content looks like HTML (contains HTML tags)
            if (isHtmlContent(content)) {
                // Parse and sanitize HTML content
                Document doc = Jsoup.parse(content);
                
                // Use a very restrictive safelist that only allows basic text formatting
                // This removes all potentially dangerous elements like scripts, iframes, etc.
                Safelist safelist = Safelist.none()
                    .addTags("p", "br", "div", "span", "strong", "b", "em", "i", "u")
                    .addAttributes("p", "class", "style")
                    .addAttributes("div", "class", "style")
                    .addAttributes("span", "class", "style");
                
                // Clean the HTML content
                String cleanedHtml = Jsoup.clean(doc.body().html(), safelist);
                
                // Extract plain text from the cleaned HTML
                Document cleanedDoc = Jsoup.parse(cleanedHtml);
                String plainText = cleanedDoc.text();
                
                // Also extract URLs that might be in the content (for spam detection)
                String urls = doc.select("a[href]").stream()
                    .map(element -> element.attr("href"))
                    .filter(url -> !url.isEmpty())
                    .collect(Collectors.joining(" "));
                
                return (plainText + " " + urls).trim();
            } else {
                // Content is plain text, return as-is but decode HTML entities
                return Jsoup.parse(content).text();
            }
        } catch (Exception e) {
            logger.warn("Failed to sanitize HTML content, falling back to regex-based cleaning: {}", e.getMessage());
            // Fallback: use regex to remove HTML tags and decode basic entities
            return content.replaceAll("<[^>]*>", " ")
                         .replaceAll("&lt;", "<")
                         .replaceAll("&gt;", ">")
                         .replaceAll("&amp;", "&")
                         .replaceAll("&quot;", "\"")
                         .replaceAll("&#39;", "'")
                         .replaceAll("\\s+", " ")
                         .trim();
        }
    }
    
    /**
     * Determines if the given content contains HTML markup.
     * 
     * @param content The content to check
     * @return true if HTML tags are found, false otherwise
     */
    public boolean isHtmlContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        
        // Check for common HTML tags
        return content.contains("<") && content.contains(">") &&
               (content.contains("<p>") || content.contains("<div>") || 
                content.contains("<span>") || content.contains("<a ") ||
                content.contains("<script>") || content.contains("<iframe>") ||
                content.contains("<img") || content.contains("<br") ||
                content.contains("<strong>") || content.contains("<em>") ||
                content.matches(".*<[a-zA-Z][a-zA-Z0-9]*[^>]*>.*"));
    }
    
    /**
     * Checks if the mindmap contains HTML content in notes.
     * This is important for spam detection as HTML content can be used to hide spam text.
     * 
     * @param mindmap The mindmap to check
     * @return true if HTML content is found in notes, false otherwise
     */
    public boolean hasHtmlContent(Mindmap mindmap) {
        if (mindmap == null) {
            return false;
        }
        
        try {
            String xml = mindmap.getXmlStr();
            if (xml == null) {
                return false;
            }
            
            // Check for HTML content in note tags
            Pattern notePattern = Pattern.compile("<note[^>]*>\\s*<!\\[CDATA\\[([^\\]]*?)\\]\\]>\\s*</note>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            java.util.regex.Matcher matcher = notePattern.matcher(xml);
            while (matcher.find()) {
                String noteContent = matcher.group(1);
                if (isHtmlContent(noteContent)) {
                    return true;
                }
            }
            
            // Also check for direct HTML content in note tags
            Pattern noteDirectPattern = Pattern.compile("<note[^>]*>([^<]*(?:<[^/][^>]*>[^<]*)*)</note>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            matcher = noteDirectPattern.matcher(xml);
            while (matcher.find()) {
                String noteContent = matcher.group(1);
                if (isHtmlContent(noteContent)) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.warn("Error checking for HTML content in mindmap {}: {}", mindmap.getId(), e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Validates note content length for HTML notes.
     * HTML notes are limited to 1000 characters to prevent spam and performance issues.
     * 
     * @param mindmap The mindmap to validate
     * @param maxLength Maximum allowed length for HTML note content (default: 1000)
     * @return Validation result with details about any violations
     */
    public NoteValidationResult validateNoteContentLength(Mindmap mindmap, int maxLength) {
        if (mindmap == null) {
            return new NoteValidationResult(true, "", 0, 0);
        }
        
        try {
            String xml = mindmap.getXmlStr();
            if (xml == null) {
                return new NoteValidationResult(true, "", 0, 0);
            }
            
            int totalNotes = 0;
            int oversizedNotes = 0;
            StringBuilder violations = new StringBuilder();
            
            // Check CDATA note content
            Pattern notePattern = Pattern.compile("<note[^>]*>\\s*<!\\[CDATA\\[([^\\]]*?)\\]\\]>\\s*</note>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            java.util.regex.Matcher matcher = notePattern.matcher(xml);
            while (matcher.find()) {
                totalNotes++;
                String noteContent = matcher.group(1);
                
                if (isHtmlContent(noteContent)) {
                    // For HTML content, count characters in the raw HTML
                    int htmlLength = noteContent.length();
                    if (htmlLength > maxLength) {
                        oversizedNotes++;
                        violations.append(String.format("HTML note %d: %d chars (limit: %d), ", totalNotes, htmlLength, maxLength));
                    }
                } else {
                    // For plain text, count characters in the text content
                    String plainText = sanitizeHtmlContent(noteContent);
                    int textLength = plainText.length();
                    if (textLength > maxLength) {
                        oversizedNotes++;
                        violations.append(String.format("Text note %d: %d chars (limit: %d), ", totalNotes, textLength, maxLength));
                    }
                }
            }
            
            // Check direct HTML content in note tags (without CDATA)
            Pattern noteDirectPattern = Pattern.compile("<note[^>]*>([^<]*(?:<[^/][^>]*>[^<]*)*)</note>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
            matcher = noteDirectPattern.matcher(xml);
            while (matcher.find()) {
                totalNotes++;
                String noteContent = matcher.group(1);
                
                if (isHtmlContent(noteContent)) {
                    int htmlLength = noteContent.length();
                    if (htmlLength > maxLength) {
                        oversizedNotes++;
                        violations.append(String.format("HTML note %d: %d chars (limit: %d), ", totalNotes, htmlLength, maxLength));
                    }
                }
            }
            
            boolean isValid = oversizedNotes == 0;
            String violationDetails = violations.length() > 0 ? violations.toString() : "";
            
            return new NoteValidationResult(isValid, violationDetails, totalNotes, oversizedNotes);
        } catch (Exception e) {
            logger.warn("Error validating note content length for mindmap {}: {}", mindmap.getId(), e.getMessage());
            return new NoteValidationResult(false, "Error validating content: " + e.getMessage(), 0, 0);
        }
    }
    
    /**
     * Gets the current character count for a specific note content.
     * This is useful for displaying a counter to users.
     * 
     * @param noteContent The note content to count
     * @return Character count information
     */
    public NoteCharacterCount getNoteCharacterCount(String noteContent) {
        if (noteContent == null || noteContent.trim().isEmpty()) {
            return new NoteCharacterCount(0, 0, false, 1000);
        }
        
        boolean isHtml = isHtmlContent(noteContent);
        int rawLength = noteContent.length();
        int textLength = isHtml ? sanitizeHtmlContent(noteContent).length() : rawLength;
        int remainingChars = 1000 - rawLength;
        
        return new NoteCharacterCount(rawLength, textLength, isHtml, remainingChars);
    }
    
    /**
     * Result class for note validation.
     */
    public static class NoteValidationResult {
        private final boolean isValid;
        private final String violationDetails;
        private final int totalNotes;
        private final int oversizedNotes;
        
        public NoteValidationResult(boolean isValid, String violationDetails, int totalNotes, int oversizedNotes) {
            this.isValid = isValid;
            this.violationDetails = violationDetails;
            this.totalNotes = totalNotes;
            this.oversizedNotes = oversizedNotes;
        }
        
        public boolean isValid() { return isValid; }
        public String getViolationDetails() { return violationDetails; }
        public int getTotalNotes() { return totalNotes; }
        public int getOversizedNotes() { return oversizedNotes; }
    }
    
    /**
     * Result class for note character counting.
     */
    public static class NoteCharacterCount {
        private final int rawLength;
        private final int textLength;
        private final boolean isHtml;
        private final int remainingChars;
        
        public NoteCharacterCount(int rawLength, int textLength, boolean isHtml, int remainingChars) {
            this.rawLength = rawLength;
            this.textLength = textLength;
            this.isHtml = isHtml;
            this.remainingChars = remainingChars;
        }
        
        public int getRawLength() { return rawLength; }
        public int getTextLength() { return textLength; }
        public boolean isHtml() { return isHtml; }
        public int getRemainingChars() { return remainingChars; }
        public boolean isOverLimit() { return rawLength > 1000; }
        public double getUsagePercentage() { return (double) rawLength / 1000.0 * 100.0; }
    }
}