package com.wisemapping.service.spam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.model.Mindmap;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.SpamDetectionService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unified integration test for all Enabled Spam Detection Strategies.
 * <p>
 * This test loads real map examples from:
 * - src/test/resources/spam/ (Expected to be detected as SPAM)
 * - src/test/resources/non-spam/ (Expected to be detected as NOT SPAM)
 * <p>
 * It uses the Spring Test Context to wire the actual SpamDetectionService and
 * its strategies,
 * ensuring the test environment matches the production configuration as closely
 * as possible.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = FullSpamDetectionIntegrationTest.TestConfig.class)
@TestPropertySource(properties = {
        "app.mindmap.note.max-length=10000",
        "app.batch.spam-detection.min-nodes-exemption=15",
        "app.batch.spam-detection.link-farm.url-threshold=20",
        "app.batch.spam-detection.link-farm.url-threshold-low-structure=10",
        "app.batch.spam-detection.link-farm.max-topics-for-low-structure=3",
        "app.batch.spam-detection.service-directory.min-nodes-exemption=15",
        "app.batch.spam-detection.service-directory.keyword-stuffing-separators=25",
        "app.batch.spam-detection.service-directory.location-variants=8",
        "app.batch.spam-detection.service-directory.near-me-repetitions=10"
})
class FullSpamDetectionIntegrationTest {

    @Configuration
    @ComponentScan("com.wisemapping.service.spam")
    @Import(SpamDetectionService.class)
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private SpamDetectionService spamDetectionService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetricsService metricsService;

    /**
     * Helper to find directory paths for spam and non-spam resources.
     */
    private static Path findExamplesDirectory(String dirName) {
        try {
            java.net.URL url = FullSpamDetectionIntegrationTest.class.getClassLoader().getResource(dirName);
            if (url != null) {
                return Paths.get(url.toURI());
            }
        } catch (Exception e) {
            // Log or handle exception if needed, but returning null allows the caller to
            // handle the missing directory
        }
        return null;
    }

    /**
     * Loads a Mindmap object from a JSON file.
     */
    private Mindmap loadMapFromJson(Path jsonFile) throws Exception {
        String jsonContent = Files.readString(jsonFile);
        JsonNode jsonNode = objectMapper.readTree(jsonContent);

        if (!jsonNode.has("xml")) {
            throw new IllegalArgumentException("JSON file " + jsonFile + " is missing 'xml' field.");
        }

        Mindmap mindmap = new Mindmap();
        String filename = jsonFile.getFileName().toString();
        String idStr = filename.replaceAll("map-(\\d+)\\.json", "$1");
        try {
            mindmap.setId(Integer.parseInt(idStr));
        } catch (NumberFormatException e) {
            mindmap.setId(0);
        }

        if (jsonNode.has("title")) {
            String title = jsonNode.get("title").asText();
            mindmap.setTitle(title != null && !title.trim().isEmpty() ? title : null);
        }

        if (jsonNode.has("description")) {
            JsonNode descNode = jsonNode.get("description");
            if (!descNode.isNull()) {
                String description = descNode.asText();
                mindmap.setDescription(description != null && !description.trim().isEmpty() ? description : null);
            }
        }

        mindmap.setXmlStr(jsonNode.get("xml").asText());
        return mindmap;
    }

    // --- SPAM TESTS ---

    static Stream<String> provideSpamMapFiles() {
        Path spamDir = findExamplesDirectory("spam");
        if (spamDir == null)
            return Stream.empty();
        try {
            return Files.list(spamDir)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(n -> n.startsWith("map-") && n.endsWith(".json"))
                    .sorted();
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    @ParameterizedTest
    @MethodSource("provideSpamMapFiles")
    void shouldDetectSpamMaps(String filename) throws Exception {
        Path spamDir = findExamplesDirectory("spam");
        assertNotNull(spamDir, "Spam directory not found");
        Mindmap mindmap = loadMapFromJson(spamDir.resolve(filename));

        SpamDetectionResult result = spamDetectionService.detectSpam(mindmap, "integration-test-spam");

        assertTrue(result.isSpam(),
                "Expected map " + filename + " to be detected as SPAM, but it was CLEAN.\n" +
                        "Title: " + mindmap.getTitle() + "\n" +
                        "Description: " + mindmap.getDescription());
    }

    // --- NON-SPAM TESTS ---

    static Stream<String> provideNonSpamMapFiles() {
        Path nonSpamDir = findExamplesDirectory("non-spam");
        if (nonSpamDir == null)
            return Stream.empty();
        try {
            return Files.list(nonSpamDir)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(n -> n.startsWith("map-") && n.endsWith(".json"))
                    .sorted();
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    @ParameterizedTest
    @MethodSource("provideNonSpamMapFiles")
    void shouldPassCleanMaps(String filename) throws Exception {
        Path nonSpamDir = findExamplesDirectory("non-spam");
        assertNotNull(nonSpamDir, "Non-spam directory not found");
        Mindmap mindmap = loadMapFromJson(nonSpamDir.resolve(filename));

        SpamDetectionResult result = spamDetectionService.detectSpam(mindmap, "integration-test-clean");

        assertFalse(result.isSpam(),
                "Expected map " + filename + " to be CLEAN, but it was detected as SPAM.\n" +
                        "Reason: " + result.getReason() + "\n" +
                        "Details: " + result.getDetails() + "\n" +
                        "Strategy: " + result.getStrategyType());
    }
}
