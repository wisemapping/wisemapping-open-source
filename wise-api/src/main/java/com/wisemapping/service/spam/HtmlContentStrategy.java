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
 * Note: Security validation (dangerous HTML patterns) is handled by
 * HtmlContentValidator
 * at save time, not by this spam detection strategy.
 */
@Component
public class HtmlContentStrategy implements SpamDetectionStrategy {

    private final static Logger logger = LogManager.getLogger();
    private final SpamContentExtractor contentExtractor;

    @Value("${app.batch.spam-detection.html.max-ratio:0.7}")
    private double maxHtmlToTextRatio;

    @Value("${app.mindmap.note.max-length:5000}")
    private int maxNoteLength;

    // Patterns that indicate HTML-based spam (not security threats)
    private static final List<Pattern> HTML_SPAM_PATTERNS = Arrays.asList(
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

            // Check HTML to text ratio
            SpamDetectionResult ratioResult = checkHtmlToTextRatio(htmlContent);
            if (ratioResult.isSpam()) {
                return ratioResult;
            }

            // Note: Security-related tag checking is handled by HtmlContentValidator at
            // save time

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
     * Counts only suspicious HTML elements, excluding common text formatting tags.
     * Text formatting tags (h1-h6, p, b, strong, i, em, u, ul, ol, li, br, span,
     * div, etc.)
     * are considered legitimate and are not counted.
     * 
     * @param htmlContent The HTML content to analyze
     * @return Count of suspicious HTML elements
     */
    private long countSuspiciousHtmlElements(String htmlContent) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return 0;
        }

        // Pattern to match HTML tags, capturing the tag name
        Pattern tagPattern = Pattern.compile("<(/?)([a-zA-Z][a-zA-Z0-9]*)[^>]*>", Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = tagPattern.matcher(htmlContent);

        // Set of legitimate text formatting tags that should not be counted
        java.util.Set<String> formattingTags = java.util.Set.of(
                "h1", "h2", "h3", "h4", "h5", "h6", // Headings
                "p", // Paragraphs
                "b", "strong", // Bold
                "i", "em", // Italic
                "u", // Underline
                "ul", "ol", "li", // Lists
                "br", // Line breaks
                "span", // Spans for styling/highlighting
                "div", // Containers for layout
                "article", "section", "header", "footer", "nav", "main", // Semantic containers
                "blockquote", // Quotes
                "code", "pre", // Code blocks
                "sub", "sup", // Subscript/superscript
                "mark", // Highlighting
                "small", "big", // Size modifiers
                "del", "ins", "s", "strike", // Deleted/inserted/strikethrough text
                "abbr", "dfn", "kbd", "samp", "var", // Text semantics
                "cite", "q", // Citations and quotes
                "time", // Time elements
                "wbr", // Word break opportunities
                "img", // Images - handled by HtmlContentValidator, not considered spam here
                "font" // Font styling
        );

        long suspiciousCount = 0;
        while (matcher.find()) {
            String tagName = matcher.group(2).toLowerCase();
            // Only count tags that are NOT in the formatting tags set
            if (!formattingTags.contains(tagName)) {
                suspiciousCount++;
            }
        }

        return suspiciousCount;
    }

    /**
     * Checks if the HTML to text ratio is suspiciously high.
     * Only counts suspicious HTML elements (excludes text formatting tags).
     */
    private SpamDetectionResult checkHtmlToTextRatio(String htmlContent) {
        // Extract plain text from HTML
        String plainText = contentExtractor.sanitizeHtmlContent(htmlContent);

        // Calculate ratio of suspicious HTML tags to text (exclude formatting tags)
        long suspiciousHtmlTagCount = countSuspiciousHtmlElements(htmlContent);

        if (plainText.trim().isEmpty()) {
            // If there's HTML but no extractable text
            // Check if we have suspicious tags. If not (e.g. only images), it's acceptable.
            if (suspiciousHtmlTagCount == 0) {
                return SpamDetectionResult.notSpam();
            }

            String snippet = htmlContent.length() > 100 ? htmlContent.substring(0, 100) + "..." : htmlContent;
            return new SpamDetectionResult(true,
                    "HTML content with no extractable text",
                    "HTML content contains no readable text. This is often used to hide keywords or links (like CSS hidden text or excessive empty tags) that are visible to bots but not users. Snippet: "
                            + snippet,
                    SpamStrategyType.HTML_CONTENT);
        }

        int textLength = plainText.length();

        if (textLength == 0) {
            // This case should be covered by the trim().isEmpty() check above,
            // but for safety if sanitization results in empty string differently

            if (suspiciousHtmlTagCount == 0) {
                return SpamDetectionResult.notSpam();
            }

            return new SpamDetectionResult(true,
                    "HTML content with zero text length",
                    "HTML content has no readable text after sanitization. This is a strong indicator of spam attempting to bypass text-based filters.",
                    SpamStrategyType.HTML_CONTENT);
        }

        double htmlToTextRatio = (double) suspiciousHtmlTagCount / textLength;

        if (htmlToTextRatio > maxHtmlToTextRatio) {
            return new SpamDetectionResult(true,
                    "High HTML to text ratio",
                    String.format(
                            "Suspicious HTML to text ratio: %.2f, threshold: %.2f (suspicious tags: %d, text length: %d)",
                            htmlToTextRatio, maxHtmlToTextRatio, suspiciousHtmlTagCount, textLength),
                    SpamStrategyType.HTML_CONTENT);
        }

        return SpamDetectionResult.notSpam();
    }

    /**
     * Checks if any note content exceeds the maximum allowed length.
     * This prevents spam by limiting the size of individual notes.
     */
    private SpamDetectionResult checkNoteContentLength(Mindmap mindmap) {
        com.wisemapping.mindmap.utils.MindmapUtils.NoteValidationResult validationResult = contentExtractor
                .validateNoteContentLength(mindmap, maxNoteLength);

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
