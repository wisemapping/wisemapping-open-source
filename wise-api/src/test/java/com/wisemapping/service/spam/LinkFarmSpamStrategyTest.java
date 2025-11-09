package com.wisemapping.service.spam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.parser.MindmapParser;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LinkFarmSpamStrategyTest {

    private LinkFarmSpamStrategy strategy;
    private SpamContentExtractor contentExtractor;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        contentExtractor = new SpamContentExtractor();
        ReflectionTestUtils.setField(contentExtractor, "maxNoteLength", 10000);
        ReflectionTestUtils.setField(contentExtractor, "spamKeywords", List.of("best", "top", "premium", "quality"));

        strategy = new LinkFarmSpamStrategy(contentExtractor);
        ReflectionTestUtils.setField(strategy, "urlThreshold", 20);
        ReflectionTestUtils.setField(strategy, "urlThresholdLowStructure", 10);
        ReflectionTestUtils.setField(strategy, "maxTopicsForLowStructure", 3);
        ReflectionTestUtils.setField(strategy, "popularDomainWhitelistRaw",
                "google.com,youtube.com,wikipedia.org,facebook.com,linkedin.com,github.com,stackoverflow.com,reddit.com,medium.com,apple.com,amazon.com,cnn.com,nytimes.com,bbc.com,docs.google.com,drive.google.com");
        ReflectionTestUtils.setField(strategy, "popularDomainWhitelist", null);

        objectMapper = new ObjectMapper();
    }

    @Test
    void map1704774WithPopularDomainsShouldBeClean() throws Exception {
        Mindmap mindmap = loadNonSpamMap("map-1704774.json");

        SpamDetectionContext context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);

        assertFalse(result.isSpam(), "Map 1704774 aggregates trusted sites and should not be flagged as link farm spam.");
    }

    @Test
    void syntheticLinkFarmShouldStillBeDetected() throws Exception {
        Mindmap mindmap = buildSyntheticLinkFarmMindmap(24);

        SpamDetectionContext context = createContext(mindmap);
        SpamDetectionResult result = strategy.detectSpam(context);

        assertTrue(result.isSpam(), "Synthetic link farm should be detected as spam.");
        assertEquals(SpamStrategyType.LINK_FARM, result.getStrategyType());
        assertNotNull(result.getReason());
    }

    private Mindmap loadNonSpamMap(String filename) throws Exception {
        Path fixturesDir = findNonSpamExamplesDirectory();
        if (fixturesDir == null) {
            throw new IllegalArgumentException("Could not find not-spam-examples directory.");
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
                cwd.resolve("doc/not-spam-examples"),
                cwd.resolve("../doc/not-spam-examples").normalize(),
                cwd.resolve("wise-api/doc/not-spam-examples"),
                cwd.resolve("../../doc/not-spam-examples").normalize(),
                cwd.resolve("../../../../../../doc/not-spam-examples").normalize(),
                cwd.getParent() != null ? cwd.getParent().resolve("doc/not-spam-examples") : null,
                Paths.get("doc/not-spam-examples").toAbsolutePath(),
                Paths.get("wise-api/doc/not-spam-examples").toAbsolutePath()
        };

        for (Path dir : possibleDirs) {
            if (dir != null && Files.exists(dir) && Files.isDirectory(dir)) {
                return dir;
            }
        }
        return null;
    }

    private SpamDetectionContext createContext(Mindmap mindmap) throws Exception {
        if (mindmap == null) {
            return null;
        }

        MapModel mapModel = MindmapParser.parseXml(mindmap.getXmlStr());
        if (mapModel.getTitle() == null && mindmap.getTitle() != null) {
            mapModel.setTitle(mindmap.getTitle());
        }
        if (mapModel.getDescription() == null && mindmap.getDescription() != null) {
            mapModel.setDescription(mindmap.getDescription());
        }
        return new SpamDetectionContext(mindmap, mapModel);
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
