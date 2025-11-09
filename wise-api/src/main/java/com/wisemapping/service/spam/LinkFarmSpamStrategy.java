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

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.model.SpamStrategyType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
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

    @Value("${app.batch.spam-detection.link-farm.popular-domain-whitelist-resource:classpath:spam/popular-domain-whitelist.yml}")
    private Resource popularDomainWhitelistResource;

    private Set<String> popularDomainWhitelistDomains;
    private Set<Pattern> popularDomainWhitelistPatterns;
    private boolean popularDomainWhitelistLoaded = false;

    // Pattern to match URLs
    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}(?:/[^\\s<>\"']*)?",
        Pattern.CASE_INSENSITIVE
    );

    public LinkFarmSpamStrategy(SpamContentExtractor contentExtractor) {
        this.contentExtractor = contentExtractor;
    }

    @Override
    public SpamDetectionResult detectSpam(SpamDetectionContext context) {
        if (context == null || context.getMindmap() == null || context.getMapModel() == null) {
            return SpamDetectionResult.notSpam();
        }

        try {
            MapModel mapModel = context.getMapModel();

            // Count nodes from the parsed model
            int topicCount = mapModel.getTotalTopicCount();

            // Extract content from the parsed model
            String content = contentExtractor.extractTextContent(mapModel,
                    context.getTitle(),
                    context.getDescription());
            if (content == null || content.trim().isEmpty()) {
                return SpamDetectionResult.notSpam();
            }

            // Count URLs in content and also check links in topics
            Matcher urlMatcher = URL_PATTERN.matcher(content);
            long urlCount = 0;
            while (urlMatcher.find()) {
                String url = urlMatcher.group();
                if (shouldCountUrl(url)) {
                    urlCount++;
                }
            }

            // Also count links from topic linkUrl fields
            for (String url : mapModel.getAllLinkUrls()) {
                if (shouldCountUrl(url)) {
                    urlCount++;
                }
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

    private boolean shouldCountUrl(String url) {
        if (url == null || url.isBlank()) {
            return false;
        }

        try {
            URI uri = new URI(url.trim());
            String host = uri.getHost();
            if (host == null) {
                return true;
            }

            host = host.toLowerCase(Locale.ROOT);
            if (host.startsWith("www.")) {
                host = host.substring(4);
            }

            if (isHostWhitelisted(host)) {
                return false;
            }

        } catch (URISyntaxException e) {
            return true;
        }

        return true;
    }

    private boolean isHostWhitelisted(String host) {
        ensurePopularDomainWhitelist();
        if (popularDomainWhitelistDomains.isEmpty() && popularDomainWhitelistPatterns.isEmpty()) {
            return false;
        }

        if (popularDomainWhitelistDomains.contains(host)) {
            return true;
        }

        for (String domain : popularDomainWhitelistDomains) {
            if (!domain.isEmpty() && host.endsWith("." + domain)) {
                return true;
            }
        }

        for (Pattern pattern : popularDomainWhitelistPatterns) {
            if (pattern.matcher(host).matches()) {
                return true;
            }
        }

        return false;
    }

    private void ensurePopularDomainWhitelist() {
        if (popularDomainWhitelistLoaded) {
            return;
        }

        popularDomainWhitelistLoaded = true;

        if (popularDomainWhitelistResource == null) {
            popularDomainWhitelistDomains = Collections.emptySet();
            popularDomainWhitelistPatterns = Collections.emptySet();
            return;
        }

        Set<String> domains = new HashSet<>();
        Set<Pattern> patterns = new HashSet<>();

        try (InputStream inputStream = popularDomainWhitelistResource.getInputStream()) {
            Yaml yaml = new Yaml();
            Object data = yaml.load(inputStream);
            extractDomains(data, domains, patterns);
        } catch (Exception e) {
            popularDomainWhitelistDomains = Collections.emptySet();
            popularDomainWhitelistPatterns = Collections.emptySet();
            return;
        }

        if (domains.isEmpty()) {
            popularDomainWhitelistDomains = Collections.emptySet();
        } else {
            popularDomainWhitelistDomains = Collections.unmodifiableSet(domains);
        }

        if (patterns.isEmpty()) {
            popularDomainWhitelistPatterns = Collections.emptySet();
        } else {
            popularDomainWhitelistPatterns = Collections.unmodifiableSet(patterns);
        }
    }

    private void extractDomains(Object data, Set<String> domains, Set<Pattern> patterns) {
        if (data == null) {
            return;
        }

        if (data instanceof Collection<?> collection) {
            for (Object element : collection) {
                addDomain(element, domains, patterns);
            }
        } else if (data instanceof Map<?, ?> map) {
            Object domainsValue = map.get("domains");
            if (domainsValue != null) {
                extractDomains(domainsValue, domains, patterns);
            }
        } else {
            addDomain(data, domains, patterns);
        }
    }

    private void addDomain(Object element, Set<String> domains, Set<Pattern> patterns) {
        if (element == null) {
            return;
        }

        String value = element.toString().trim().toLowerCase(Locale.ROOT);
        if (value.isEmpty()) {
            return;
        }

        if (containsWildcard(value)) {
            Pattern pattern = compileDomainPattern(value);
            if (pattern != null) {
                patterns.add(pattern);
            }
        } else {
            domains.add(value);
        }
    }

    private boolean containsWildcard(String value) {
        return value.indexOf('*') >= 0 || value.indexOf('?') >= 0;
    }

    private Pattern compileDomainPattern(String patternValue) {
        StringBuilder regexBuilder = new StringBuilder("^");
        for (char ch : patternValue.toCharArray()) {
            switch (ch) {
                case '*':
                    regexBuilder.append(".*");
                    break;
                case '?':
                    regexBuilder.append('.');
                    break;
                case '.':
                    regexBuilder.append("\\.");
                    break;
                default:
                    regexBuilder.append(Pattern.quote(Character.toString(ch)));
                    break;
            }
        }
        regexBuilder.append("$");
        try {
            return Pattern.compile(regexBuilder.toString(), Pattern.CASE_INSENSITIVE);
        } catch (Exception e) {
            return null;
        }
    }
}
