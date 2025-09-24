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

package com.wisemapping.exceptions;

/**
 * Exception thrown when HTML content validation fails.
 * This exception provides specific details about what validation rule was violated.
 */
public class HtmlContentValidationException extends WiseMappingException {
    
    private final String validationType;
    private final String detectedContent;
    
    /**
     * Creates a new HTML content validation exception.
     * 
     * @param message The error message
     * @param validationType The type of validation that failed (e.g., "LENGTH", "SECURITY")
     * @param detectedContent The specific content that caused the validation failure
     */
    public HtmlContentValidationException(String message, String validationType, String detectedContent) {
        super(message);
        this.validationType = validationType;
        this.detectedContent = detectedContent;
    }
    
    /**
     * Creates a new HTML content validation exception without detected content.
     * 
     * @param message The error message
     * @param validationType The type of validation that failed (e.g., "LENGTH", "SECURITY")
     */
    public HtmlContentValidationException(String message, String validationType) {
        super(message);
        this.validationType = validationType;
        this.detectedContent = null;
    }
    
    /**
     * Gets the type of validation that failed.
     * 
     * @return The validation type (e.g., "LENGTH", "SECURITY")
     */
    public String getValidationType() {
        return validationType;
    }
    
    /**
     * Gets the specific content that caused the validation failure.
     * 
     * @return The detected content, or null if not applicable
     */
    public String getDetectedContent() {
        return detectedContent;
    }
    
    /**
     * Gets a user-friendly error message with specific details.
     * 
     * @return A formatted error message
     */
    public String getUserFriendlyMessage() {
        if (detectedContent != null) {
            return String.format("%s (Detected: %s)", getMessage(), detectedContent);
        }
        return getMessage();
    }
}
