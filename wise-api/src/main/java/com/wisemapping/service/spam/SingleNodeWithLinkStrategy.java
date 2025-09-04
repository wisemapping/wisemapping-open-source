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
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Component
public class SingleNodeWithLinkStrategy implements SpamDetectionStrategy {
    
    private final SpamContentExtractor contentExtractor;

    public SingleNodeWithLinkStrategy(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }

    @Override
    public SpamDetectionResult detectSpam(Mindmap mindmap) {
        try {
            String xml = mindmap.getXmlStr();
            if (xml.trim().isEmpty()) {
                return SpamDetectionResult.notSpam();
            }
            
            // Count topic elements (nodes)
            int topicCount = (int) contentExtractor.countOccurrences(xml, "<topic");
            
            // Check if there's only one topic (central node) and it contains a link
            if (topicCount == 1) {
                boolean hasLink = xml.contains("<link") && xml.contains("url=");
                if (hasLink) {
                    String content = contentExtractor.extractTextContent(mindmap);
                    boolean hasCeoKeywords = contentExtractor.hasSpamKeywords(content);
                    if (hasCeoKeywords) {
                        return SpamDetectionResult.spam("Single node with link and spam keywords", 
                                                       "XML: " + xml + ", Content: " + content);
                    }
                }
            }
            
            // For maps with 2-3 nodes, check if central + minimal child nodes with links
            if (topicCount <= 3) {
                boolean hasLinks = xml.contains("<link") && xml.contains("url=");
                boolean hasCeoKeywords = contentExtractor.hasSpamKeywords(xml.toLowerCase());
                if (hasLinks && hasCeoKeywords) {
                    return SpamDetectionResult.spam("Few nodes with links and spam keywords", 
                                                   "XML: " + xml + ", TopicCount: " + topicCount);
                }
            }
            
            return SpamDetectionResult.notSpam();
        } catch (UnsupportedEncodingException e) {
            return SpamDetectionResult.notSpam();
        }
    }

    @Override
    public String getStrategyName() {
        return "SingleNodeWithLink";
    }
}