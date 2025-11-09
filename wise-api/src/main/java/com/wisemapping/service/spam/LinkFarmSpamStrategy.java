/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   You may obtain a copy of the license at
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Detects link farm spam - maps that contain excessive URLs for SEO purposes.
 * 
 * This strategy catches spam that uses mindmaps as link farms, typically containing
 * hundreds of URLs pointing to the same domain or various external sites.
 * Common patterns include gambling sites, adult content, or SEO link building campaigns.
 */
@Component
public class LinkFarmSpamStrategy implements SpamDetectionStrategy {

    private final SpamContentExtractor contentExtractor;
    
    @Value("${app.batch.spam-detection.link-farm.url-threshold:20}")
    private int urlThreshold;
    
    @Value("${app.batch.spam-detection.link-farm.url-threshold-low-structure:10}")
    private int urlThresholdLowStructure;
    
    @Value("${app.batch.spam-detection.link-farm.max-topics-for-low-structure:3}")
    private int maxTopicsForLowStructure;

    // Pattern to match URLs
    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/[^\\s<>\"']*)?",
        Pattern.CASE_INSENSITIVE
    );

    public LinkFarmSpamStrategy(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }

    @Override
    public SpamDetectionResult detectSpam(Mindmap mindmap) {
        if (mindmap == null) {
            return SpamDetectionResult.notSpam();
        }

        try {
            String xml = mindmap.getXmlStr();
            if (xml == null || xml.trim().isEmpty()) {
                return SpamDetectionResult.notSpam();
            }

            // Count nodes
            int topicCount = (int) contentExtractor.countOccurrences(xml, "<topic");
            
            // Extract content
            String content = contentExtractor.extractTextContent(mindmap);
            if (content == null || content.trim().isEmpty()) {
                return SpamDetectionResult.notSpam();
            }

            // Count URLs in content
            Matcher urlMatcher = URL_PATTERN.matcher(content);
            long urlCount = 0;
            while (urlMatcher.find()) {
                urlCount++;
            }

            // Rule 1: Extreme link stuffing - 20+ URLs regardless of structure
            if (urlCount >= urlThreshold) {
                return SpamDetectionResult.spam(
                    "Link farm spam detected - excessive URLs",
                    String.format("URLCount: %d, TopicCount: %d", urlCount, topicCount),
                    getType()
                );
            }

            // Rule 2: Moderate link stuffing in low-structure maps (link farm pattern)
            if (topicCount <= maxTopicsForLowStructure && urlCount >= urlThresholdLowStructure) {
                return SpamDetectionResult.spam(
                    "Link farm spam detected - excessive URLs in minimal structure",
                    String.format("URLCount: %d, TopicCount: %d (low structure)", urlCount, topicCount),
                    getType()
                );
            }

            // Rule 3: High URL density - URLs per topic ratio
            if (topicCount > 0 && urlCount > 0) {
                double urlDensity = (double) urlCount / topicCount;
                if (urlDensity >= 5.0 && urlCount >= 5) {
                    return SpamDetectionResult.spam(
                        "Link farm spam detected - high URL density",
                        String.format("URLCount: %d, TopicCount: %d, Density: %.2f URLs/topic", 
                                    urlCount, topicCount, urlDensity),
                        getType()
                    );
                }
            }

        } catch (Exception e) {
            // If we can't process the content, don't flag as spam
            return SpamDetectionResult.notSpam();
        }

        return SpamDetectionResult.notSpam();
    }

    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.LINK_FARM;
    }
}

