package com.wisemapping.service.spam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.parser.MindmapParser;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.SpamStrategyType;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.SpamDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ServiceDirectorySpamStrategy using real spam examples from doc/spam/
 * 
 * This test loads JSON files containing real spam map metadata and verifies they are detected.
 */
@ExtendWith(MockitoExtension.class)
class ServiceDirectorySpamDetectionTest {

    private ServiceDirectorySpamStrategy serviceDirectoryStrategy;
    private LinkFarmSpamStrategy linkFarmStrategy;
    private SpamContentExtractor contentExtractor;
    private ObjectMapper objectMapper;
    private SpamDetectionService spamDetectionService;

    @Mock
    private Resource spamKeywordsResource;

    @BeforeEach
    void setUp() throws Exception {
        // Mock spam keywords file content (minimal for this test)
        String keywordsContent = "best\ntop\npremium\nquality\nprofessional\nexperienced\nreliable\naffordable";
        
        InputStream stream = new java.io.ByteArrayInputStream(keywordsContent.getBytes());
        when(spamKeywordsResource.getInputStream()).thenReturn(stream);
        
        // Create content extractor with mocked resource
        contentExtractor = new SpamContentExtractor();
        ReflectionTestUtils.setField(contentExtractor, "spamKeywordsResource", spamKeywordsResource);
        contentExtractor.loadSpamKeywords();
        
        // Create strategy with test configuration
        serviceDirectoryStrategy = new ServiceDirectorySpamStrategy(contentExtractor);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "minNodesExemption", 15);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "keywordStuffingSeparatorThreshold", 25);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "locationVariantsThreshold", 8);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "nearMeRepetitionsThreshold", 10);

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
        
        objectMapper = new ObjectMapper();

        FewNodesWithContentStrategy fewNodesStrategy = new FewNodesWithContentStrategy(contentExtractor);
        ReflectionTestUtils.setField(fewNodesStrategy, "minNodesExemption", 15);

        DescriptionLengthStrategy descriptionLengthStrategy = new DescriptionLengthStrategy(contentExtractor);
        ReflectionTestUtils.setField(descriptionLengthStrategy, "minNodesExemption", 15);
        ReflectionTestUtils.setField(descriptionLengthStrategy, "maxDescriptionLength", 200);

        spamDetectionService = new SpamDetectionService(
                List.of(
                        serviceDirectoryStrategy,
                        linkFarmStrategy,
                        fewNodesStrategy,
                        descriptionLengthStrategy
                )
        );
        MetricsService metricsService = mock(MetricsService.class);
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

    /**
     * Finds the spam fixture directory by trying multiple possible locations.
     * This handles different working directories when running tests from IDE vs Maven.
     */
    private static Path findSpamExamplesDirectory() {
        Path cwd = Paths.get("").toAbsolutePath();
        
        // Try multiple possible locations
        Path[] possibleDirs = {
            // When running from project root
            cwd.resolve("doc/spam"),
            // When running from wise-api subdirectory (Maven test execution)
            cwd.resolve("../doc/spam").normalize(),
            // When running from wise-api subdirectory (alternative)
            cwd.resolve("wise-api/doc/spam"),
            // When running from wise-api/target/test-classes
            cwd.resolve("../../doc/spam").normalize(),
            // When running from wise-api/target/test-classes/com/wisemapping/service/spam
            cwd.resolve("../../../../../../doc/spam").normalize(),
            // When running from wise-api directory
            cwd.getParent() != null ? cwd.getParent().resolve("doc/spam") : null,
            // Absolute paths
            Paths.get("doc/spam").toAbsolutePath(),
            Paths.get("wise-api/doc/spam").toAbsolutePath(),
            // Legacy fallbacks for earlier layouts
            cwd.resolve("doc/spam-examples"),
            cwd.resolve("../doc/spam-examples").normalize(),
            cwd.resolve("wise-api/doc/spam-examples"),
            cwd.resolve("../../doc/spam-examples").normalize()
        };
        
        for (Path dir : possibleDirs) {
            if (dir != null && Files.exists(dir) && Files.isDirectory(dir)) {
                // Verify it contains at least one map-*.json file
                try {
                    boolean hasMapFiles = Files.list(dir)
                        .anyMatch(path -> {
                            String name = path.getFileName().toString();
                            return name.startsWith("map-") && name.endsWith(".json");
                        });
                    if (hasMapFiles) {
                        return dir;
                    }
                } catch (Exception e) {
                    // Continue to next path
                }
            }
        }
        
        return null;
    }

    /**
     * Loads a map JSON file from doc/spam/ and creates a Mindmap object
     */
    private Mindmap loadMapFromJson(String filename) throws Exception {
        Path spamExamplesDir = findSpamExamplesDirectory();
        
        if (spamExamplesDir == null) {
            throw new IllegalArgumentException("Could not find spam fixture directory. " +
                "Please ensure the doc/spam/ directory exists in the project root.");
        }
        
        Path jsonFile = spamExamplesDir.resolve(filename);
        
        if (!Files.exists(jsonFile)) {
            throw new IllegalArgumentException("Could not find JSON file: " + filename + 
                " in directory: " + spamExamplesDir);
        }
        
        // Read and parse JSON
        String jsonContent = Files.readString(jsonFile);
        JsonNode jsonNode = objectMapper.readTree(jsonContent);
        
        // Verify the file has the expected metadata structure
        if (!jsonNode.has("xml")) {
            throw new IllegalArgumentException("JSON file " + filename + " is missing 'xml' field. " +
                "Please re-download the metadata using: /api/restful/maps/{id}/metadata?xml=true");
        }
        
        // Create Mindmap object
        Mindmap mindmap = new Mindmap();
        
        // Extract map ID from filename
        String idStr = filename.replaceAll("map-(\\d+)\\.json", "$1");
        mindmap.setId(Integer.parseInt(idStr));
        
        // Load title (required for spam detection)
        if (jsonNode.has("title")) {
            String title = jsonNode.get("title").asText();
            mindmap.setTitle(title != null && !title.trim().isEmpty() ? title : null);
        }
        
        // Load description (can be null or empty - spam detectors handle this)
        if (jsonNode.has("description")) {
            JsonNode descNode = jsonNode.get("description");
            if (descNode.isNull()) {
                mindmap.setDescription(null);
            } else {
                String description = descNode.asText();
                mindmap.setDescription(description != null && !description.trim().isEmpty() ? description : null);
            }
        } else {
            mindmap.setDescription(null);
        }
        
        // Load XML content (required for spam detection)
        String xmlContent = jsonNode.get("xml").asText();
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON file " + filename + " has empty 'xml' field. " +
                "Please re-download the metadata using: /api/restful/maps/{id}/metadata?xml=true");
        }
        mindmap.setXmlStr(xmlContent);
        
        // Verify we have at least title or XML content for spam detection
        if ((mindmap.getTitle() == null || mindmap.getTitle().trim().isEmpty()) && 
            (xmlContent == null || xmlContent.trim().isEmpty())) {
            throw new IllegalArgumentException("JSON file " + filename + " is missing both 'title' and 'xml' fields. " +
                "Please re-download the metadata using: /api/restful/maps/{id}/metadata?xml=true");
        }
        
        return mindmap;
    }

    /**
     * Creates a SpamDetectionContext from a Mindmap by parsing its XML.
     */
    private SpamDetectionContext createContext(Mindmap mindmap) throws Exception {
        if (mindmap == null) {
            return null;
        }
        
        String xmlContent = mindmap.getXmlStr();
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("Mindmap has no XML content");
        }
        
        MapModel mapModel = MindmapParser.parseXml(xmlContent);
        
        // Set title and description from entity if not in model
        if (mapModel.getTitle() == null && mindmap.getTitle() != null) {
            mapModel.setTitle(mindmap.getTitle());
        }
        if (mapModel.getDescription() == null && mindmap.getDescription() != null) {
            mapModel.setDescription(mindmap.getDescription());
        }
        
        return new SpamDetectionContext(mindmap, mapModel);
    }

    @Test
    void testMap1902545_UpDownDeskAustralia_ShouldBeDetected() throws Exception {
        // Map 1902545: Has address, phone (+61 1300 650 773), website, email, 2 topics
        // Should be detected by Rule 1 (complete contact info) or Rule 5 (address + website + locations)
        Mindmap mindmap = loadMapFromJson("map-1902545.json");
        
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        assertTrue(mindmap.getXmlStr().contains("UpDown Desk"), "Should contain business name");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1902545 (UpDown Desk Australia) should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1901495_AdventureYogi_ShouldBeDetected() throws Exception {
        // Map 1901495: Has address, phone (01273 782 734), website, 2 topics
        // Should be detected by Rule 1 (complete contact info) or Rule 6 (address + website + low node count)
        Mindmap mindmap = loadMapFromJson("map-1901495.json");
        
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        assertTrue(mindmap.getXmlStr().contains("Adventure Yogi"), "Should contain business name");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1901495 (Adventure Yogi) should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1924052_DoughGirl_ShouldBeDetected() throws Exception {
        // Map 1924052: Has address, phone (01754 768120), website, 3 topics
        // Should be detected by Rule 1 (complete contact info) or Rule 6 (address + website + low node count)
        Mindmap mindmap = loadMapFromJson("map-1924052.json");
        
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        assertTrue(mindmap.getXmlStr().contains("DoughGirl"), "Should contain business name");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1924052 (DoughGirl) should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1841477_WelcomeBen_ShouldBeDetected() throws Exception {
        // Map 1841477: Has complete contact info (address, phone, website, email), 2 topics
        // Should be detected by Rule 1 (complete contact info)
        Mindmap mindmap = loadMapFromJson("map-1841477.json");
        
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1841477 (Welcome Ben) should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1835747_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1835747.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1835747 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1848035_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1848035.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1848035 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1940825_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1940825.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1940825 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1912233_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1912233.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1912233 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1922153_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1922153.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1922153 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1884075_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1884075.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1884075 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1943823_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1943823.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1943823 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1923339_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1923339.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1923339 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1935323_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1935323.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1935323 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1943830_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1943830.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1943830 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1909519_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1909519.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1909519 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1911897_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1911897.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1911897 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testMap1858501_ShouldBeDetected() throws Exception {
        Mindmap mindmap = loadMapFromJson("map-1858501.json");
        assertNotNull(mindmap.getXmlStr(), "XML should not be null");
        
        var context = createContext(mindmap);
        var result = serviceDirectoryStrategy.detectSpam(context);
        
        assertTrue(result.isSpam(), 
            "Map 1858501 should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType());
    }

    @Test
    void testNullMindmap() {
        assertFalse(serviceDirectoryStrategy.detectSpam((SpamDetectionContext) null).isSpam());
    }

    @Test
    void testEmptyXml() throws Exception {
        Mindmap mindmap = new Mindmap();
        // Use minimal valid XML instead of empty string (empty XML is invalid)
        mindmap.setXmlStr(Mindmap.getDefaultMindmapXml("Test"));
        
        var context = createContext(mindmap);
        assertFalse(serviceDirectoryStrategy.detectSpam(context).isSpam());
    }

    /**
     * Provides a stream of all map JSON filenames from the spam fixture directory.
     * This method is used by the parametrized test to load all downloaded maps.
     */
    static Stream<String> provideAllMapFiles() {
        Path spamExamplesDir = findSpamExamplesDirectory();
        
        if (spamExamplesDir == null) {
            // Return a stream with a placeholder to avoid "no arguments" error
            // The test will fail with a clear message
            return Stream.of("__DIRECTORY_NOT_FOUND__");
        }
        
        try {
            Stream<String> files = Files.list(spamExamplesDir)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(filename -> filename.startsWith("map-") && filename.endsWith(".json"))
                    .sorted();
            
            // Check if we found any files
            long count = files.count();
            if (count == 0) {
                return Stream.of("__NO_FILES_FOUND__");
            }
            
            // Re-list since we consumed the stream
            return Files.list(spamExamplesDir)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(filename -> filename.startsWith("map-") && filename.endsWith(".json"))
                    .sorted();
        } catch (Exception e) {
            // Return placeholder to avoid "no arguments" error
            return Stream.of("__ERROR_LISTING_FILES__");
        }
    }

    /**
     * Parametrized test that loads ALL maps from the spam fixture directory
     * and verifies they are detected as spam.
     * 
     * This test ensures that all downloaded spam maps are properly detected
     * by the ServiceDirectorySpamStrategy.
     */
    @ParameterizedTest
    @MethodSource("provideAllMapFiles")
    void testAllDownloadedMapsShouldBeDetectedAsSpam(String filename) throws Exception {
        // Handle special placeholder values that indicate directory/file issues
        if (filename.equals("__DIRECTORY_NOT_FOUND__")) {
            fail("Could not find spam fixture directory. Please ensure doc/spam/ exists in the project root.");
        }
        if (filename.equals("__NO_FILES_FOUND__")) {
            fail("Found spam fixture directory but no map-*.json files. Please ensure map files are present.");
        }
        if (filename.equals("__ERROR_LISTING_FILES__")) {
            fail("Error listing files in spam directory. Check file permissions.");
        }
        
        Mindmap mindmap = loadMapFromJson(filename);
        
        assertNotNull(mindmap.getXmlStr(), "XML should not be null for " + filename);
        
        var result = spamDetectionService.detectSpam(mindmap, "service-directory-fixture");
        
        assertTrue(result.isSpam(), 
            "Map " + filename + " should be detected as spam. " +
            "Reason: " + result.getReason() + ", Details: " + result.getDetails());
        assertEquals(SpamStrategyType.SERVICE_DIRECTORY, result.getStrategyType(),
            "Map " + filename + " should be detected by SERVICE_DIRECTORY strategy");
    }
}

