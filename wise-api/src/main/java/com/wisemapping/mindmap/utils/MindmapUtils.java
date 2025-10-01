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

package com.wisemapping.mindmap.utils;

import com.wisemapping.mindmap.model.MindmapData;
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.parser.MindmapParser;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for mindmap operations and transformations.
 * This class provides common operations that can be performed on mindmaps.
 */
public class MindmapUtils {
    
    private static final int MAX_SUPPORTED_NODES = 4000;
    
    /**
     * Validates the basic structure of mindmap XML content.
     * 
     * @param xmlContent The XML content to validate
     * @throws MindmapValidationException if validation fails
     */
    public static void validateXmlContent(@NotNull String xmlContent) throws MindmapValidationException {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw MindmapValidationException.emptyMindmap();
        }
        
        // Perform basic structure validation
        if (!xmlContent.trim().endsWith("</map>") || !xmlContent.trim().startsWith("<map")) {
            throw MindmapValidationException.invalidFormat(xmlContent);
        }
        
        int numberOfTopics = xmlContent.split("<topic").length;
        if (numberOfTopics == 0) {
            throw MindmapValidationException.invalidFormat(xmlContent);
        }
        
        if (numberOfTopics > MAX_SUPPORTED_NODES) {
            throw MindmapValidationException.tooBigMindmap(numberOfTopics);
        }
    }
    
    /**
     * Extracts text content from mindmap XML for analysis purposes.
     * 
     * @param xmlContent The XML content to analyze
     * @return Extracted text content
     */
    @NotNull
    public static String extractTextContent(@NotNull String xmlContent) {
        return MindmapParser.extractTextContent(xmlContent);
    }
    
    /**
     * Checks if the mindmap contains HTML content in notes.
     * 
     * @param xmlContent The XML content to check
     * @return true if HTML content is found, false otherwise
     */
    public static boolean hasHtmlContent(@NotNull String xmlContent) {
        return MindmapParser.hasHtmlContent(xmlContent);
    }
    
    /**
     * Parses mindmap XML into a structured representation.
     * 
     * @param xmlContent The XML content to parse
     * @return Parsed mindmap structure
     * @throws MindmapValidationException if parsing fails
     */
    @NotNull
    public static MapModel parseMindmap(@NotNull String xmlContent) throws MindmapValidationException {
        return MindmapParser.parseXml(xmlContent);
    }
    
    /**
     * Creates a MindmapData object from XML content.
     * 
     * @param title The title for the mindmap
     * @param xmlContent The XML content
     * @return A new MindmapData instance
     * @throws MindmapValidationException if the XML is invalid
     */
    @NotNull
    public static MindmapData createMindmapData(@NotNull String title, @NotNull String xmlContent) throws MindmapValidationException {
        validateXmlContent(xmlContent);
        return new MindmapData(title, xmlContent);
    }
    
    /**
     * Gets all text content from a mindmap structure.
     * 
     * @param structure The mindmap structure
     * @return List of all text content
     */
    @NotNull
    public static List<String> getAllTextContent(@NotNull MapModel structure) {
        return structure.getAllTopics().stream()
                .map(Topic::getEffectiveText)
                .filter(text -> text != null && !text.trim().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all note content from a mindmap structure.
     * 
     * @param structure The mindmap structure
     * @return List of all note content
     */
    @NotNull
    public static List<String> getAllNoteContent(@NotNull MapModel structure) {
        return structure.getAllTopics().stream()
                .map(Topic::getNoteContent)
                .filter(note -> note != null && !note.trim().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all link URLs from a mindmap structure.
     * 
     * @param structure The mindmap structure
     * @return List of all link URLs
     */
    @NotNull
    public static List<String> getAllLinkUrls(@NotNull MapModel structure) {
        return structure.getAllTopics().stream()
                .map(Topic::getLinkUrl)
                .filter(url -> url != null && !url.trim().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Counts the total number of nodes in a mindmap structure.
     * 
     * @param structure The mindmap structure
     * @return Total node count
     */
    public static int countNodes(@NotNull MapModel structure) {
        return structure.getTotalTopicCount();
    }
    
    /**
     * Counts the number of nodes with content (text, notes, or links).
     * 
     * @param structure The mindmap structure
     * @return Number of nodes with content
     */
    public static int countNodesWithContent(@NotNull MapModel structure) {
        return (int) structure.getAllTopics().stream()
                .filter(Topic::hasContent)
                .count();
    }
    
    /**
     * Counts the number of nodes with notes.
     * 
     * @param structure The mindmap structure
     * @return Number of nodes with notes
     */
    public static int countNodesWithNotes(@NotNull MapModel structure) {
        return (int) structure.getAllTopics().stream()
                .filter(node -> node.getNoteContent() != null && !node.getNoteContent().trim().isEmpty())
                .count();
    }
    
    /**
     * Counts the number of nodes with links.
     * 
     * @param structure The mindmap structure
     * @return Number of nodes with links
     */
    public static int countNodesWithLinks(@NotNull MapModel structure) {
        return (int) structure.getAllTopics().stream()
                .filter(node -> node.getLinkUrl() != null && !node.getLinkUrl().trim().isEmpty())
                .count();
    }
    
    /**
     * Escapes XML attribute values.
     * 
     * @param value The value to escape
     * @return Escaped value safe for XML attributes
     */
    @NotNull
    public static String escapeXmlAttribute(@NotNull String value) {
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
    
    /**
     * Unescapes XML attribute values.
     * 
     * @param value The value to unescape
     * @return Unescaped value
     */
    @NotNull
    public static String unescapeXmlAttribute(@NotNull String value) {
        return value.replace("&amp;", "&")
                   .replace("&lt;", "<")
                   .replace("&gt;", ">")
                   .replace("&quot;", "\"")
                   .replace("&#39;", "'");
    }
    
    /**
     * Fallback method for text extraction when XML parsing fails.
     */
    @NotNull
    private static String extractTextContentFallback(@NotNull String xmlContent) {
        StringBuilder text = new StringBuilder();
        
        // Extract text from text attributes
        java.util.regex.Pattern textPattern = java.util.regex.Pattern.compile("text=\"([^\"]*?)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = textPattern.matcher(xmlContent);
        while (matcher.find()) {
            text.append(matcher.group(1)).append(" ");
        }
        
        // Extract content from note tags
        java.util.regex.Pattern notePattern = java.util.regex.Pattern.compile("<note[^>]*>\\s*<!\\[CDATA\\[([^\\]]*?)\\]\\]>\\s*</note>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        matcher = notePattern.matcher(xmlContent);
        while (matcher.find()) {
            text.append(matcher.group(1)).append(" ");
        }
        
        // Extract URLs from link attributes
        java.util.regex.Pattern linkPattern = java.util.regex.Pattern.compile("url=\"([^\"]*?)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        matcher = linkPattern.matcher(xmlContent);
        while (matcher.find()) {
            text.append(matcher.group(1)).append(" ");
        }
        
        return text.toString().trim();
    }
    
    /**
     * Fallback method for HTML content detection when XML parsing fails.
     */
    private static boolean hasHtmlContentFallback(@NotNull String xmlContent) {
        java.util.regex.Pattern notePattern = java.util.regex.Pattern.compile("<note[^>]*>\\s*<!\\[CDATA\\[([^\\]]*?)\\]\\]>\\s*</note>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = notePattern.matcher(xmlContent);
        while (matcher.find()) {
            String noteContent = matcher.group(1);
            if (isHtmlContent(noteContent)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Validates note content length for HTML notes.
     * 
     * @param xmlContent The XML content to validate
     * @param maxLength Maximum allowed length for note content
     * @return Validation result with details about any violations
     */
    @NotNull
    public static NoteValidationResult validateNoteContentLength(@NotNull String xmlContent, int maxLength) {
        try {
            MindmapParser parser = new MindmapParser();
            MapModel mapModel = parser.parseXml(xmlContent);
            return validateNoteContentLengthFromModel(mapModel, maxLength);
        } catch (MindmapValidationException e) {
            // Fallback to regex-based validation
            return validateNoteContentLengthFallback(xmlContent, maxLength);
        }
    }
    
    /**
     * Validates note content length using parsed mindmap model.
     */
    @NotNull
    private static NoteValidationResult validateNoteContentLengthFromModel(@NotNull MapModel mapModel, int maxLength) {
        int totalNotes = 0;
        int oversizedNotes = 0;
        StringBuilder violations = new StringBuilder();
        
        for (Topic topic : mapModel.getAllTopics()) {
            String noteContent = topic.getNote();
            if (noteContent != null && !noteContent.trim().isEmpty()) {
                totalNotes++;
                
                if (isHtmlContent(noteContent)) {
                    // For HTML content, count characters in the text content (stripped of HTML tags)
                    String plainTextContent = MindmapParser.extractPlainTextContent(noteContent);
                    int textLength = plainTextContent.length();
                    if (textLength > maxLength) {
                        oversizedNotes++;
                        violations.append(String.format("HTML note %d: %d chars (limit: %d), ", totalNotes, textLength, maxLength));
                    }
                } else {
                    // For plain text, count characters in the text content
                    int textLength = noteContent.length();
                    if (textLength > maxLength) {
                        oversizedNotes++;
                        violations.append(String.format("Text note %d: %d chars (limit: %d), ", totalNotes, textLength, maxLength));
                    }
                }
            }
        }
        
        boolean isValid = oversizedNotes == 0;
        String violationDetails = violations.length() > 0 ? violations.toString() : "";
        
        return new NoteValidationResult(isValid, violationDetails, totalNotes, oversizedNotes);
    }
    
    /**
     * Fallback method using regex to validate note content length.
     */
    @NotNull
    private static NoteValidationResult validateNoteContentLengthFallback(@NotNull String xmlContent, int maxLength) {
        int totalNotes = 0;
        int oversizedNotes = 0;
        StringBuilder violations = new StringBuilder();
        
        // Check CDATA note content
        java.util.regex.Pattern notePattern = java.util.regex.Pattern.compile("<note[^>]*>\\s*<!\\[CDATA\\[([^\\]]*?)\\]\\]>\\s*</note>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = notePattern.matcher(xmlContent);
        while (matcher.find()) {
            totalNotes++;
            String noteContent = matcher.group(1);
            
            if (isHtmlContent(noteContent)) {
                int htmlLength = noteContent.length();
                if (htmlLength > maxLength) {
                    oversizedNotes++;
                    violations.append(String.format("HTML note %d: %d chars (limit: %d), ", totalNotes, htmlLength, maxLength));
                }
            } else {
                int textLength = noteContent.length();
                if (textLength > maxLength) {
                    oversizedNotes++;
                    violations.append(String.format("Text note %d: %d chars (limit: %d), ", totalNotes, textLength, maxLength));
                }
            }
        }
        
        boolean isValid = oversizedNotes == 0;
        String violationDetails = violations.length() > 0 ? violations.toString() : "";
        
        return new NoteValidationResult(isValid, violationDetails, totalNotes, oversizedNotes);
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
     * Determines if content contains HTML markup.
     */
    private static boolean isHtmlContent(@NotNull String content) {
        if (content.trim().isEmpty()) {
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
}
