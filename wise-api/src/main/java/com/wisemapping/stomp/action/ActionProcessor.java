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

package com.wisemapping.stomp.action;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.stomp.command.*;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * ActionProcessor coordinates between StompCommands and their corresponding Actions.
 * This class maintains a registry of action implementations and delegates command
 * execution to the appropriate action.
 * 
 * The processor is responsible for:
 * - Mapping command types to their corresponding actions
 * - Validating commands before execution
 * - Executing actions and handling results
 * - Managing the action lifecycle (do -> update)
 */
public class ActionProcessor {
    
    private final Map<String, Action<? extends StompCommand>> actionRegistry;
    
    public ActionProcessor() {
        this.actionRegistry = new HashMap<>();
        initializeActionRegistry();
    }
    
    /**
     * Processes a STOMP command by delegating to the appropriate action.
     * 
     * @param command The command to process
     * @param mapModel The mindmap model to modify
     * @return The result of the action execution
     * @throws ActionExecutionException if the action cannot be executed
     */
    @NotNull
    public Action.ActionResult processCommand(@NotNull StompCommand command, @NotNull MapModel mapModel) 
            throws Action.ActionExecutionException {
        
        String actionType = command.getActionType();
        Action<? extends StompCommand> action = actionRegistry.get(actionType);
        
        if (action == null) {
            throw new Action.ActionExecutionException("No action found for command type: " + actionType);
        }
        
        // Validate the command
        validateCommand(command);
        
        // Check if the action can be applied
        if (!canApplyAction(action, command, mapModel)) {
            throw new Action.ActionExecutionException("Action cannot be applied: " + actionType);
        }
        
        // Execute the action
        Action.ActionResult result = executeAction(action, command, mapModel);
        
        // Update the mindmap after the action
        updateAfterAction(action, command, mapModel);
        
        return result;
    }
    
    /**
     * Validates a command before processing.
     */
    private void validateCommand(@NotNull StompCommand command) throws Action.ActionExecutionException {
        if (command.getMindmapId() == null || command.getMindmapId().trim().isEmpty()) {
            throw new Action.ActionExecutionException("Mindmap ID cannot be null or empty");
        }
        
        if (command.getUserId() == null || command.getUserId().trim().isEmpty()) {
            throw new Action.ActionExecutionException("User ID cannot be null or empty");
        }
        
        if (command.getActionId() == null || command.getActionId().trim().isEmpty()) {
            throw new Action.ActionExecutionException("Action ID cannot be null or empty");
        }
        
        // Call the command's own validation
        command.validate();
    }
    
    /**
     * Checks if an action can be applied to the given command and mindmap.
     */
    @SuppressWarnings("unchecked")
    private boolean canApplyAction(@NotNull Action<? extends StompCommand> action, 
                                  @NotNull StompCommand command, 
                                  @NotNull MapModel mapModel) {
        try {
            Action<StompCommand> typedAction = (Action<StompCommand>) action;
            return typedAction.canApply(command, mapModel);
        } catch (ClassCastException e) {
            return false;
        }
    }
    
    /**
     * Executes an action with the given command and mindmap.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    private Action.ActionResult executeAction(@NotNull Action<? extends StompCommand> action,
                                            @NotNull StompCommand command,
                                            @NotNull MapModel mapModel) throws Action.ActionExecutionException {
        Action<StompCommand> typedAction = (Action<StompCommand>) action;
        return typedAction.doAction(command, mapModel);
    }
    
    /**
     * Updates the mindmap after an action has been executed.
     */
    @SuppressWarnings("unchecked")
    private void updateAfterAction(@NotNull Action<? extends StompCommand> action,
                                 @NotNull StompCommand command,
                                 @NotNull MapModel mapModel) throws Action.ActionExecutionException {
        Action<StompCommand> typedAction = (Action<StompCommand>) action;
        typedAction.update(command, mapModel);
    }
    
    /**
     * Initializes the action registry with all available actions.
     */
    private void initializeActionRegistry() {
        // Register all action implementations
        registerAction(new AddTopicActionImpl());
        registerAction(new DeleteActionImpl());
        registerAction(new ChangeFeatureActionImpl());
        registerAction(new AddFeatureActionImpl());
        registerAction(new RemoveFeatureActionImpl());
        registerAction(new DragTopicActionImpl());
        
        // TODO: Register remaining actions when implemented
        // registerAction(new AddRelationshipActionImpl());
        // registerAction(new MoveControlPointActionImpl());
        // registerAction(new GenericFunctionActionImpl());
    }
    
    /**
     * Registers an action in the registry.
     */
    private void registerAction(@NotNull Action<? extends StompCommand> action) {
        actionRegistry.put(action.getActionType(), action);
    }
    
    /**
     * Gets the number of registered actions.
     */
    public int getRegisteredActionCount() {
        return actionRegistry.size();
    }
    
    /**
     * Checks if an action type is registered.
     */
    public boolean isActionRegistered(@NotNull String actionType) {
        return actionRegistry.containsKey(actionType);
    }
    
    /**
     * Gets all registered action types.
     */
    @NotNull
    public java.util.Set<String> getRegisteredActionTypes() {
        return actionRegistry.keySet();
    }
}
