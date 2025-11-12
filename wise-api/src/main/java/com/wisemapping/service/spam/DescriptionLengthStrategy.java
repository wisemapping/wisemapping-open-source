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
import com.wisemapping.model.SpamStrategyType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.batch.spam-detection.description-length.enabled", havingValue = "true", matchIfMissing = true)
public class DescriptionLengthStrategy implements SpamDetectionStrategy {

    private final SpamContentExtractor contentExtractor;
    
    @Value("${app.batch.spam-detection.min-nodes-exemption:15}")
    private int minNodesExemption;
    
    @Value("${app.batch.spam-detection.max-description-length:200}")
    private int maxDescriptionLength;

    private static final int MARKETING_KEYWORD_THRESHOLD = 3;

    public DescriptionLengthStrategy(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }

    @Override
    public SpamDetectionResult detectSpam(SpamDetectionContext context) {
        if (context == null || context.getMindmap() == null || context.getMapModel() == null) {
            return SpamDetectionResult.notSpam();
        }

        String description = context.getDescription();
        String title = context.getTitle();
        
        // If there's no description, can't be spam by description criteria
        if (description == null || description.trim().isEmpty()) {
            return SpamDetectionResult.notSpam();
        }

        MapModel mapModel = context.getMapModel();
        
        // Extract content to check for marketing keywords
        String content = contentExtractor.extractTextContent(mapModel, title, description);
        final boolean marketingHeavy = contentExtractor.countSpamKeywords(content.toLowerCase()) >= MARKETING_KEYWORD_THRESHOLD;

        // Check node count first - any mindmap with more than the configured threshold is considered legitimate content (not spam)
        int topicCount = mapModel.getTotalTopicCount();
        if (topicCount > minNodesExemption && !marketingHeavy) {
            return SpamDetectionResult.notSpam();
        }

        String descriptionTrimmed = description.trim();
        int descriptionLength = descriptionTrimmed.length();
        
        // Rule 1: Check if description is suspiciously long
        if (descriptionLength > maxDescriptionLength) {
            // Additional check: If title is contained in description, it's more likely spam
            if (title != null && !title.trim().isEmpty()) {
                String titleLower = title.trim().toLowerCase();
                String descriptionLower = descriptionTrimmed.toLowerCase();
                
                if (descriptionLower.contains(titleLower)) {
                    return SpamDetectionResult.spam(
                        "Long description with title duplication detected",
                        String.format("Description length: %d (max: %d), Title '%s' found in description", 
                                    descriptionLength, maxDescriptionLength, title),
                        getType()
                    );
                }
            }
            
            return SpamDetectionResult.spam(
                "Description exceeds maximum length",
                String.format("Description length: %d (max: %d), Content: '%s'", 
                            descriptionLength, maxDescriptionLength, 
                            descriptionTrimmed.substring(0, Math.min(100, descriptionLength)) + "..."),
                getType()
            );
        }
        
        // Rule 2: Title contained in description (even for shorter descriptions)
        // This is a common spam pattern where they duplicate the business name/title in description
        if (title != null && !title.trim().isEmpty() && descriptionLength > 50) {
            String titleLower = title.trim().toLowerCase();
            String descriptionLower = descriptionTrimmed.toLowerCase();
            
            if (descriptionLower.contains(titleLower)) {
                return SpamDetectionResult.spam(
                    "Title text found in description - possible spam pattern",
                    String.format("Title: '%s', Description length: %d, Content: '%s'", 
                                title, descriptionLength, descriptionTrimmed),
                    getType()
                );
            }
        }

        return SpamDetectionResult.notSpam();
    }

    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.DESCRIPTION_LENGTH;
    }
}

