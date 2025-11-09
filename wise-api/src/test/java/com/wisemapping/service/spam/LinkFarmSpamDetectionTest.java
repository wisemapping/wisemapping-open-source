package com.wisemapping.service.spam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.SpamDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LinkFarmSpamDetectionTest {

    private LinkFarmSpamStrategy linkFarmStrategy;
    private ServiceDirectorySpamStrategy serviceDirectoryStrategy;
    private SpamContentExtractor contentExtractor;
    private ObjectMapper objectMapper;
    private SpamDetectionService spamDetectionService;

    @BeforeEach
    void setUp() throws Exception {
        contentExtractor = new SpamContentExtractor();
        ReflectionTestUtils.setField(contentExtractor, "maxNoteLength", 10000);
        ReflectionTestUtils.setField(contentExtractor, "spamKeywords", List.of("best", "top", "premium", "quality"));

        linkFarmStrategy = new LinkFarmSpamStrategy(contentExtractor);
        ReflectionTestUtils.setField(linkFarmStrategy, "urlThreshold", 20);
        ReflectionTestUtils.setField(linkFarmStrategy, "urlThresholdLowStructure", 10);
        ReflectionTestUtils.setField(linkFarmStrategy, "maxTopicsForLowStructure", 3);
        String whitelistYaml = loadWhitelistFixture();
        ReflectionTestUtils.setField(
                linkFarmStrategy,
                "popularDomainWhitelistResource",
                new ByteArrayResource(whitelistYaml.getBytes(StandardCharsets.UTF_8))
        );
        ReflectionTestUtils.setField(linkFarmStrategy, "popularDomainWhitelistDomains", null);
        ReflectionTestUtils.setField(linkFarmStrategy, "popularDomainWhitelistPatterns", null);
        ReflectionTestUtils.setField(linkFarmStrategy, "popularDomainWhitelistLoaded", false);

        serviceDirectoryStrategy = new ServiceDirectorySpamStrategy(contentExtractor);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "minNodesExemption", 15);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "keywordStuffingSeparatorThreshold", 25);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "locationVariantsThreshold", 8);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "nearMeRepetitionsThreshold", 10);

        objectMapper = new ObjectMapper();

        FewNodesWithContentStrategy fewNodesStrategy = new FewNodesWithContentStrategy(contentExtractor);
        ReflectionTestUtils.setField(fewNodesStrategy, "minNodesExemption", 15);

        DescriptionLengthStrategy descriptionLengthStrategy = new DescriptionLengthStrategy(contentExtractor);
        ReflectionTestUtils.setField(descriptionLengthStrategy, "minNodesExemption", 15);
        ReflectionTestUtils.setField(descriptionLengthStrategy, "maxDescriptionLength", 200);

        spamDetectionService = new SpamDetectionService(
                List.of(
                        linkFarmStrategy,
                        serviceDirectoryStrategy,
                        fewNodesStrategy,
                        descriptionLengthStrategy
                )
        );
        MetricsService metricsService = Mockito.mock(MetricsService.class);
        ReflectionTestUtils.setField(spamDetectionService, "metricsService", metricsService);
        ReflectionTestUtils.setField(spamDetectionService, "spamContentExtractor", contentExtractor);
    }

    private String loadWhitelistFixture() throws Exception {
        Path whitelistPath = locateWhitelistResource();
        if (whitelistPath == null) {
            throw new IllegalStateException("Could not find popular-domain-whitelist.yml for testing.");
        }
        return Files.readString(whitelistPath, StandardCharsets.UTF_8);
    }

    private Path locateWhitelistResource() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path[] candidates = new Path[] {
                cwd.resolve("src/main/resources/spam/popular-domain-whitelist.yml"),
                cwd.resolve("../src/main/resources/spam/popular-domain-whitelist.yml").normalize(),
                cwd.resolve("wise-api/src/main/resources/spam/popular-domain-whitelist.yml"),
                cwd.getParent() != null ? cwd.getParent().resolve("src/main/resources/spam/popular-domain-whitelist.yml") : null,
                Paths.get("src/main/resources/spam/popular-domain-whitelist.yml").toAbsolutePath()
        };

        for (Path candidate : candidates) {
            if (candidate != null && Files.exists(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    @ParameterizedTest(name = "{1}")
    @MethodSource("nonSpamFixtures")
    void nonSpamMapsShouldBeClean(String fixtureFile, String expectation) throws Exception {
        Mindmap mindmap = loadNonSpamMap(fixtureFile);

        SpamDetectionResult result = spamDetectionService.detectSpam(mindmap, "non-spam-fixture");

        assertFalse(result.isSpam(), expectation);
    }

    @Test
    void wildcardDomainsShouldBeWhitelisted() throws Exception {
        StringBuilder xmlBuilder = new StringBuilder(
                "<map name=\"2000000\" version=\"tango\"><topic central=\"true\" id=\"1\"><text><![CDATA[Campus Resources]]></text>");

        int topicId = 2;
        for (int i = 0; i < 22; i++) {
            xmlBuilder.append("<topic id=\"").append(topicId++)
                    .append("\" text=\"Faculty ").append(i + 1).append("\">")
                    .append("<link url=\"https://dept").append(i).append(".university.edu/resource")
                    .append(i).append("\" urlType=\"url\"/></topic>");
        }
        xmlBuilder.append("<topic id=\"").append(topicId++)
                .append("\" text=\"Government Support\"><link url=\"https://services.gov.uk\" urlType=\"url\"/></topic>");
        xmlBuilder.append("</topic></map>");

        String xml = xmlBuilder.toString();
        Mindmap mindmap = new Mindmap();
        mindmap.setId(2000000);
        mindmap.setTitle("Campus Resources");
        mindmap.setXmlStr(xml);

        SpamDetectionResult result = spamDetectionService.detectSpam(mindmap, "wildcard-whitelist");

        assertFalse(result.isSpam(), "Wildcard whitelist entries should allow .edu and .gov domains.");
    }

    @Test
    void syntheticLinkFarmShouldStillBeDetected() throws Exception {
        Mindmap mindmap = buildSyntheticLinkFarmMindmap(24);

        SpamDetectionResult result = spamDetectionService.detectSpam(mindmap, "synthetic-link-farm");

        assertTrue(result.isSpam(), "Synthetic link farm should be detected as spam.");
        assertEquals(SpamStrategyType.LINK_FARM, result.getStrategyType());
        assertNotNull(result.getReason());
    }

    private Mindmap loadNonSpamMap(String filename) throws Exception {
        Path fixturesDir = findNonSpamExamplesDirectory();
        if (fixturesDir == null) {
            throw new IllegalArgumentException("Could not find non-spam fixture directory.");
        }

        Path jsonFile = fixturesDir.resolve(filename);
        if (!Files.exists(jsonFile)) {
            throw new IllegalArgumentException("Could not find JSON file: " + filename);
        }

        String jsonContent = Files.readString(jsonFile);
        JsonNode jsonNode = objectMapper.readTree(jsonContent);

        if (!jsonNode.has("xml")) {
            throw new IllegalArgumentException("JSON file " + filename + " is missing 'xml' field.");
        }

        Mindmap mindmap = new Mindmap();
        mindmap.setId(Integer.parseInt(filename.replaceAll("map-(\\d+)\\.json", "$1")));
        mindmap.setTitle(jsonNode.has("title") && !jsonNode.get("title").isNull() ? jsonNode.get("title").asText() : null);
        if (jsonNode.has("description") && !jsonNode.get("description").isNull()) {
            String description = jsonNode.get("description").asText();
            mindmap.setDescription(description != null && !description.trim().isEmpty() ? description : null);
        }
        mindmap.setXmlStr(jsonNode.get("xml").asText());
        return mindmap;
    }

    private Path findNonSpamExamplesDirectory() {
        Path cwd = Paths.get("").toAbsolutePath();
        Path[] possibleDirs = new Path[] {
                cwd.resolve("doc/non-spam"),
                cwd.resolve("../doc/non-spam").normalize(),
                cwd.resolve("wise-api/doc/non-spam"),
                cwd.resolve("../../doc/non-spam").normalize(),
                cwd.resolve("../../../../../../doc/non-spam").normalize(),
                cwd.getParent() != null ? cwd.getParent().resolve("doc/non-spam") : null,
                Paths.get("doc/non-spam").toAbsolutePath(),
                Paths.get("wise-api/doc/non-spam").toAbsolutePath(),
                // Legacy fallbacks for older layouts
                cwd.resolve("doc/not-spam-examples"),
                cwd.resolve("../doc/not-spam-examples").normalize(),
                cwd.resolve("wise-api/doc/not-spam-examples"),
                cwd.resolve("../../doc/not-spam-examples").normalize()
        };

        for (Path dir : possibleDirs) {
            if (dir != null && Files.exists(dir) && Files.isDirectory(dir)) {
                return dir;
            }
        }
        return null;
    }

    private static Stream<Arguments> nonSpamFixtures() {
        return Stream.of(
                Arguments.of(
                        "map-1704774.json",
                        "Map 1704774 aggregates trusted sites and should not be flagged as link farm spam."
                ),
                Arguments.of(
                        "map-1400591.json",
                        "Map 1400591 is a process-oriented mind map and must remain visible."
                )
        );
    }

    private Mindmap buildSyntheticLinkFarmMindmap(int urlCount) throws Exception {
        StringBuilder xml = new StringBuilder("<map name=\"999999\" version=\"tango\"><topic central=\"true\" id=\"1\"><text><![CDATA[Spammy Link Farm]]></text>");
        for (int i = 0; i < urlCount; i++) {
            int topicId = i + 2;
            xml.append("<topic id=\"").append(topicId).append("\" text=\"Spam Link ")
               .append(i + 1).append("\"><link url=\"https://spammy-example.com/page")
               .append(i + 1).append("\" urlType=\"url\"/></topic>");
        }
        xml.append("</topic></map>");

        Mindmap mindmap = new Mindmap();
        mindmap.setId(999999);
        mindmap.setTitle("Synthetic Link Farm");
        mindmap.setDescription("Automatically generated link farm with suspicious domains.");
        mindmap.setXmlStr(xml.toString());
        return mindmap;
    }
}
