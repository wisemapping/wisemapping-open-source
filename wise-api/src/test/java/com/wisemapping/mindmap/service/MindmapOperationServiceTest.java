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
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.stomp.command.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for MindmapOperationService to verify mindmap operations.
 */
public class MindmapOperationServiceTest {

    private MindmapOperationService operationService;
    private MapModel testMap;

    @BeforeEach
    public void setUp() {
        operationService = new MindmapOperationService();
        testMap = createTestMindmap();
    }

    @Test
    @DisplayName("Should add topic to root level successfully")
    public void testAddTopicToRoot() throws Exception {
        AddTopicCommand command = new AddTopicCommand("test-mindmap", "user1");
        command.setText("New Root Topic");
        command.setNote("This is a note");
        command.setLinkUrl("http://example.com");
        
        Topic addedTopic = operationService.addTopic(testMap, command);
        
        assertNotNull(addedTopic);
        assertEquals("New Root Topic", addedTopic.getText());
        assertEquals("This is a note", addedTopic.getNote());
        assertEquals("http://example.com", addedTopic.getLinkUrl());
        assertNotNull(addedTopic.getId());
        
        // Verify topic was added to mindmap
        assertTrue(testMap.getTopics().contains(addedTopic));
        assertEquals(2, testMap.getTopics().size()); // Original + new topic
    }

    @Test
    @DisplayName("Should add topic as child of existing topic")
    public void testAddTopicAsChild() throws Exception {
        // Find the central topic
        Topic centralTopic = testMap.getCentralTopic();
        assertNotNull(centralTopic);
        String parentId = centralTopic.getId();
        
        AddTopicCommand command = new AddTopicCommand("test-mindmap", "user1");
        command.setText("New Child Topic");
        command.setParentTopicId(Integer.parseInt(parentId));
        
        Topic addedTopic = operationService.addTopic(testMap, command);
        
        assertNotNull(addedTopic);
        assertEquals("New Child Topic", addedTopic.getText());
        
        // Verify topic was added as child
        assertTrue(centralTopic.getChildren().contains(addedTopic));
        assertEquals(1, centralTopic.getChildren().size());
    }

    @Test
    @DisplayName("Should add note feature to existing topic")
    public void testAddNoteFeature() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();
        
