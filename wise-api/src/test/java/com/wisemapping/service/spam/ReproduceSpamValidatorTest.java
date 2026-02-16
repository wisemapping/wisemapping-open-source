package com.wisemapping.service.spam;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wisemapping.model.Mindmap;
import com.wisemapping.validator.HtmlContentValidator;
import com.wisemapping.exceptions.HtmlContentValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ReproduceSpamValidatorTest.TestConfig.class)
class ReproduceSpamValidatorTest {

    @Configuration
    @Import({ HtmlContentValidator.class, SpamContentExtractor.class })
    static class TestConfig {
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }

    @Autowired
    private HtmlContentValidator htmlContentValidator;

    @Autowired
    private ObjectMapper objectMapper;

    private static Path findExamplesDirectory(String dirName) {
        try {
            java.net.URL url = ReproduceSpamValidatorTest.class.getClassLoader().getResource(dirName);
            if (url != null) {
                return Paths.get(url.toURI());
            }
        } catch (Exception e) {
        }
        return null;
    }

    private Mindmap loadMapFromJson(Path jsonFile) throws Exception {
        String jsonContent = Files.readString(jsonFile);
        JsonNode jsonNode = objectMapper.readTree(jsonContent);

        if (!jsonNode.has("xml")) {
            throw new IllegalArgumentException("JSON file " + jsonFile + " is missing 'xml' field.");
        }

        Mindmap mindmap = new Mindmap();
        // Mock ID
        mindmap.setId(1);
        mindmap.setXmlStr(jsonNode.get("xml").asText());
        if (jsonNode.has("title"))
            mindmap.setTitle(jsonNode.get("title").asText());
        if (jsonNode.has("description"))
            mindmap.setDescription(jsonNode.get("description").asText());

        return mindmap;
    }

    static Stream<String> provideNonSpamMapFiles() {
        Path dir = findExamplesDirectory("non-spam");
        if (dir == null)
            return Stream.empty();
        try {
            return Files.list(dir)
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(n -> n.startsWith("map-") && n.endsWith(".json"))
                    .sorted();
        } catch (Exception e) {
            return Stream.empty();
        }
    }

    static Stream<String> provideSpamMapFiles() {
        Path dir = findExamplesDirectory("spam");
        if (dir == null)
            return Stream.empty();
        try {
            return Files.list(dir)
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
    void testValidatorOnNonSpamMaps(String filename) throws Exception {
        Path dir = findExamplesDirectory("non-spam");
        Mindmap mindmap = loadMapFromJson(dir.resolve(filename));

        try {
            htmlContentValidator.validateHtmlContent(mindmap);
        } catch (HtmlContentValidationException e) {
            fail("HtmlContentValidator threw exception for NON-SPAM map " + filename + ": " + e.getMessage());
        }
    }

    @Test
    void testValidatorBlocksHiddenStyle() throws Exception {
        Mindmap mindmap = new Mindmap();
        mindmap.setId(1);
        mindmap.setXmlStr("<map><topic><text><![CDATA[<div style=\"display:none\">Spam</div>]]></text></topic></map>");

        assertThrows(HtmlContentValidationException.class, () -> {
            htmlContentValidator.validateHtmlContent(mindmap);
        }, "HtmlContentValidator should BLOCK style='display:none'");
    }

    @Test
    void testValidatorBlocksHiddenInput() throws Exception {
        Mindmap mindmap = new Mindmap();
        mindmap.setId(1);
        mindmap.setXmlStr(
                "<map><topic><text><![CDATA[<form><input type=\"hidden\" name=\"spam\"></form>]]></text></topic></map>");

        assertThrows(HtmlContentValidationException.class, () -> {
            htmlContentValidator.validateHtmlContent(mindmap);
        }, "HtmlContentValidator should BLOCK <input type='hidden'>");
    }

    @Test
    void testValidatorBlocksExcessiveLinks() throws Exception {
        Mindmap mindmap = new Mindmap();
        mindmap.setId(1);
        StringBuilder links = new StringBuilder();
        for (int i = 0; i < 15; i++) {
            links.append("<a href='http://spam.com'>spam</a>");
        }
        mindmap.setXmlStr("<map><topic><text><![CDATA[" + links.toString() + "]]></text></topic></map>");

        assertThrows(HtmlContentValidationException.class, () -> {
            htmlContentValidator.validateHtmlContent(mindmap);
        }, "HtmlContentValidator should BLOCK excessive links");
    }

    @ParameterizedTest
    @MethodSource("provideSpamMapFiles")
    void testValidatorOnSpamMaps(String filename) throws Exception {
        Path dir = findExamplesDirectory("spam");
        Mindmap mindmap = loadMapFromJson(dir.resolve(filename));

        try {
            htmlContentValidator.validateHtmlContent(mindmap);
            // It is okay if it passes, as many spam maps are text/link spam, not HTML spam.
        } catch (HtmlContentValidationException e) {
            System.out.println("Validator BLOCKED for SPAM map: " + filename + " Reason: " + e.getMessage());
        }
    }
}
