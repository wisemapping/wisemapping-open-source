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
    CONTACT_INFO("CI", "ContactInfo"),
    
    // Few Nodes with Content Spam
    FEW_NODES("FN", "FewNodesWithContent"),
    
    // User Behavior Spam
    USER_BEHAVIOR("UB", "UserBehavior"),
    
    // Keyword Pattern Spam (currently disabled)
    KEYWORD_PATTERN("KP", "KeywordPattern"),
    
    // Unknown/Generic spam
    UNKNOWN("UN", "Unknown");
    
    private final String code;
    private final String strategyName;
    
    SpamStrategyType(String code, String strategyName) {
        this.code = code;
        this.strategyName = strategyName;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getStrategyName() {
        return strategyName;
    }
    
    
    /**
     * Get spam strategy type from code string
     * @param code The short code
     * @return The corresponding spam strategy type, or UNKNOWN if not found
     */
    public static SpamStrategyType fromCode(String code) {
        if (code == null) {
            return UNKNOWN;
        }
        
        for (SpamStrategyType spamType : values()) {
            if (spamType.code.equals(code)) {
                return spamType;
            }
        }
        
        return UNKNOWN;
    }
}
