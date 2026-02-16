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
import com.wisemapping.mindmap.parser.MindmapParser;
import com.wisemapping.mindmap.utils.MindmapUtils;
import com.wisemapping.mindmap.utils.MindmapUtils.NoteValidationResult;
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
import java.util.List;
import java.util.stream.Collectors;
import java.util.regex.Pattern;

/**
 * Extracts text content from mindmaps for spam detection purposes.
 * This class focuses solely on spam detection and delegates XML parsing to
 * MindmapParser.
 */
@Component
public class SpamContentExtractor {
    private final static Logger logger = LogManager.getLogger();

    @Value("classpath:spam-keywords.properties")
    private Resource spamKeywordsResource;

    @Value("${app.mindmap.note.max-length:10000}")
    private int maxNoteLength;

    private List<String> spamKeywords;

    /**
     * Set of forbidden HTML tags that serve as a strong indicator of spam or
     * security risk.
     * These tags are blocked by validation and flagged by spam detection.
     */
    public static final java.util.Set<String> FORBIDDEN_TAGS = java.util.Set.of(
            "script", "object", "iframe", "embed", "form", "input", "img");

    /**
     * Patterns that indicate HTML-based spam (not security threats).
     * These are used both for spam detection and for blocking content at save time.
     */
    public static final List<Pattern> HTML_SPAM_PATTERNS = java.util.Arrays.asList(
            // Hidden text patterns (commonly used to hide spam from users but not from
            // search engines)
            Pattern.compile("style\\s*=\\s*[\"']display\\s*:\\s*none[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("style\\s*=\\s*[\"']visibility\\s*:\\s*hidden[\"']", Pattern.CASE_INSENSITIVE),
            Pattern.compile("style\\s*=\\s*[\"']position\\s*:\\s*absolute[^\"']*left\\s*:\\s*-[0-9]+px[\"']",
                    Pattern.CASE_INSENSITIVE),

            // Suspicious color schemes (white text on white background)
            Pattern.compile("color\\s*:\\s*white[^;]*background\\s*:\\s*white", Pattern.CASE_INSENSITIVE),
            Pattern.compile("color\\s*:\\s*#fff[^;]*background\\s*:#fff", Pattern.CASE_INSENSITIVE),

            // Hidden input fields (potential form spam)
            Pattern.compile("<input[^>]*type\\s*=\\s*[\"']hidden[\"'][^>]*>", Pattern.CASE_INSENSITIVE),

            // Meta refresh redirects (common in spam)
            Pattern.compile("<meta[^>]*http-equiv\\s*=\\s*[\"']refresh[\"'][^>]*>", Pattern.CASE_INSENSITIVE),

            // Excessive links (more than 10 links in a single note)
            Pattern.compile("(<a[^>]*href[^>]*>.*?</a>.*?){10,}", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));

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

    /**
     * Extracts text content from a mindmap model for spam analysis.
     * This method works with the parsed MapModel instead of parsing XML.
     * 
     * @param mapModel    The parsed mindmap model
     * @param title       Optional title from the mindmap entity
     * @param description Optional description from the mindmap entity
     * @return Extracted text content
     */
    public String extractTextContent(MapModel mapModel, String title, String description) {
        StringBuilder content = new StringBuilder();

        // Add title (from entity or model)
        String effectiveTitle = title != null ? title : mapModel.getTitle();
        if (effectiveTitle != null && !effectiveTitle.trim().isEmpty()) {
            content.append(effectiveTitle).append(" ");
        }

        // Add description (from entity or model)
        String effectiveDescription = description != null ? description : mapModel.getDescription();
        if (effectiveDescription != null && !effectiveDescription.trim().isEmpty()) {
            content.append(effectiveDescription).append(" ");
        }

        // Extract text from all topics
        for (Topic topic : mapModel.getAllTopics()) {
            if (topic.getText() != null && !topic.getText().trim().isEmpty()) {
                content.append(topic.getText()).append(" ");
            }
            if (topic.getNote() != null && !topic.getNote().trim().isEmpty()) {
                // Extract plain text from note (handles HTML)
                String noteText = MindmapParser.extractPlainTextContent(topic.getNote());
                content.append(noteText).append(" ");
            }
        }

        return content.toString();
    }

    /**
     * Extracts text content from a mindmap for spam analysis.
     * This is a convenience method that parses XML if needed (for backward
     * compatibility).
     * Prefer using extractTextContent(MapModel, String, String) when you already
     * have a parsed model.
     * 
     * @param mindmap The mindmap to analyze
     * @return Extracted text content
     */
    public String extractTextContent(Mindmap mindmap) {
        StringBuilder content = new StringBuilder();

        if (mindmap.getTitle() != null) {
            content.append(mindmap.getTitle()).append(" ");
        }

        if (mindmap.getDescription() != null) {
            content.append(mindmap.getDescription()).append(" ");
        }

        try {
            String xmlContent = mindmap.getXmlStr();
            if (xmlContent != null && !xmlContent.trim().isEmpty()) {
                // Use the static mindmap parser
                content.append(MindmapParser.extractTextContent(xmlContent));
            }
        } catch (Exception e) {
            logger.warn("Error extracting text content from mindmap {}: {}", mindmap.getId(), e.getMessage());
        }

        return content.toString();
    }

    /**
     * Checks if the mindmap model contains HTML content in notes.
     * This is important for spam detection as HTML content can be used to hide spam
     * text.
     * 
     * @param mapModel The parsed mindmap model
     * @return true if HTML content is found in notes, false otherwise
     */
    public boolean hasHtmlContent(MapModel mapModel) {
        if (mapModel == null) {
            return false;
        }

        // Check all topics for HTML content in notes
        for (Topic topic : mapModel.getAllTopics()) {
            if (topic.getNote() != null && isHtmlContent(topic.getNote())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks if the mindmap contains HTML content in notes.
     * This is a convenience method for backward compatibility.
     * Prefer using hasHtmlContent(MapModel) when you already have a parsed model.
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

            // Use the static mindmap parser
            return MindmapParser.hasHtmlContent(xml);

        } catch (Exception e) {
            logger.warn("Error checking for HTML content in mindmap {}: {}", mindmap.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Validates note content length for HTML notes.
     * 
     * @param mindmap   The mindmap to validate
     * @param maxLength Maximum allowed length for note content
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

            // Use the static mindmap parser
            return MindmapUtils.validateNoteContentLength(xml, maxLength);

        } catch (Exception e) {
            logger.warn("Error validating note content length for mindmap {}: {}", mindmap.getId(), e.getMessage());
            return new NoteValidationResult(false, "Error validating content: " + e.getMessage(), 0, 0);
        }
    }

    /**
     * Counts the number of spam keywords found in the given content.
     * 
     * @param lowerContent The content to check (should be lowercase)
     * @return Number of spam keywords found
     */
    public long countSpamKeywords(String lowerContent) {
        return spamKeywords != null ? spamKeywords.stream()
                .filter(keyword -> lowerContent.contains(keyword))
                .count() : 0;
    }

    /**
     * Counts the number of spam keywords present across the entire mindmap content.
     *
     * @param mindmap The mindmap to analyze
     * @return Number of spam keywords detected
     */
    public long countSpamKeywords(Mindmap mindmap) {
        if (mindmap == null) {
            return 0;
        }
        final String content = extractTextContent(mindmap);
        if (content == null) {
            return 0;
        }
        return countSpamKeywords(content.toLowerCase());
    }

    /**
     * Determines whether the mindmap contains heavy marketing indicators based on
     * keyword matches.
     *
     * @param mindmap   Mindmap under analysis
     * @param threshold Minimum number of keyword matches required
     * @return true if heavy marketing indicators are present
     */
    public boolean hasMarketingIndicators(Mindmap mindmap, int threshold) {
        return countSpamKeywords(mindmap) >= Math.max(threshold, 1);
    }

    /**
     * Normalizes content for regex-based pattern detection by removing bullet
     * prefixes and
     * collapsing whitespace. This makes it easier for pattern strategies to
     * identify contact
     * information even when formatted with decorative characters.
     *
     * @param content Raw content to normalize
     * @return Normalized content
     */
    public String normalizeForPatternMatching(String content) {
        if (content == null) {
            return "";
        }

        String withoutBullets = Pattern.compile("(?m)^[\\s]*[•◦▪●□■▫▸►➤➔➢➣➧➨\\-*]+\\s*")
                .matcher(content)
                .replaceAll("");

        return withoutBullets.replaceAll("\\s+", " ").trim();
    }

    /**
     * Checks if the given content contains any spam keywords.
     * 
     * @param lowerContent The content to check (should be lowercase)
     * @return true if spam keywords are found, false otherwise
     */
    public boolean hasSpamKeywords(String lowerContent) {
        return spamKeywords != null && spamKeywords.stream()
                .anyMatch(keyword -> lowerContent.contains(keyword));
    }

    /**
     * Gets the list of spam keywords.
     * 
     * @return List of spam keywords
     */
    public List<String> getSpamKeywords() {
        return spamKeywords;
    }

    /**
     * Counts the number of occurrences of a substring in the given text.
     * 
     * @param text      The text to search in
     * @param substring The substring to count
     * @return Number of occurrences
     */
    public long countOccurrences(String text, String substring) {
        if (text == null || substring == null || text.isEmpty() || substring.isEmpty()) {
            return 0;
        }

        int count = 0;
        int index = 0;
        while ((index = text.indexOf(substring, index)) != -1) {
            count++;
            index += substring.length();
        }
        return count;
    }

    /**
     * Checks if the mindmap creator's email address belongs to an educational
     * institution.
     * This includes educational domains from all countries, such as:
     * - .edu (US)
     * - .edu.xx (many countries: .edu.au, .edu.br, .edu.uk, etc.)
     * - .ac.xx (academic domains: .ac.uk, .ac.jp, .ac.za, etc.)
     * - .sch.xx (school domains: .sch.uk, .sch.za, etc.)
     * - .university (generic TLD)
     * - .school (generic TLD)
     * 
     * Educational institutions are trusted and owners with these email domains are
     * very unlikely to be spammers.
     * 
     * @param mindmap The mindmap to check
     * @return true if the creator's email belongs to an educational institution,
     *         false otherwise
     */
    public boolean hasEduOrOrgEmail(Mindmap mindmap) {
        if (mindmap == null) {
            return false;
        }

        try {
            com.wisemapping.model.Account creator = mindmap.getCreator();
            if (creator == null) {
                return false;
            }

            String creatorEmail = creator.getEmail();
            if (creatorEmail == null || creatorEmail.trim().isEmpty()) {
                return false;
            }

            // Extract domain from creator's email (everything after @)
            String emailLower = creatorEmail.toLowerCase().trim();
            int atIndex = emailLower.indexOf('@');
            if (atIndex > 0 && atIndex < emailLower.length() - 1) {
                String domain = emailLower.substring(atIndex + 1);
                return isEducationalDomain(domain);
            }

            return false;
        } catch (Exception e) {
            logger.debug("Error checking creator's email domain for educational institution in mindmap {}: {}",
                    mindmap.getId(), e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a domain belongs to an educational institution.
     * Supports international educational domain patterns from various countries.
     * 
     * @param domain The domain to check (e.g., "university.edu.au",
     *               "example.ac.uk")
     * @return true if the domain appears to be from an educational institution,
     *         false otherwise
     */
    private boolean isEducationalDomain(String domain) {
        if (domain == null || domain.isEmpty()) {
            return false;
        }

        String domainLower = domain.toLowerCase();

        // Check for generic educational TLDs
        if (domainLower.endsWith(".university") || domainLower.endsWith(".school")) {
            return true;
        }

        // Check for .edu (US) or .edu.xx (international educational domains)
        // Examples: .edu, .edu.au, .edu.br, .edu.uk, .edu.mx, etc.
        if (domainLower.contains(".edu")) {
            // Match .edu at the end or .edu. followed by country code
            if (domainLower.endsWith(".edu") || domainLower.matches(".*\\.edu\\.[a-z]{2,3}(\\.[a-z]{2})?$")) {
                return true;
            }
        }

        // Check for .ac.xx (academic domains used in many countries)
        // Examples: .ac.uk, .ac.jp, .ac.za, .ac.nz, .ac.in, etc.
        if (domainLower.matches(".*\\.ac\\.[a-z]{2,3}(\\.[a-z]{2})?$")) {
            return true;
        }

        // Check for .sch.xx (school domains used in some countries)
        // Examples: .sch.uk, .sch.za, etc.
        if (domainLower.matches(".*\\.sch\\.[a-z]{2,3}(\\.[a-z]{2})?$")) {
            return true;
        }

        return false;
    }

    /**
     * Counts the number of characters in a note, handling both HTML and plain text.
     * 
     * @param noteContent The note content to count
     * @return Character count information
     */
    public NoteCharacterCount countNoteCharacters(String noteContent) {
        if (noteContent == null || noteContent.trim().isEmpty()) {
            return new NoteCharacterCount(0, 0, false, maxNoteLength);
        }

        boolean isHtml = isHtmlContent(noteContent);
        int rawLength = noteContent.length();

        // For HTML content, also count the text content length
        int textLength = rawLength;
        if (isHtml) {
            String plainTextContent = MindmapParser.extractPlainTextContent(noteContent);
            textLength = plainTextContent.length();
        }

        // Use text content length for remaining chars calculation to align with
        // frontend
        int remainingChars = maxNoteLength - textLength;

        return new NoteCharacterCount(rawLength, textLength, isHtml, remainingChars, maxNoteLength);
    }

    /**
     * Gets the note character count for a given note content.
     * 
     * @param noteContent The note content
     * @return Character count information
     */
    public NoteCharacterCount getNoteCharacterCount(String noteContent) {
        return countNoteCharacters(noteContent);
    }

    /**
     * Checks if content contains HTML markup.
     * 
     * @param content The content to check
     * @return true if content contains HTML, false otherwise
     */
    public boolean isHtmlContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }

        return content.contains("<") && content.contains(">") &&
                (content.contains("<p>") || content.contains("<div>") ||
                        content.contains("<span>") || content.contains("<a ") ||
                        content.contains("<script>") || content.contains("<iframe>") ||
                        content.contains("<img") || content.contains("<br") ||
                        content.contains("<strong>") || content.contains("<em>") ||
                        content.matches(".*<[a-zA-Z][a-zA-Z0-9]*[^>]*>.*"));
    }

    /**
     * Sanitizes HTML content by removing dangerous elements.
     * 
     * @param content The HTML content to sanitize
     * @return Sanitized content
     */
    public String sanitizeHtmlContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return content;
        }

        try {
            return MindmapParser.sanitizeHtmlContent(content);
        } catch (Exception e) {
            logger.warn("Error sanitizing HTML content: {}", e.getMessage());
            return content;
        }
    }

    /**
     * Counts unique keyword types in content.
     * 
     * @param content The content to analyze
     * @return Number of unique keyword types found
     */
    public int countUniqueKeywordTypes(String content) {
        if (content == null || content.trim().isEmpty()) {
            return 0;
        }

        String lowerContent = content.toLowerCase();
        return (int) spamKeywords.stream()
                .filter(keyword -> lowerContent.contains(keyword))
                .count();
    }

    /**
     * Result class for note character counting.
     */
    public static class NoteCharacterCount {
        private final int rawLength;
        private final int textLength;
        private final boolean isHtml;
        private final int remainingChars;
        private final int maxLength;

        public NoteCharacterCount(int rawLength, int textLength, boolean isHtml, int remainingChars, int maxLength) {
            this.rawLength = rawLength;
            this.textLength = textLength;
            this.isHtml = isHtml;
            this.remainingChars = remainingChars;
            this.maxLength = maxLength;
        }

        public NoteCharacterCount(int rawLength, int textLength, boolean isHtml, int maxLength) {
            this.rawLength = rawLength;
            this.textLength = textLength;
            this.isHtml = isHtml;
            this.remainingChars = maxLength - rawLength;
            this.maxLength = maxLength;
        }

        public int getRawLength() {
            return rawLength;
        }

        public int getTextLength() {
            return textLength;
        }

        public boolean isHtml() {
            return isHtml;
        }

        public int getRemainingChars() {
            return remainingChars;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public boolean isOverLimit() {
            return textLength > maxLength;
        }

        public double getUsagePercentage() {
            return maxLength > 0 ? (textLength / (double) maxLength) * 100.0 : 0.0;
        }
    }
}