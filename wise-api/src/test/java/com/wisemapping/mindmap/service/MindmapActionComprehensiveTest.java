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

package com.wisemapping.mindmap.service;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.model.MapMetadata;
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.stomp.command.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive test class for each mindmap action.
 * This class tests every possible action that can be performed on a mindmap,
 * ensuring each operation works correctly and maintains data integrity.
 */
public class MindmapActionComprehensiveTest {

    private MindmapOperationService operationService;
    private MapModel testMap;

    @BeforeEach
    public void setUp() {
        operationService = new MindmapOperationService();
        
        // Create a test mindmap with a central topic
        testMap = new MapModel("Test Mindmap");
        testMap.getMetadata().setVersion("1.0");
        testMap.getMetadata().setTheme("default");
        testMap.getMetadata().setAuthor("TestUser");
        testMap.getMetadata().setCreatedDate(LocalDateTime.now());
        testMap.getMetadata().setLastModifiedDate(LocalDateTime.now());
        
        // Add central topic
        Topic centralTopic = new Topic("Central Topic");
        centralTopic.setCentral(true);
        centralTopic.setId("1");
        testMap.addTopic(centralTopic);
    }

    // ==================== ADD TOPIC TESTS ====================

    @Test
    @DisplayName("Should add a new topic to the mindmap")
    public void testAddTopic() throws Exception {
        AddTopicCommand command = new AddTopicCommand("test-mindmap", "user1");
        command.setText("New Topic");
        command.setParentTopicId(1);

        Topic newTopic = operationService.addTopic(testMap, command);

        assertNotNull(newTopic);
        assertEquals("New Topic", newTopic.getText());
        assertNotNull(newTopic.getId());
        assertFalse(newTopic.isCentral());
        
        // Verify the topic was added to the central topic's children
        Topic centralTopic = testMap.getCentralTopic();
        assertTrue(centralTopic.getChildren().contains(newTopic));
        assertEquals(1, centralTopic.getChildren().size());
    }

    @Test
    @DisplayName("Should add a topic with note and link")
    public void testAddTopicWithNoteAndLink() throws Exception {
        AddTopicCommand command = new AddTopicCommand("test-mindmap", "user1");
        command.setText("Rich Topic");
        command.setNote("This is a note");
        command.setLinkUrl("http://example.com");
        command.setParentTopicId(1);

        Topic newTopic = operationService.addTopic(testMap, command);

        assertEquals("Rich Topic", newTopic.getText());
        assertEquals("This is a note", newTopic.getNote());
        assertEquals("http://example.com", newTopic.getLinkUrl());
    }

    @Test
    @DisplayName("Should add nested topics (grandchild)")
    public void testAddNestedTopic() throws Exception {
        // First, add a child topic
        AddTopicCommand childCommand = new AddTopicCommand("test-mindmap", "user1");
        childCommand.setText("Child Topic");
        childCommand.setParentTopicId(1);
        Topic childTopic = operationService.addTopic(testMap, childCommand);

        // Then add a grandchild topic
        AddTopicCommand grandchildCommand = new AddTopicCommand("test-mindmap", "user1");
        grandchildCommand.setText("Grandchild Topic");
        grandchildCommand.setParentTopicId(Integer.parseInt(childTopic.getId()));
        Topic grandchildTopic = operationService.addTopic(testMap, grandchildCommand);

        assertNotNull(grandchildTopic);
        assertEquals("Grandchild Topic", grandchildTopic.getText());
        assertTrue(childTopic.getChildren().contains(grandchildTopic));
    }

    // ==================== ADD FEATURE TESTS ====================

