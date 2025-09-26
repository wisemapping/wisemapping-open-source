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

package com.wisemapping.mindmap.service.action;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.mindmap.model.MapMetadata;
import com.wisemapping.mindmap.model.Topic;
import com.wisemapping.stomp.command.*;
import com.wisemapping.stomp.action.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ActionProcessor.
 * This class tests the coordination between StompCommands and their corresponding Actions.
 */
public class ActionProcessorTest {

    private ActionProcessor actionProcessor;
    private MapModel testMap;

    @BeforeEach
    public void setUp() {
        actionProcessor = new ActionProcessor();
        
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

    @Test
    @DisplayName("Should process AddTopic command successfully")
    public void testProcessAddTopicCommand() throws Exception {
        AddTopicCommand command = new AddTopicCommand("test-mindmap", "user1");
        command.setText("New Topic");
        command.setParentTopicId(1);

        Action.ActionResult result = actionProcessor.processCommand(command, testMap);

        assertTrue(result.isSuccess());
        assertEquals("ADD_TOPIC", result.getActionType());
        assertEquals("Topic added successfully", result.getMessage());
        assertNotNull(result.getResult());
        
        // Verify the topic was added
        Topic centralTopic = testMap.getCentralTopic();
        assertEquals(1, centralTopic.getChildren().size());
        assertEquals("New Topic", centralTopic.getChildren().get(0).getText());
    }

    @Test
    @DisplayName("Should process Delete command successfully")
    public void testProcessDeleteCommand() throws Exception {
        // First add a topic to delete
        AddTopicCommand addCommand = new AddTopicCommand("test-mindmap", "user1");
        addCommand.setText("Topic to Delete");
        addCommand.setParentTopicId(1);
        actionProcessor.processCommand(addCommand, testMap);

        // Now delete it
        DeleteCommand deleteCommand = new DeleteCommand("test-mindmap", "user1");
        deleteCommand.setTopicIds(Arrays.asList(2)); // Assuming the added topic gets ID 2

        Action.ActionResult result = actionProcessor.processCommand(deleteCommand, testMap);

        assertTrue(result.isSuccess());
        assertEquals("DELETE", result.getActionType());
        assertEquals("Deleted 1 topic(s) successfully", result.getMessage());
        
        // Verify the topic was deleted
        Topic centralTopic = testMap.getCentralTopic();
        assertEquals(0, centralTopic.getChildren().size());
    }

    @Test
    @DisplayName("Should process ChangeFeature command successfully")
    public void testProcessChangeFeatureCommand() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        ChangeFeatureCommand command = new ChangeFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("text");
        command.setNewValue("Updated Text");

        Action.ActionResult result = actionProcessor.processCommand(command, testMap);

        assertTrue(result.isSuccess());
        assertEquals("CHANGE_FEATURE", result.getActionType());
        assertEquals("Feature 'text' changed successfully", result.getMessage());
        
        // Verify the text was changed
        assertEquals("Updated Text", centralTopic.getText());
    }

