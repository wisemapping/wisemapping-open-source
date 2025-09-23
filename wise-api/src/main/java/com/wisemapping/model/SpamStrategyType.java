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

package com.wisemapping.model;

/**
 * Enum representing spam strategy types for database storage.
 * These are short codes that map to spam detection strategies.
 */
public enum SpamStrategyType {
    
    // Contact Information Spam
    CONTACT_INFO('C', "ContactInfo"),
    
    // Few Nodes with Content Spam
    FEW_NODES('F', "FewNodesWithContent"),
    
    // User Behavior Spam
    USER_BEHAVIOR('U', "UserBehavior"),
    
    // Keyword Pattern Spam (currently disabled)
    KEYWORD_PATTERN('K', "KeywordPattern"),
    
    // HTML Content Spam
    HTML_CONTENT('H', "HtmlContent"),
    
    // Unknown/Generic spam
    UNKNOWN('X', "Unknown");
    
    private final char code;
    private final String strategyName;
    
    SpamStrategyType(char code, String strategyName) {
        this.code = code;
        this.strategyName = strategyName;
    }
    
    public char getCode() {
        return code;
    }
    
    public String getStrategyName() {
        return strategyName;
    }
    
    
    /**
     * Get spam strategy type from code char
     * @param code The short code
     * @return The corresponding spam strategy type, or UNKNOWN if not found
     */
    public static SpamStrategyType fromCode(char code) {
        for (SpamStrategyType spamType : values()) {
            if (spamType.code == code) {
                return spamType;
            }
        }
        
        return UNKNOWN;
    }
    
    /**
     * Get spam strategy type from code string (for backward compatibility)
     * @param code The short code string
     * @return The corresponding spam strategy type, or UNKNOWN if not found
     */
    public static SpamStrategyType fromCode(String code) {
        if (code == null || code.length() != 1) {
            return UNKNOWN;
        }
        
        return fromCode(code.charAt(0));
    }
}
