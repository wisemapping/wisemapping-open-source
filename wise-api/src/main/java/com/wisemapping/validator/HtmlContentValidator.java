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

package com.wisemapping.validator;

import com.wisemapping.exceptions.HtmlContentValidationException;
import com.wisemapping.exceptions.InvalidMindmapException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Mindmap;
import com.wisemapping.service.spam.SpamContentExtractor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Validates HTML content in mindmap notes for security and length constraints.
 */
@Component
public class HtmlContentValidator {
    
    private final static Logger logger = LogManager.getLogger();
    private final SpamContentExtractor contentExtractor;
    
    private static final int MAX_NOTE_LENGTH = 1000;
    
    @Autowired
    public HtmlContentValidator(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }
    
    /**
     * Validates HTML content in mindmap notes.
     * 
     * @param mindmap The mindmap to validate
     * @throws WiseMappingException if validation fails
     */
    public void validateHtmlContent(Mindmap mindmap) throws WiseMappingException {
        if (mindmap == null) {
            return;
        }
        
        try {
            String xml = mindmap.getXmlStr();
            if (xml == null || xml.trim().isEmpty()) {
                return;
            }
            
            // Validate note content length
            SpamContentExtractor.NoteValidationResult validationResult = 
                contentExtractor.validateNoteContentLength(mindmap, MAX_NOTE_LENGTH);
            
            if (!validationResult.isValid()) {
                String errorMessage = String.format(
                    "Note content exceeds maximum length of %d characters. %s", 
                    MAX_NOTE_LENGTH, 
                    validationResult.getViolationDetails()
                );
                logger.warn("HTML content validation failed for mindmap {}: {}", mindmap.getId(), errorMessage);
                throw new HtmlContentValidationException(errorMessage, "LENGTH");
            }
            
            // Additional security validations can be added here
            validateHtmlSecurity(xml);
            
        } catch (HtmlContentValidationException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Error validating HTML content for mindmap {}: {}", mindmap.getId(), e.getMessage());
            throw new HtmlContentValidationException("Error validating HTML content: " + e.getMessage(), "UNKNOWN");
        }
    }
    
    /**
     * Validates HTML content for security issues.
     * 
     * @param xml The XML content to validate
     * @throws HtmlContentValidationException if security issues are found
     */
    private void validateHtmlSecurity(String xml) throws HtmlContentValidationException {
        // Check for dangerous HTML patterns
        String dangerousPattern = getDangerousHtmlPattern(xml);
        if (dangerousPattern != null) {
            String errorMessage = String.format(
                "HTML content contains potentially dangerous patterns: %s. " +
                "This content is blocked for security reasons. Please remove or modify the content and try again.",
                dangerousPattern
            );
            logger.warn("HTML security validation failed: {}", dangerousPattern);
            throw new HtmlContentValidationException(errorMessage, "SECURITY", dangerousPattern);
        }
    }
    
    /**
     * Gets the specific dangerous HTML pattern found in the XML content.
     * URLs are allowed since mindmaps can contain legitimate links on nodes.
     * 
     * @param xml The XML content to check
     * @return String describing the dangerous pattern found, or null if none found
     */
    private String getDangerousHtmlPattern(String xml) {
        // Check for script tags
        if (xml.contains("<script") || xml.contains("</script>")) {
            return "script tags (<script> or </script>) - these can execute malicious code";
        }
        
        // Check for iframe tags
        if (xml.contains("<iframe") || xml.contains("</iframe>")) {
            return "iframe tags (<iframe>) - these can embed external content";
        }
        
        // Check for object/embed tags
        if (xml.contains("<object")) {
            return "object tags (<object>) - these can embed external content";
        }
        if (xml.contains("<embed")) {
            return "embed tags (<embed>) - these can embed external content";
        }
        
        // Check for form tags
        if (xml.contains("<form") || xml.contains("</form>")) {
            return "form tags (<form>) - these can submit data to external sites";
        }
        
        // Check for javascript: URLs (dangerous)
        if (xml.toLowerCase().contains("javascript:")) {
            return "javascript: URLs - these can execute malicious code";
        }
        
        // Check for event handlers
        String eventHandler = detectSpecificEventHandler(xml);
        if (eventHandler != null) {
            return eventHandler;
        }
        
        // Note: Regular URLs (http://, https://, etc.) are allowed
        // as mindmaps can legitimately contain links on nodes
        
        return null;
    }
    
    /**
     * Detects specific event handlers in the XML content and returns detailed information.
     * 
     * @param xml The XML content to check
     * @return String describing the specific event handler found, or null if none found
     */
    private String detectSpecificEventHandler(String xml) {
        java.util.regex.Pattern eventPattern = java.util.regex.Pattern.compile(
            "(on\\w+)\\s*=\\s*[\"']([^\"']*)[\"']", 
            java.util.regex.Pattern.CASE_INSENSITIVE
        );
        
        java.util.regex.Matcher matcher = eventPattern.matcher(xml);
        if (matcher.find()) {
            String eventName = matcher.group(1);
            String eventCode = matcher.group(2);
            
            // Truncate long event code for display
            String displayCode = eventCode.length() > 50 ? 
                eventCode.substring(0, 47) + "..." : eventCode;
            
            return String.format("event handler '%s' with code '%s' - these can execute malicious code", 
                eventName, displayCode);
        }
        
        return null;
    }
    
    /**
     * Checks if the XML contains dangerous HTML patterns.
     * URLs are allowed since mindmaps can contain legitimate links on nodes.
     * 
     * @param xml The XML content to check
     * @return true if dangerous patterns are found
     */
    private boolean containsDangerousHtmlPatterns(String xml) {
        return getDangerousHtmlPattern(xml) != null;
    }
    
    /**
     * Validates a single note content string.
     * 
     * @param noteContent The note content to validate
     * @throws HtmlContentValidationException if validation fails
     */
    public void validateNoteContent(String noteContent) throws HtmlContentValidationException {
        if (noteContent == null || noteContent.trim().isEmpty()) {
            return;
        }
        
        // Check length
        if (noteContent.length() > MAX_NOTE_LENGTH) {
            String errorMessage = String.format(
                "Note content exceeds maximum length of %d characters (found %d characters). " +
                "Please shorten the content and try again.",
                MAX_NOTE_LENGTH, noteContent.length()
            );
            logger.warn("Note content length validation failed: {} characters (max: {})", 
                noteContent.length(), MAX_NOTE_LENGTH);
            throw new HtmlContentValidationException(errorMessage, "LENGTH", 
                String.format("%d characters (limit: %d)", noteContent.length(), MAX_NOTE_LENGTH));
        }
        
        // Check for dangerous patterns
        String dangerousPattern = getDangerousHtmlPattern(noteContent);
        if (dangerousPattern != null) {
            String errorMessage = String.format(
                "Note content contains potentially dangerous HTML patterns: %s. " +
                "This content is blocked for security reasons. Please remove or modify the content and try again.",
                dangerousPattern
            );
            logger.warn("Note content security validation failed: {}", dangerousPattern);
            throw new HtmlContentValidationException(errorMessage, "SECURITY", dangerousPattern);
        }
    }
}
