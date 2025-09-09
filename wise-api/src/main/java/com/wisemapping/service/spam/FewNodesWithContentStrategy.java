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

package com.wisemapping.service.spam;

import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class FewNodesWithContentStrategy implements SpamDetectionStrategy {

    private final SpamContentExtractor contentExtractor;
    
    @Value("${app.batch.spam-detection.min-nodes-exemption:15}")
    private int minNodesExemption;

    public FewNodesWithContentStrategy(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }

    @Override
    public SpamDetectionResult detectSpam(Mindmap mindmap) {
        SpamDetectionResult result = SpamDetectionResult.notSpam();
        
        try {
            String xml = mindmap.getXmlStr();
            if (!xml.trim().isEmpty()) {
                // Count topic elements (nodes)
                int topicCount = (int) contentExtractor.countOccurrences(xml, "<topic");

                // Any mindmap with more than the configured threshold is considered legitimate content (not spam)
                if (topicCount > minNodesExemption) {
                    return SpamDetectionResult.notSpam();
                }

                // For maps with 2-3 nodes, check if central + minimal child nodes with links or notes
                if (topicCount <= 3) {
                    boolean hasLinks = xml.contains("<link") && xml.contains("url=");
                    boolean hasNotes = xml.contains("<note>");
                    if (hasLinks || hasNotes) {
                        String reason = hasLinks && hasNotes ? "Few nodes with links, notes and spam keywords" :
                                       hasLinks ? "Few nodes with links and spam keywords" :
                                       "Few nodes with notes and spam keywords";
                        result = SpamDetectionResult.spam(reason,
                                "XML: " + xml + ", TopicCount: " + topicCount);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            // Keep default notSpam result
        }
        
        return result;
    }

    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.FEW_NODES;
    }
}