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

package com.wisemapping.mindmap.parser;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.mindmap.model.MapMetadata;
import com.wisemapping.mindmap.utils.MindmapValidationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.SAXException;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.stream.Collectors;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for mindmap XML content that extracts structured data from XML.
 * This parser can be used independently of the main application.
 */
public class MindmapParser {
    
    private static final Logger logger = LogManager.getLogger();
    private static final int MAX_SUPPORTED_NODES = 4000;
    
    private static final DocumentBuilder documentBuilder;
    
    static {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setValidating(false);
            factory.setFeature("http://xml.org/sax/features/namespaces", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException("Failed to initialize XML parser", e);
        }
    }
    
    /**
     * Parses XML content into a MapModel.
     * 
     * @param xmlContent The XML content to parse
     * @return Parsed mindmap model
     * @throws MindmapValidationException if parsing fails
     */
    @NotNull
    public static MapModel parseXml(@NotNull String xmlContent) throws MindmapValidationException {
        validateXmlContent(xmlContent);
        
        try {
            Document document = parseXmlDocument(xmlContent);
            return extractMapModel(document);
        } catch (Exception e) {
            throw new MindmapValidationException("Failed to parse mindmap XML", e);
        }
    }
    
    /**
     * Extracts text content from mindmap XML for analysis purposes.
     * 
     * @param xmlContent The XML content to analyze
     * @return Extracted text content
     */
    @NotNull
    public static String extractTextContent(@NotNull String xmlContent) {
        try {
            MapModel mapModel = parseXml(xmlContent);
            StringBuilder text = new StringBuilder();
            
            if (mapModel.getTitle() != null) {
                text.append(mapModel.getTitle()).append(" ");
            }
            
            for (Topic topic : mapModel.getAllTopics()) {
                if (topic.getText() != null) {
                    text.append(topic.getText()).append(" ");
                }
                if (topic.getNote() != null) {
                    text.append(sanitizeHtmlContent(topic.getNote())).append(" ");
                }
                if (topic.getLinkUrl() != null) {
                    text.append(topic.getLinkUrl()).append(" ");
                }
            }
            
            return text.toString().trim();
        } catch (Exception e) {
            logger.warn("XML parsing failed, falling back to regex-based extraction: {}", e.getMessage());
            return extractTextContentFallback(xmlContent);
        }
    }
    
    /**
     * Checks if the mindmap contains HTML content in notes.
     * 
     * @param xmlContent The XML content to check
     * @return true if HTML content is found, false otherwise
     */
    public static boolean hasHtmlContent(@NotNull String xmlContent) {
        try {
            MapModel mapModel = parseXml(xmlContent);
            for (Topic topic : mapModel.getAllTopics()) {
                if (topic.getNote() != null && isHtmlContent(topic.getNote())) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            logger.warn("Error checking for HTML content: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Validates the basic structure of mindmap XML.
     * 
     * @param xmlContent The XML content to validate
     * @throws MindmapValidationException if validation fails
     */
    public static void validateXmlContent(@NotNull String xmlContent) throws MindmapValidationException {
        if (xmlContent == null || xmlContent.trim().isEmpty()) {
            throw new MindmapValidationException("Mindmap XML content is empty");
        }
        
        // Perform basic structure validation
        if (!xmlContent.trim().endsWith("</map>") || !xmlContent.trim().startsWith("<map")) {
            throw new MindmapValidationException("Invalid mindmap XML format");
        }
        
        int numberOfTopics = xmlContent.split("<topic").length;
        if (numberOfTopics == 0) {
            throw new MindmapValidationException("Mindmap must contain at least one topic");
        }
        
        if (numberOfTopics > MAX_SUPPORTED_NODES) {
            throw new MindmapValidationException("Mindmap contains too many nodes: " + numberOfTopics + " (max: " + MAX_SUPPORTED_NODES + ")");
        }
    }
    
    /**
     * Parses XML string into a DOM Document.
     */
    @Nullable
    private static Document parseXmlDocument(@NotNull String xmlContent) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(StandardCharsets.UTF_8));
            return documentBuilder.parse(inputStream);
        } catch (SAXException | IOException e) {
            logger.debug("Failed to parse XML: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * Extracts mindmap model from parsed XML document.
     */
    @NotNull
    private static MapModel extractMapModel(@NotNull Document document) {
        Element rootElement = document.getDocumentElement();
        MapModel mapModel = new MapModel();
        
        // Extract map attributes
        mapModel.setTitle(rootElement.getAttribute("name"));
        
        // Extract metadata
        MapMetadata metadata = new MapMetadata();
        metadata.setVersion(rootElement.getAttribute("version"));
        metadata.setTheme(rootElement.getAttribute("theme"));
        mapModel.setMetadata(metadata);
        
        // Extract topics
        List<Topic> topics = extractTopics(rootElement);
        mapModel.setTopics(topics);
        
        return mapModel;
    }
    
    /**
     * Extracts topic nodes from XML element.
     */
    @NotNull
    private static List<Topic> extractTopics(@NotNull Element element) {
        List<Topic> topics = new ArrayList<>();
        NodeList children = element.getChildNodes();
        
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE && "topic".equals(child.getNodeName())) {
                Element topicElement = (Element) child;
                Topic topic = extractTopicNode(topicElement);
                topics.add(topic);
            }
        }
        
        return topics;
    }
    
    /**
     * Extracts a single topic node from XML element.
     */
    @NotNull
    private static Topic extractTopicNode(@NotNull Element topicElement) {
        Topic topic = new Topic();
        
        // Extract attributes
        topic.setText(topicElement.getAttribute("text"));
        topic.setId(topicElement.getAttribute("id"));
        topic.setCentral("true".equals(topicElement.getAttribute("central")));
        
        // Extract child elements
        NodeList children = topicElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String tagName = childElement.getTagName();
                
                switch (tagName) {
                    case "text":
                        topic.setText(childElement.getTextContent());
                        break;
                    case "note":
                        topic.setNote(extractNoteContent(childElement));
                        break;
                    case "link":
                        topic.setLinkUrl(childElement.getAttribute("url"));
                        break;
                    case "topic":
                        // Recursively extract child topics
                        Topic childTopic = extractTopicNode(childElement);
                        topic.addChild(childTopic);
                        break;
                }
            }
        }
        
        return topic;
    }
    