    @Test
    @DisplayName("Should add note feature to existing topic")
    public void testAddNoteFeature() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        AddFeatureCommand command = new AddFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), "note");
        command.setFeatureValue("Added note content");

        operationService.addFeature(testMap, command);

        assertEquals("Added note content", centralTopic.getNote());
        assertTrue(centralTopic.hasNote());
    }

    @Test
    @DisplayName("Should add link feature to existing topic")
    public void testAddLinkFeature() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        AddFeatureCommand command = new AddFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), "link");
        command.setFeatureValue("https://example.com");

        operationService.addFeature(testMap, command);

        assertEquals("https://example.com", centralTopic.getLinkUrl());
        assertTrue(centralTopic.hasLink());
    }

    @Test
    @DisplayName("Should add multiple topics with different features")
    public void testAddMultipleTopicsWithFeatures() throws Exception {
        // Add first topic with note
        AddTopicCommand addCommand1 = new AddTopicCommand("test-mindmap", "user1");
        addCommand1.setText("Topic with Note");
        addCommand1.setNote("This topic has a note");
        addCommand1.setParentTopicId(1);
        Topic topic1 = operationService.addTopic(testMap, addCommand1);

        // Add second topic with link
        AddTopicCommand addCommand2 = new AddTopicCommand("test-mindmap", "user1");
        addCommand2.setText("Topic with Link");
        addCommand2.setLinkUrl("https://example.com");
        addCommand2.setParentTopicId(1);
        Topic topic2 = operationService.addTopic(testMap, addCommand2);

        // Verify both topics were added correctly
        assertEquals(2, testMap.getCentralTopic().getChildren().size());
        assertEquals("This topic has a note", topic1.getNote());
        assertEquals("https://example.com", topic2.getLinkUrl());
        assertTrue(topic1.hasNote());
        assertTrue(topic2.hasLink());
    }

    // ==================== CHANGE FEATURE TESTS ====================

    @Test
    @DisplayName("Should change topic text")
    public void testChangeTopicText() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        ChangeFeatureCommand command = new ChangeFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("text");
        command.setNewValue("Changed text");

        operationService.changeFeature(testMap, command);

        assertEquals("Changed text", centralTopic.getText());
    }

    @Test
    @DisplayName("Should change topic note")
    public void testChangeTopicNote() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        centralTopic.setNote("Original note");
        String topicId = centralTopic.getId();

        ChangeFeatureCommand command = new ChangeFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("note");
        command.setNewValue("Updated note");

        operationService.changeFeature(testMap, command);

        assertEquals("Updated note", centralTopic.getNote());
    }

    @Test
    @DisplayName("Should change topic link")
    public void testChangeTopicLink() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        centralTopic.setLinkUrl("http://original.com");
        String topicId = centralTopic.getId();

        ChangeFeatureCommand command = new ChangeFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("link");
        command.setNewValue("https://updated.com");

        operationService.changeFeature(testMap, command);

        assertEquals("https://updated.com", centralTopic.getLinkUrl());
    }

    // ==================== REMOVE FEATURE TESTS ====================

    @Test
    @DisplayName("Should remove note feature from topic")
    public void testRemoveNoteFeature() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        centralTopic.setNote("Note to be removed");
        String topicId = centralTopic.getId();

        RemoveFeatureCommand command = new RemoveFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("note");

        operationService.removeFeature(testMap, command);

        assertNull(centralTopic.getNote());
        assertFalse(centralTopic.hasNote());
    }

    @Test
    @DisplayName("Should remove link feature from topic")
    public void testRemoveLinkFeature() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        centralTopic.setLinkUrl("http://example.com");
        String topicId = centralTopic.getId();

        RemoveFeatureCommand command = new RemoveFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("link");

        operationService.removeFeature(testMap, command);

        assertNull(centralTopic.getLinkUrl());
        assertFalse(centralTopic.hasLink());
    }

    // ==================== DELETE TESTS ====================

    @Test
    @DisplayName("Should delete a single topic")
    public void testDeleteSingleTopic() throws Exception {
        // Add a topic to delete
        AddTopicCommand addCommand = new AddTopicCommand("test-mindmap", "user1");
        addCommand.setText("Topic to Delete");
        addCommand.setParentTopicId(1);
        Topic topicToDelete = operationService.addTopic(testMap, addCommand);

        // Delete the topic
        DeleteCommand command = new DeleteCommand("test-mindmap", "user1");
        command.setTopicIds(Arrays.asList(Integer.parseInt(topicToDelete.getId())));

        operationService.deleteTopics(testMap, command);

        // Verify topic was removed
        Topic centralTopic = testMap.getCentralTopic();
        assertFalse(centralTopic.getChildren().contains(topicToDelete));
        assertEquals(0, centralTopic.getChildren().size());
    }

    @Test
    @DisplayName("Should delete multiple topics")
    public void testDeleteMultipleTopics() throws Exception {
        // Add multiple topics
        AddTopicCommand addCommand1 = new AddTopicCommand("test-mindmap", "user1");
        addCommand1.setText("Topic 1");
        addCommand1.setParentTopicId(1);
        Topic topic1 = operationService.addTopic(testMap, addCommand1);
        topic1.setId("2"); // Ensure numeric ID

        AddTopicCommand addCommand2 = new AddTopicCommand("test-mindmap", "user1");
        addCommand2.setText("Topic 2");
        addCommand2.setParentTopicId(1);
        Topic topic2 = operationService.addTopic(testMap, addCommand2);
        topic2.setId("3"); // Ensure numeric ID

        // Delete both topics
        DeleteCommand command = new DeleteCommand("test-mindmap", "user1");
        command.setTopicIds(Arrays.asList(2, 3));

        operationService.deleteTopics(testMap, command);

        // Verify both topics were removed
        Topic centralTopic = testMap.getCentralTopic();
        assertEquals(0, centralTopic.getChildren().size());
    }

    @Test
    @DisplayName("Should handle deleting non-existent topic gracefully")
    public void testDeleteNonExistentTopic() throws Exception {
        DeleteCommand command = new DeleteCommand("test-mindmap", "user1");
        command.setTopicIds(Arrays.asList(999)); // Non-existent ID

        assertDoesNotThrow(() -> operationService.deleteTopics(testMap, command));
    }

    // ==================== DRAG TESTS ====================

    @Test
    @DisplayName("Should drag topic to new position")
    public void testDragTopic() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        DragTopicCommand command = new DragTopicCommand("test-mindmap", "user1", Integer.parseInt(topicId), 150.0, 250.0);

        assertDoesNotThrow(() -> operationService.dragTopic(testMap, command));
    }

    @Test
    @DisplayName("Should drag topic with negative coordinates")
    public void testDragTopicWithNegativeCoordinates() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        DragTopicCommand command = new DragTopicCommand("test-mindmap", "user1", Integer.parseInt(topicId), -100.0, -200.0);

        assertDoesNotThrow(() -> operationService.dragTopic(testMap, command));
    }

    // ==================== COMPLEX SCENARIO TESTS ====================

    @Test
    @DisplayName("Should handle complex multi-step operations")
    public void testComplexMultiStepOperations() throws Exception {
        // Step 1: Add multiple topics
        AddTopicCommand addCommand1 = new AddTopicCommand("test-mindmap", "user1");
        addCommand1.setText("Topic A");
        addCommand1.setParentTopicId(1);
        Topic topicA = operationService.addTopic(testMap, addCommand1);
        topicA.setId("2");

        AddTopicCommand addCommand2 = new AddTopicCommand("test-mindmap", "user1");
        addCommand2.setText("Topic B");
        addCommand2.setParentTopicId(1);
        Topic topicB = operationService.addTopic(testMap, addCommand2);
        topicB.setId("3");

        // Step 2: Add features to topics
        AddFeatureCommand noteCommand = new AddFeatureCommand("test-mindmap", "user1", 2, "note");
        noteCommand.setFeatureValue("Note for Topic A");
        operationService.addFeature(testMap, noteCommand);

        AddFeatureCommand linkCommand = new AddFeatureCommand("test-mindmap", "user1", 3, "link");
        linkCommand.setFeatureValue("https://topic-b.com");
        operationService.addFeature(testMap, linkCommand);

        // Step 3: Change features
        ChangeFeatureCommand changeCommand = new ChangeFeatureCommand("test-mindmap", "user1", 2, 1);
        changeCommand.setFeatureType("text");
        changeCommand.setNewValue("Updated Topic A");
        operationService.changeFeature(testMap, changeCommand);

        // Step 4: Drag topics
        DragTopicCommand dragCommand = new DragTopicCommand("test-mindmap", "user1", 3, 300.0, 400.0);
        operationService.dragTopic(testMap, dragCommand);

        // Verify final state
        assertEquals(2, testMap.getCentralTopic().getChildren().size());
        assertEquals("Updated Topic A", topicA.getText());
        assertEquals("Note for Topic A", topicA.getNote());
        assertEquals("https://topic-b.com", topicB.getLinkUrl());
    }

    @Test
    @DisplayName("Should maintain data integrity during multiple operations")
    public void testDataIntegrityDuringMultipleOperations() throws Exception {
        // Initial state
        int initialTopicCount = testMap.getTotalTopicCount();
        Topic centralTopic = testMap.getCentralTopic();
        String originalText = centralTopic.getText();

        // Perform multiple operations
        AddTopicCommand addCommand = new AddTopicCommand("test-mindmap", "user1");
        addCommand.setText("New Topic");
        addCommand.setParentTopicId(1);
        Topic newTopic = operationService.addTopic(testMap, addCommand);

        ChangeFeatureCommand changeCommand = new ChangeFeatureCommand("test-mindmap", "user1", Integer.parseInt(newTopic.getId()), 1);
        changeCommand.setFeatureType("note");
        changeCommand.setNewValue("Topic note");
        operationService.changeFeature(testMap, changeCommand);

        AddFeatureCommand linkCommand = new AddFeatureCommand("test-mindmap", "user1", Integer.parseInt(newTopic.getId()), "link");
        linkCommand.setFeatureValue("http://example.com");
        operationService.addFeature(testMap, linkCommand);

        // Verify data integrity
        assertEquals(initialTopicCount + 1, testMap.getTotalTopicCount());
        assertEquals(originalText, centralTopic.getText()); // Central topic unchanged
        assertEquals("New Topic", newTopic.getText());
        assertEquals("Topic note", newTopic.getNote());
        assertEquals("http://example.com", newTopic.getLinkUrl());
        assertTrue(newTopic.hasContent());
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    @DisplayName("Should handle operations on empty mindmap")
    public void testOperationsOnEmptyMindmap() throws Exception {
        MapModel emptyMap = new MapModel("Empty Map");
        
        AddTopicCommand command = new AddTopicCommand("test-mindmap", "user1");
        command.setText("First Topic");

        Topic newTopic = operationService.addTopic(emptyMap, command);

        assertNotNull(newTopic);
        assertEquals("First Topic", newTopic.getText());
        assertEquals(1, emptyMap.getTotalTopicCount());
    }

    @Test
    @DisplayName("Should handle operations with special characters")
    public void testOperationsWithSpecialCharacters() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        // Test with special characters in text
        ChangeFeatureCommand textCommand = new ChangeFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        textCommand.setFeatureType("text");
        textCommand.setNewValue("Topic with special chars: <>&\"'");
        operationService.changeFeature(testMap, textCommand);

        // Test with special characters in note
        AddFeatureCommand noteCommand = new AddFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), "note");
        noteCommand.setFeatureValue("Note with <b>HTML</b> and & symbols");
        operationService.addFeature(testMap, noteCommand);

        assertEquals("Topic with special chars: <>&\"'", centralTopic.getText());
        assertEquals("Note with <b>HTML</b> and & symbols", centralTopic.getNote());
    }

    @Test
    @DisplayName("Should handle operations with very long content")
    public void testOperationsWithLongContent() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        // Create very long content
        StringBuilder longText = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longText.append("Very long text content. ");
        }

        ChangeFeatureCommand command = new ChangeFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("text");
        command.setNewValue(longText.toString());
        operationService.changeFeature(testMap, command);

        assertEquals(longText.toString(), centralTopic.getText());
        assertTrue(centralTopic.getText().length() > 10000);
    }
}