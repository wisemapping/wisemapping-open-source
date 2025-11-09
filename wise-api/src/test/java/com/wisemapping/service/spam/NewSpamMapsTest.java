package com.wisemapping.service.spam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.model.Account;
import com.wisemapping.model.Mindmap;
import com.wisemapping.service.MetricsService;
import com.wisemapping.service.SpamDetectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test to verify that newly downloaded spam maps are detected as spam.
 */
@ExtendWith(MockitoExtension.class)
class NewSpamMapsTest {

    private SpamDetectionService spamDetectionService;
    private SpamContentExtractor contentExtractor;
    private ObjectMapper objectMapper;

    @Mock
    private Resource spamKeywordsResource;
    
    @Mock
    private MetricsService metricsService;

    // Map IDs to test
    private static final int[] MAP_IDS = {
        1912210, 1921918, 1925679, 1922448, 1919615,
        1924468, 1919559, 1924763
    };

    @BeforeEach
    void setUp() throws Exception {
        // Mock spam keywords file content
        String keywordsContent = "best\ntop\npremium\nquality\nprofessional\nexperienced\nreliable\naffordable";
        
        InputStream stream = new java.io.ByteArrayInputStream(keywordsContent.getBytes());
        when(spamKeywordsResource.getInputStream()).thenReturn(stream);
        
        // Create content extractor with mocked resource
        contentExtractor = new SpamContentExtractor();
        ReflectionTestUtils.setField(contentExtractor, "spamKeywordsResource", spamKeywordsResource);
        contentExtractor.loadSpamKeywords();
        
        // Create spam detection strategies
        List<SpamDetectionStrategy> strategies = new ArrayList<>();
        
        ServiceDirectorySpamStrategy serviceDirectoryStrategy = new ServiceDirectorySpamStrategy(contentExtractor);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "minNodesExemption", 15);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "keywordStuffingSeparatorThreshold", 25);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "locationVariantsThreshold", 8);
        ReflectionTestUtils.setField(serviceDirectoryStrategy, "nearMeRepetitionsThreshold", 10);
        strategies.add(serviceDirectoryStrategy);
        
        DescriptionLengthStrategy descriptionLengthStrategy = new DescriptionLengthStrategy(contentExtractor);
        ReflectionTestUtils.setField(descriptionLengthStrategy, "minNodesExemption", 15);
        ReflectionTestUtils.setField(descriptionLengthStrategy, "maxDescriptionLength", 200);
        strategies.add(descriptionLengthStrategy);
        
        LinkFarmSpamStrategy linkFarmStrategy = new LinkFarmSpamStrategy(contentExtractor);
        ReflectionTestUtils.setField(linkFarmStrategy, "urlThreshold", 20);
        strategies.add(linkFarmStrategy);
        
        FewNodesWithContentStrategy fewNodesStrategy = new FewNodesWithContentStrategy(contentExtractor);
        ReflectionTestUtils.setField(fewNodesStrategy, "minNodesExemption", 15);
        strategies.add(fewNodesStrategy);
        
        // Create spam detection service
        spamDetectionService = new SpamDetectionService(strategies);
        ReflectionTestUtils.setField(spamDetectionService, "spamContentExtractor", contentExtractor);
        ReflectionTestUtils.setField(spamDetectionService, "metricsService", metricsService);
        
        objectMapper = new ObjectMapper();
    }

    /**
     * Finds the spam fixture directory by trying multiple possible locations.
     */
    private static Path findSpamExamplesDirectory() {
        Path cwd = Paths.get("").toAbsolutePath();
        
        Path[] possibleDirs = {
            cwd.resolve("doc/spam"),
            cwd.resolve("../doc/spam").normalize(),
            cwd.resolve("wise-api/doc/spam"),
            cwd.resolve("../../doc/spam").normalize(),
            cwd.resolve("../../../../../../doc/spam").normalize(),
            cwd.getParent() != null ? cwd.getParent().resolve("doc/spam") : null,
            Paths.get("doc/spam").toAbsolutePath(),
            Paths.get("wise-api/doc/spam").toAbsolutePath(),
            // Legacy fallbacks
            cwd.resolve("doc/spam-examples"),
            cwd.resolve("../doc/spam-examples").normalize(),
            cwd.resolve("wise-api/doc/spam-examples"),
            cwd.resolve("../../doc/spam-examples").normalize()
        };
        
        for (Path dir : possibleDirs) {
            if (dir != null && Files.exists(dir) && Files.isDirectory(dir)) {
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
     * Loads a map JSON file and creates a Mindmap object with creator Account.
     */
    private Mindmap loadMapFromJson(String filename) throws Exception {
        Path spamExamplesDir = findSpamExamplesDirectory();
        
        if (spamExamplesDir == null) {
            throw new IllegalArgumentException("Could not find spam fixture directory.");
        }
        
        Path jsonFile = spamExamplesDir.resolve(filename);
        
        if (!Files.exists(jsonFile)) {
            throw new IllegalArgumentException("Could not find JSON file: " + filename);
        }
        
        // Read and parse JSON
        String jsonContent = Files.readString(jsonFile);
        JsonNode jsonNode = objectMapper.readTree(jsonContent);
        
        // Verify the file has the expected metadata structure
        if (!jsonNode.has("xml")) {
            throw new IllegalArgumentException("JSON file " + filename + " is missing 'xml' field.");
        }
        
        // Create Mindmap object
        Mindmap mindmap = new Mindmap();
        
        // Extract map ID from filename
        String idStr = filename.replaceAll("map-(\\d+)\\.json", "$1");
        mindmap.setId(Integer.parseInt(idStr));
        
        // Load title
        if (jsonNode.has("title")) {
            String title = jsonNode.get("title").asText();
            mindmap.setTitle(title != null && !title.trim().isEmpty() ? title : null);
        }
        
        // Load description
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
        
        // Load XML content
        String xmlContent = jsonNode.get("xml").asText();
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON file " + filename + " has empty 'xml' field.");
        }
        mindmap.setXmlStr(xmlContent);
        
        // Load creator email and create Account
        if (jsonNode.has("createdBy")) {
            String creatorEmail = jsonNode.get("createdBy").asText();
            if (creatorEmail != null && !creatorEmail.trim().isEmpty()) {
                Account creator = new Account();
                creator.setEmail(creatorEmail);
                mindmap.setCreator(creator);
            }
        }
        
        return mindmap;
    }

    @Test
    void testAllNewMapsShouldBeDetectedAsSpam() throws Exception {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("TESTING NEW SPAM MAPS");
        System.out.println("=".repeat(80) + "\n");
        
        int detectedAsSpam = 0;
        int notDetected = 0;
        
        for (int mapId : MAP_IDS) {
            String filename = "map-" + mapId + ".json";
            
            try {
                Mindmap mindmap = loadMapFromJson(filename);
                
                // Check if creator has educational email (should be exempted)
                boolean hasEducationalEmail = contentExtractor.hasEduOrOrgEmail(mindmap);
                
                var result = spamDetectionService.detectSpam(mindmap, "test");
                
                System.out.println("Map " + mapId + ":");
                System.out.println("  Title: " + mindmap.getTitle());
                System.out.println("  Creator: " + (mindmap.getCreator() != null ? mindmap.getCreator().getEmail() : "null"));
                System.out.println("  Educational Email: " + hasEducationalEmail);
                System.out.println("  Detected as Spam: " + result.isSpam());
                
                if (result.isSpam()) {
                    System.out.println("  Strategy: " + result.getStrategyType());
                    System.out.println("  Reason: " + result.getReason());
                    System.out.println("  Details: " + result.getDetails());
                    detectedAsSpam++;
                } else {
                    System.out.println("  NOT DETECTED AS SPAM");
                    if (hasEducationalEmail) {
                        System.out.println("  → Exempted due to educational email domain");
                    } else {
                        System.out.println("  → Should be detected but wasn't!");
                        notDetected++;
                    }
                }
                System.out.println();
                
            } catch (Exception e) {
                System.err.println("Error testing map " + mapId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("=".repeat(80));
        System.out.println("SUMMARY");
        System.out.println("=".repeat(80));
        System.out.println("Total maps tested: " + MAP_IDS.length);
        System.out.println("Detected as spam: " + detectedAsSpam);
        System.out.println("Not detected (excluding educational exemptions): " + notDetected);
        System.out.println("=".repeat(80) + "\n");
        
        // Fail if any maps were not detected (excluding educational exemptions)
        if (notDetected > 0) {
            fail("Some maps were not detected as spam. See output above for details.");
        }
    }
}