    @Test
    @DisplayName("Should process AddFeature command successfully")
    public void testProcessAddFeatureCommand() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        AddFeatureCommand command = new AddFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), "note");
        command.setFeatureValue("Added note content");

        Action.ActionResult result = actionProcessor.processCommand(command, testMap);

        assertTrue(result.isSuccess());
        assertEquals("ADD_FEATURE", result.getActionType());
        assertEquals("Feature 'note' added successfully", result.getMessage());
        
        // Verify the note was added
        assertEquals("Added note content", centralTopic.getNote());
        assertTrue(centralTopic.hasNote());
    }

    @Test
    @DisplayName("Should process RemoveFeature command successfully")
    public void testProcessRemoveFeatureCommand() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        centralTopic.setNote("Note to be removed");
        String topicId = centralTopic.getId();

        RemoveFeatureCommand command = new RemoveFeatureCommand("test-mindmap", "user1", Integer.parseInt(topicId), 1);
        command.setFeatureType("note");

        Action.ActionResult result = actionProcessor.processCommand(command, testMap);

        assertTrue(result.isSuccess());
        assertEquals("REMOVE_FEATURE", result.getActionType());
        assertEquals("Feature 'note' removed successfully", result.getMessage());
        
        // Verify the note was removed
        assertNull(centralTopic.getNote());
        assertFalse(centralTopic.hasNote());
    }

    @Test
    @DisplayName("Should process DragTopic command successfully")
    public void testProcessDragTopicCommand() throws Exception {
        Topic centralTopic = testMap.getCentralTopic();
        String topicId = centralTopic.getId();

        DragTopicCommand command = new DragTopicCommand("test-mindmap", "user1", Integer.parseInt(topicId), 150.0, 250.0);

        Action.ActionResult result = actionProcessor.processCommand(command, testMap);

        assertTrue(result.isSuccess());
        assertEquals("DRAG_TOPIC", result.getActionType());
        assertEquals("Topic dragged to position (150.0, 250.0)", result.getMessage());
    }

    @Test
    @DisplayName("Should fail for unknown command type")
    public void testProcessUnknownCommand() {
        // Create a mock command with unknown type
        StompCommand unknownCommand = new StompCommand("test-mindmap", "user1") {
            @Override
            public String getActionType() {
                return "UNKNOWN_ACTION";
            }
            
            @Override
            public void validate() {
                // Mock validation
            }
        };

        assertThrows(Action.ActionExecutionException.class, () -> {
            actionProcessor.processCommand(unknownCommand, testMap);
        });
    }

    @Test
    @DisplayName("Should validate command parameters")
    public void testValidateCommandParameters() {
        // Test with null mindmap ID
        AddTopicCommand command = new AddTopicCommand(null, "user1");
        command.setText("Test Topic");

        assertThrows(Action.ActionExecutionException.class, () -> {
            actionProcessor.processCommand(command, testMap);
        });
    }

    @Test
    @DisplayName("Should check action registry")
    public void testActionRegistry() {
        assertTrue(actionProcessor.isActionRegistered("ADD_TOPIC"));
        assertTrue(actionProcessor.isActionRegistered("DELETE"));
        assertTrue(actionProcessor.isActionRegistered("CHANGE_FEATURE"));
        assertTrue(actionProcessor.isActionRegistered("ADD_FEATURE"));
        assertTrue(actionProcessor.isActionRegistered("REMOVE_FEATURE"));
        assertTrue(actionProcessor.isActionRegistered("DRAG_TOPIC"));
        
        assertFalse(actionProcessor.isActionRegistered("UNKNOWN_ACTION"));
        
        assertTrue(actionProcessor.getRegisteredActionCount() >= 6);
        assertTrue(actionProcessor.getRegisteredActionTypes().contains("ADD_TOPIC"));
    }

    @Test
    @DisplayName("Should handle complex multi-step operations")
    public void testComplexMultiStepOperations() throws Exception {
        // Step 1: Add a topic
        AddTopicCommand addCommand = new AddTopicCommand("test-mindmap", "user1");
        addCommand.setText("Complex Topic");
        addCommand.setParentTopicId(1);
        Action.ActionResult addResult = actionProcessor.processCommand(addCommand, testMap);
        assertTrue(addResult.isSuccess());

        // Step 2: Add a note to the topic
        AddFeatureCommand noteCommand = new AddFeatureCommand("test-mindmap", "user1", 2, "note");
        noteCommand.setFeatureValue("Complex note content");
        Action.ActionResult noteResult = actionProcessor.processCommand(noteCommand, testMap);
        assertTrue(noteResult.isSuccess());

        // Step 3: Change the topic text
        ChangeFeatureCommand changeCommand = new ChangeFeatureCommand("test-mindmap", "user1", 2, 1);
        changeCommand.setFeatureType("text");
        changeCommand.setNewValue("Updated Complex Topic");
        Action.ActionResult changeResult = actionProcessor.processCommand(changeCommand, testMap);
        assertTrue(changeResult.isSuccess());

        // Step 4: Drag the topic
        DragTopicCommand dragCommand = new DragTopicCommand("test-mindmap", "user1", 2, 300.0, 400.0);
        Action.ActionResult dragResult = actionProcessor.processCommand(dragCommand, testMap);
        assertTrue(dragResult.isSuccess());

        // Verify final state
        Topic centralTopic = testMap.getCentralTopic();
        assertEquals(1, centralTopic.getChildren().size());
        
        Topic complexTopic = centralTopic.getChildren().get(0);
        assertEquals("Updated Complex Topic", complexTopic.getText());
        assertEquals("Complex note content", complexTopic.getNote());
        assertTrue(complexTopic.hasContent());
    }

    @Test
    @DisplayName("Should update metadata after each action")
    public void testMetadataUpdate() throws Exception {
        LocalDateTime originalModifiedDate = testMap.getMetadata().getLastModifiedDate();
        
        // Wait a bit to ensure timestamp difference
        Thread.sleep(10);
        
        AddTopicCommand command = new AddTopicCommand("test-mindmap", "user1");
        command.setText("Metadata Test Topic");
        command.setParentTopicId(1);
        
        actionProcessor.processCommand(command, testMap);
        
        // Verify metadata was updated
        LocalDateTime newModifiedDate = testMap.getMetadata().getLastModifiedDate();
        assertTrue(newModifiedDate.isAfter(originalModifiedDate));
    }
}
