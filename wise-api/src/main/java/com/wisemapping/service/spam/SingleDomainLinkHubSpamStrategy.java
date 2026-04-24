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
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.model.SpamStrategyType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects maps whose structure is a single-domain link hub: a central topic
 * acting as a business/brand name, with first-level children that are all leaf
 * topics each pointing (via {@code <link url="..."/>}) to the same external
 * domain. This pattern is typical of SEO backlink maps created to boost a
 * target site and carries no legitimate mindmap content.
 */
@Component
public class SingleDomainLinkHubSpamStrategy implements SpamDetectionStrategy {

    @Value("${app.batch.spam-detection.single-domain-link-hub.min-first-level-links:3}")
    private int minFirstLevelLinks;

    @Value("${app.batch.spam-detection.single-domain-link-hub.same-domain-ratio:0.8}")
    private double sameDomainRatio;

    @Override
    public SpamDetectionResult detectSpam(SpamDetectionContext context) {
        if (context == null || context.getMapModel() == null) {
            return SpamDetectionResult.notSpam();
        }

        MapModel mapModel = context.getMapModel();
        Topic central = mapModel.getCentralTopic();
        if (central == null) {
            return SpamDetectionResult.notSpam();
        }

        List<Topic> firstLevel = central.getChildren();
        if (firstLevel.size() < minFirstLevelLinks) {
            return SpamDetectionResult.notSpam();
        }

        // First-level topics must be simple leaves (structure typical of this pattern).
        // If the author has built out real sub-branches, it's likely not this kind of
        // spam.
        long branchesWithChildren = firstLevel.stream()
                .filter(t -> !t.getChildren().isEmpty())
                .count();
        if (branchesWithChildren > 0) {
            return SpamDetectionResult.notSpam();
        }

        // Count first-level topics that carry an external link and group them by
        // their registrable domain.
        Map<String, Integer> domainCounts = new HashMap<>();
        int topicsWithLinks = 0;
        for (Topic t : firstLevel) {
            if (!t.hasLink()) {
                continue;
            }
            String domain = extractDomain(t.getLinkUrl());
            if (domain == null) {
                continue;
            }
            topicsWithLinks++;
            domainCounts.merge(domain, 1, (a, b) -> a + b);
        }

        if (topicsWithLinks < minFirstLevelLinks) {
            return SpamDetectionResult.notSpam();
        }

        Map.Entry<String, Integer> dominant = domainCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
        if (dominant == null) {
            return SpamDetectionResult.notSpam();
        }

        double ratio = (double) dominant.getValue() / firstLevel.size();
        if (ratio < sameDomainRatio) {
            return SpamDetectionResult.notSpam();
        }

        return SpamDetectionResult.spam(
                "Single-domain link hub detected - first-level topics all link to the same external domain",
                String.format(
                        "CentralText: '%s', FirstLevelCount: %d, FirstLevelWithLinks: %d, DominantDomain: %s, DominantCount: %d, Ratio: %.2f",
                        central.getText(), firstLevel.size(), topicsWithLinks,
                        dominant.getKey(), dominant.getValue(), ratio),
                getType());
    }

    private String extractDomain(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        try {
            String normalized = url.trim();
            if (!normalized.matches("^[a-zA-Z][a-zA-Z0-9+.-]*://.*")) {
                normalized = "http://" + normalized;
            }
            String host = URI.create(normalized).getHost();
            if (host == null) {
                return null;
            }
            host = host.toLowerCase();
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }
            return host;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    @Override
    public SpamStrategyType getType() {
        return SpamStrategyType.SINGLE_DOMAIN_LINK_HUB;
    }
}
