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

package com.wisemapping.mindmap.serializer;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.model.MapMetadata;
import com.wisemapping.mindmap.model.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Serializes MapModel to XML format.
 * This class provides the reverse operation of MindmapParser.
 */
public class XmlMindmapSerializer {
    
    /**
     * Serializes a MapModel to XML string.
     * 
     * @param mapModel The map model to serialize
     * @return XML string representation
     * @throws SerializationException if serialization fails
     */
    @NotNull
    public static String serializeToXml(@NotNull MapModel mapModel) throws SerializationException {
        try {
            Document document = createDocument();
            Element rootElement = createRootElement(document, mapModel);
            document.appendChild(rootElement);
            
            // Add topics
            for (Topic topic : mapModel.getTopics()) {
                Element topicElement = serializeTopic(document, topic);
                rootElement.appendChild(topicElement);
            }
            
            return documentToString(document);
        } catch (Exception e) {
            throw new SerializationException("Failed to serialize MapModel to XML", e);
        }
    }
    
    /**
     * Creates a new XML document.
     */
    @NotNull
    private static Document createDocument() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(false);
        factory.setValidating(false);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }
    
    /**
     * Creates the root map element with attributes.
     */
    @NotNull
    private static Element createRootElement(@NotNull Document document, @NotNull MapModel mapModel) {
        Element rootElement = document.createElement("map");
        
        // Set map attributes
        if (mapModel.getTitle() != null && !mapModel.getTitle().trim().isEmpty()) {
            rootElement.setAttribute("name", mapModel.getTitle());
        }
        
        // Set metadata attributes
        MapMetadata metadata = mapModel.getMetadata();
        if (metadata.getVersion() != null && !metadata.getVersion().trim().isEmpty()) {
            rootElement.setAttribute("version", metadata.getVersion());
        } else {
            // Default version if not specified
            rootElement.setAttribute("version", "tango");
        }
        if (metadata.getTheme() != null && !metadata.getTheme().trim().isEmpty()) {
            rootElement.setAttribute("theme", metadata.getTheme());
        }
        
        return rootElement;
    }
    
    /**
     * Serializes a topic to XML element.
     */
    @NotNull
    private static Element serializeTopic(@NotNull Document document, @NotNull Topic topic) {
        Element topicElement = document.createElement("topic");
        
        // Set topic attributes
        if (topic.getId() != null && !topic.getId().trim().isEmpty()) {
            topicElement.setAttribute("id", topic.getId());
        }
        if (topic.isCentral()) {
            topicElement.setAttribute("central", "true");
        }
        
        // Handle text content - can be in attribute or element
        if (topic.getText() != null && !topic.getText().trim().isEmpty()) {
            // For simple text, use attribute
            if (!topic.getText().contains("\n") && !topic.getText().contains("<")) {
                topicElement.setAttribute("text", topic.getText());
            } else {
                // For complex text with newlines or HTML, use text element with CDATA
                Element textElement = document.createElement("text");
                textElement.appendChild(document.createCDATASection(topic.getText()));
                topicElement.appendChild(textElement);
            }
        }
        
        // Add child elements for complex content
        if (topic.getNote() != null && !topic.getNote().trim().isEmpty()) {
            Element noteElement = document.createElement("note");
            noteElement.appendChild(document.createCDATASection(topic.getNote()));
            topicElement.appendChild(noteElement);
        }
        
        if (topic.getLinkUrl() != null && !topic.getLinkUrl().trim().isEmpty()) {
            Element linkElement = document.createElement("link");
            linkElement.setAttribute("url", topic.getLinkUrl());
            topicElement.appendChild(linkElement);
        }
        
        // Recursively serialize child topics
        for (Topic childTopic : topic.getChildren()) {
            Element childElement = serializeTopic(document, childTopic);
            topicElement.appendChild(childElement);
        }
        
        return topicElement;
    }
    
    /**
     * Converts a document to XML string.
     */
    @NotNull
    private static String documentToString(@NotNull Document document) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(document);
        transformer.transform(source, result);
        
        return writer.toString();
    }
    
    /**
     * Exception thrown when XML serialization fails.
     */
    public static class SerializationException extends Exception {
        public SerializationException(@NotNull String message, @Nullable Throwable cause) {
            super(message, cause);
        }
    }
}