        AddFeatureCommand command = new AddFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), "note");
        command.setFeatureValue("This is a new note");
        
        operationService.addFeature(testMap, command);
        
        assertEquals("This is a new note", centralTopic.getNote());
    }

    @Test
    @DisplayName("Should add link feature to existing topic")
    public void testAddLinkFeature() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();
        
        AddFeatureCommand command = new AddFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), "link");
        command.setFeatureValue("http://newexample.com");
        
        operationService.addFeature(testMap, command);
        
        assertEquals("http://newexample.com", centralTopic.getLinkUrl());
    }

    @Test
    @DisplayName("Should change text feature of existing topic")
    public void testChangeTextFeature() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();
        String originalText = centralTopic.getText();
        
        ChangeFeatureCommand command = new ChangeFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("text");
        command.setNewValue("Updated Text");
        
        operationService.changeFeature(testMap, command);
        
        assertEquals("Updated Text", centralTopic.getText());
    }

    @Test
    @DisplayName("Should change note feature of existing topic")
    public void testChangeNoteFeature() throws Exception {
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
    @DisplayName("Should remove note feature from existing topic")
    public void testRemoveNoteFeature() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        centralTopic.setNote("Original note");
        String topicId = centralTopic.getId();
        
        RemoveFeatureCommand command = new RemoveFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("note");
        
        operationService.removeFeature(testMap, command);
        
        assertNull(centralTopic.getNote());
    }

    @Test
    @DisplayName("Should remove link feature from existing topic")
    public void testRemoveLinkFeature() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        centralTopic.setLinkUrl("http://example.com");
        String topicId = centralTopic.getId();
        
        RemoveFeatureCommand command = new RemoveFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("link");
        
        operationService.removeFeature(testMap, command);
        
        assertNull(centralTopic.getLinkUrl());
    }

    @Test
    @DisplayName("Should delete topic successfully")
    public void testDeleteTopic() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();
        int originalTopicCount = testMap.getTotalTopicCount();
        
        DeleteCommand command = new DeleteCommand("test-mindmap", "user1");
        command.setTopicIds(java.util.Arrays.asList(Integer.parseInt(topicId)));
        
        operationService.deleteTopics(testMap, command);
        
        // Verify topic was removed
        assertFalse(testMap.getTopics().contains(centralTopic));
        assertEquals(originalTopicCount - 1, testMap.getTotalTopicCount());
    }

    @Test
    @DisplayName("Should handle drag topic operation")
    public void testDragTopic() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();
        
        DragTopicCommand command = new DragTopicCommand("test-mindmap", "user1", Integer.parseInt(topicId), 100.0, 200.0);
        
        // Should not throw exception
        assertDoesNotThrow(() -> operationService.dragTopic(testMap, command));
    }

    @Test
    @DisplayName("Should throw exception when adding topic to non-existent parent")
    public void testAddTopicToNonExistentParent() {
        AddTopicCommand command = new AddTopicCommand("test-mindmap", "user1");
        command.setText("New Topic");
        command.setParentTopicId(99999); // Non-existent ID
        
        assertThrows(MindmapOperationService.MindmapOperationException.class, () -> {
            operationService.addTopic(testMap, command);
        });
    }

    @Test
    @DisplayName("Should throw exception when adding feature to non-existent topic")
    public void testAddFeatureToNonExistentTopic() {
        AddFeatureCommand command = new AddFeatureCommand("test-mindmap", "user1", 99999, "note");
        command.setFeatureValue("Note value");
        
        assertThrows(MindmapOperationService.MindmapOperationException.class, () -> {
            operationService.addFeature(testMap, command);
        });
    }

    @Test
    @DisplayName("Should throw exception when changing feature of non-existent topic")
    public void testChangeFeatureOfNonExistentTopic() {
        ChangeFeatureCommand command = new ChangeFeatureCommand("test-mindmap", "user1", 99999, 1);
        command.setFeatureType("text");
        command.setNewValue("New value");
        
        assertThrows(MindmapOperationService.MindmapOperationException.class, () -> {
            operationService.changeFeature(testMap, command);
        });
    }

    @Test
    @DisplayName("Should throw exception for unsupported feature type")
    public void testUnsupportedFeatureType() {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();
        
        AddFeatureCommand command = new AddFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), "unsupported");
        command.setFeatureValue("Some value");
        
        assertThrows(MindmapOperationService.MindmapOperationException.class, () -> {
            operationService.addFeature(testMap, command);
        });
    }

    @Test
    @DisplayName("Should validate command before executing operation")
    public void testCommandValidation() {
        AddTopicCommand invalidCommand = new AddTopicCommand();
        // Missing required fields
        
        assertThrows(MindmapOperationService.MindmapOperationException.class, () -> {
            operationService.addTopic(testMap, invalidCommand);
        });
    }

    @Test
    @DisplayName("Should handle deleting multiple topics")
    public void testDeleteMultipleTopics() throws Exception {
        // Add another topic with a numeric ID
        AddTopicCommand addCommand = new AddTopicCommand("test-mindmap", "user1");
        addCommand.setText("Second Topic");
        Topic secondTopic = operationService.addTopic(testMap, addCommand);
        
        // Set a numeric ID for the second topic to avoid UUID parsing issues
        secondTopic.setId("2");
        
        Topic centralTopic = testMap.getCentralTopic();
        
        DeleteCommand command = new DeleteCommand("test-mindmap", "user1");
        command.setTopicIds(java.util.Arrays.asList(
            Integer.parseInt(centralTopic.getId()),
            Integer.parseInt(secondTopic.getId())
        ));
        
        operationService.deleteTopics(testMap, command);
        
        // Both topics should be removed
        assertTrue(testMap.getTopics().isEmpty());
    }

    /**
     * Creates a test mindmap for testing operations.
     */
    private MapModel createTestMindmap() {
        MapModel map = new MapModel("Test Mindmap");
        
        Topic centralTopic = new Topic("Central Topic");
        centralTopic.setCentral(true);
        centralTopic.setId("1");
        map.addTopic(centralTopic);
        
        return map;
    }
}
