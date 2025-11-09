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
import org.springframework.stereotype.Component;

@Component
public class FewNodesWithContentStrategy implements SpamDetectionStrategy {

    private final SpamContentExtractor contentExtractor;
    
    @Value("${app.batch.spam-detection.min-nodes-exemption:15}")
    private int minNodesExemption;

    private static final int MARKETING_KEYWORD_THRESHOLD = 3;

    public FewNodesWithContentStrategy(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }

    @Override
    public SpamDetectionResult detectSpam(SpamDetectionContext context) {
        if (context == null || context.getMindmap() == null || context.getMapModel() == null) {
            return SpamDetectionResult.notSpam();
        }

        MapModel mapModel = context.getMapModel();
        
        // Extract content to check for marketing keywords
        String content = contentExtractor.extractTextContent(mapModel, 
                                                              context.getTitle(), 
                                                              context.getDescription());
        final boolean marketingHeavy = contentExtractor.countSpamKeywords(content.toLowerCase()) >= MARKETING_KEYWORD_THRESHOLD;
        
        int topicCount = mapModel.getTotalTopicCount();
        
        // Any mindmap with more than the configured threshold is considered legitimate content (not spam)
        if (topicCount > minNodesExemption && !marketingHeavy) {
            return SpamDetectionResult.notSpam();
        }

        // For maps with 2-3 nodes, check if they have links or notes
        if (topicCount <= 3) {
            boolean hasLinks = mapModel.countTopicsWithLinks() > 0;
            boolean hasNotes = mapModel.countTopicsWithNotes() > 0;
            if (hasLinks || hasNotes) {
                String reason = hasLinks && hasNotes ? "Few nodes with links, notes and spam keywords" :
                               hasLinks ? "Few nodes with links and spam keywords" :
                               "Few nodes with notes and spam keywords";
                return SpamDetectionResult.spam(reason,
                        "TopicCount: " + topicCount, getType());
            }
        }
        
        return SpamDetectionResult.notSpam();
    }

    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.FEW_NODES;
    }
}