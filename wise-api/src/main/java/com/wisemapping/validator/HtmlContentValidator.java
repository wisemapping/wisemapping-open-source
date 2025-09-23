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
                throw new WiseMappingException(errorMessage);
            }
            
            // Additional security validations can be added here
            validateHtmlSecurity(xml);
            
        } catch (InvalidMindmapException e) {
            throw e;
        } catch (Exception e) {
            logger.warn("Error validating HTML content for mindmap {}: {}", mindmap.getId(), e.getMessage());
            throw new WiseMappingException("Error validating HTML content: " + e.getMessage());
        }
    }
    
    /**
     * Validates HTML content for security issues.
     * 
     * @param xml The XML content to validate
     * @throws WiseMappingException if security issues are found
     */
    private void validateHtmlSecurity(String xml) throws WiseMappingException {
        // Check for dangerous HTML patterns
        if (containsDangerousHtmlPatterns(xml)) {
            throw new WiseMappingException("HTML content contains potentially dangerous patterns");
        }
    }
    
    /**
     * Checks if the XML contains dangerous HTML patterns.
     * URLs are allowed since mindmaps can contain legitimate links on nodes.
     * 
     * @param xml The XML content to check
     * @return true if dangerous patterns are found
     */
    private boolean containsDangerousHtmlPatterns(String xml) {
        // Check for script tags
        if (xml.contains("<script") || xml.contains("</script>")) {
            return true;
        }
        
        // Check for iframe tags
        if (xml.contains("<iframe") || xml.contains("</iframe>")) {
            return true;
        }
        
        // Check for object/embed tags
        if (xml.contains("<object") || xml.contains("<embed")) {
            return true;
        }
        
        // Check for form tags
        if (xml.contains("<form") || xml.contains("</form>")) {
            return true;
        }
        
        // Check for javascript: URLs (dangerous)
        if (xml.toLowerCase().contains("javascript:")) {
            return true;
        }
        
        // Check for event handlers
        if (xml.matches(".*on\\w+\\s*=\\s*[\"'][^\"']*[\"'].*")) {
            return true;
        }
        
        // Note: Regular URLs (http://, https://, etc.) are allowed
        // as mindmaps can legitimately contain links on nodes
        
        return false;
    }
    
    /**
     * Validates a single note content string.
     * 
     * @param noteContent The note content to validate
     * @throws WiseMappingException if validation fails
     */
    public void validateNoteContent(String noteContent) throws WiseMappingException {
        if (noteContent == null || noteContent.trim().isEmpty()) {
            return;
        }
        
        // Check length
        if (noteContent.length() > MAX_NOTE_LENGTH) {
            throw new WiseMappingException(
                String.format("Note content exceeds maximum length of %d characters (found %d)", 
                    MAX_NOTE_LENGTH, noteContent.length())
            );
        }
        
        // Check for dangerous patterns
        if (containsDangerousHtmlPatterns(noteContent)) {
            throw new WiseMappingException("Note content contains potentially dangerous HTML patterns");
        }
    }
}