    /**
     * Extracts content from note elements, handling both CDATA and direct content.
     */
    @NotNull
    private static String extractNoteContent(@NotNull Element noteElement) {
        StringBuilder noteContent = new StringBuilder();
        
        NodeList children = noteElement.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.CDATA_SECTION_NODE) {
                noteContent.append(child.getTextContent());
            } else if (child.getNodeType() == Node.TEXT_NODE) {
                String text = child.getTextContent();
                if (text != null && !text.trim().isEmpty()) {
                    noteContent.append(text);
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                noteContent.append(child.getTextContent());
            }
        }
        
        return noteContent.toString();
    }
    
    /**
     * Recursively extracts text content from topic elements.
     */
    private static void extractTextFromTopics(@NotNull Element element, @NotNull StringBuilder text) {
        String tagName = element.getTagName();
        
        // Extract text attribute
        String textAttr = element.getAttribute("text");
        if (!textAttr.isEmpty()) {
            text.append(textAttr).append(" ");
        }
        
        // Extract URL attribute from link elements
        if ("link".equals(tagName)) {
            String urlAttr = element.getAttribute("url");
            if (!urlAttr.isEmpty()) {
                text.append(urlAttr).append(" ");
            }
        }
        
        // Process child elements
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String childTagName = childElement.getTagName();
                
                if ("text".equals(childTagName)) {
                    String textContent = childElement.getTextContent();
                    if (textContent != null && !textContent.trim().isEmpty()) {
                        text.append(textContent.trim()).append(" ");
                    }
                } else if ("note".equals(childTagName)) {
                    String noteContent = extractNoteContent(childElement);
                    if (!noteContent.isEmpty()) {
                        text.append(noteContent).append(" ");
                    }
                } else if ("link".equals(childTagName)) {
                    String urlAttr = childElement.getAttribute("url");
                    if (!urlAttr.isEmpty()) {
                        text.append(urlAttr).append(" ");
                    }
                } else if ("topic".equals(childTagName)) {
                    extractTextFromTopics(childElement, text);
                }
            }
        }
    }
    
    /**
     * Checks for HTML content in a parsed XML document.
     */
    private static boolean hasHtmlContentInDocument(@NotNull Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) child;
                String tagName = childElement.getTagName();
                
                if ("note".equals(tagName)) {
                    String noteContent = extractNoteContent(childElement);
                    if (isHtmlContent(noteContent)) {
                        return true;
                    }
                } else if ("topic".equals(tagName)) {
                    if (hasHtmlContentInDocument(childElement)) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }
    
    /**
     * Determines if content contains HTML markup.
     */
    private static boolean isHtmlContent(@NotNull String content) {
        if (content.trim().isEmpty()) {
            return false;
        }
        
        return content.contains("<") && content.contains(">") &&
               (content.contains("<p>") || content.contains("<div>") || 
                content.contains("<span>") || content.contains("<a ") ||
                content.contains("<script>") || content.contains("<iframe>") ||
                content.contains("<img") || content.contains("<br") ||
                content.contains("<strong>") || content.contains("<em>") ||
                content.matches(".*<[a-zA-Z][a-zA-Z0-9]*[^>]*>.*"));
    }

    /**
     * Sanitizes HTML content by removing dangerous elements and attributes while preserving text content.
     *
     * @param content The content to sanitize (may be plain text or HTML)
     * @return Sanitized text content safe for spam detection
     */
    @NotNull
    public static String sanitizeHtmlContent(@NotNull String content) {
        if (content.trim().isEmpty()) {
            return "";
        }

        // Check if content looks like HTML (contains HTML tags)
        if (isHtmlContent(content)) {
            // Parse and sanitize HTML content
            org.jsoup.nodes.Document doc = Jsoup.parse(content);

            // Use a very restrictive safelist that only allows basic text formatting
            Safelist safelist = Safelist.none()
                .addTags("p", "br", "div", "span", "strong", "b", "em", "i", "u")
                .addAttributes("p", "class", "style")
                .addAttributes("div", "class", "style")
                .addAttributes("span", "class", "style");

            // Clean the HTML content
            String cleanedHtml = Jsoup.clean(doc.body().html(), safelist);

            // Extract plain text from the cleaned HTML
            org.jsoup.nodes.Document cleanedDoc = Jsoup.parse(cleanedHtml);
            String plainText = cleanedDoc.text();

            // Also extract URLs that might be in the content (for spam detection)
            String urls = doc.select("a[href]").stream()
                .map(element -> element.attr("href"))
                .filter(url -> !url.isEmpty())
                .collect(Collectors.joining(" "));

            return (plainText + " " + urls).trim();
        } else {
            // Content is plain text, return as-is but decode HTML entities
            return Jsoup.parse(content).text();
        }
    }

    /**
     * Extracts plain text content from HTML, similar to how the frontend's textContent property works.
     * This method strips all HTML tags without including URLs, making it suitable for character counting.
     *
     * @param content The content to process (may be plain text or HTML)
     * @return Plain text content with all HTML tags removed
     */
    @NotNull
    public static String extractPlainTextContent(@NotNull String content) {
        if (content.trim().isEmpty()) {
            return "";
        }

        // Check if content looks like HTML (contains HTML tags)
        if (isHtmlContent(content)) {
            // Parse HTML and extract only the text content (like frontend textContent)
            org.jsoup.nodes.Document doc = Jsoup.parse(content);
            return doc.text();
        } else {
            // Content is plain text, return as-is but decode HTML entities
            return Jsoup.parse(content).text();
        }
    }
    
    /**
     * Fallback method using regex for text extraction when XML parsing fails.
     */
    @NotNull
    private static String extractTextContentFallback(@NotNull String xmlContent) {
        StringBuilder text = new StringBuilder();
        
        // Extract text from text attributes
        java.util.regex.Pattern textPattern = java.util.regex.Pattern.compile("text=\"([^\"]*?)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = textPattern.matcher(xmlContent);
        while (matcher.find()) {
            text.append(matcher.group(1)).append(" ");
        }
        
        // Extract content from note tags
        java.util.regex.Pattern notePattern = java.util.regex.Pattern.compile("<note[^>]*>\\s*<!\\[CDATA\\[([^\\]]*?)\\]\\]>\\s*</note>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        matcher = notePattern.matcher(xmlContent);
        while (matcher.find()) {
            text.append(matcher.group(1)).append(" ");
        }
        
        // Extract URLs from link attributes
        java.util.regex.Pattern linkPattern = java.util.regex.Pattern.compile("url=\"([^\"]*?)\"", java.util.regex.Pattern.CASE_INSENSITIVE);
        matcher = linkPattern.matcher(xmlContent);
        while (matcher.find()) {
            text.append(matcher.group(1)).append(" ");
        }
        
        return text.toString().trim();
    }
    
    /**
     * Fallback method using regex to check for HTML content.
     */
    private static boolean hasHtmlContentFallback(@NotNull String xmlContent) {
        java.util.regex.Pattern notePattern = java.util.regex.Pattern.compile("<note[^>]*>\\s*<!\\[CDATA\\[([^\\]]*?)\\]\\]>\\s*</note>", java.util.regex.Pattern.CASE_INSENSITIVE | java.util.regex.Pattern.DOTALL);
        java.util.regex.Matcher matcher = notePattern.matcher(xmlContent);
        while (matcher.find()) {
            String noteContent = matcher.group(1);
            if (isHtmlContent(noteContent)) {
                return true;
            }
        }
        
        return false;
    }
}
