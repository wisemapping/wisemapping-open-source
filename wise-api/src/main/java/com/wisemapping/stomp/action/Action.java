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
import com.wisemapping.stomp.command.StompCommand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base interface for all mindmap actions.
 * Each action implementation handles a specific type of mindmap operation.
 * 
 * Actions are designed to be immutable objects without state - they contain
 * only the logic to modify the map. They take a StompCommand and apply the 
 * changes to a MapModel.
 */
public interface Action<T extends StompCommand> {
    
    /**
     * Executes the action on the given MapModel.
     * This method applies the changes described by the StompCommand to the MapModel.
     * 
     * @param command The command to execute
     * @param mapModel The mindmap model to modify
     * @return The result of the action execution
     * @throws ActionExecutionException if the action cannot be executed
     */
    @NotNull
    ActionResult doAction(@NotNull T command, @NotNull MapModel mapModel) throws ActionExecutionException;
    
    /**
     * Updates the mindmap model after the action has been executed.
     * This method can be used to perform post-processing, validation, or cleanup.
     * 
     * @param command The command that was executed
     * @param mapModel The mindmap model that was modified
     * @throws ActionExecutionException if the update fails
     */
    void update(@NotNull T command, @NotNull MapModel mapModel) throws ActionExecutionException;
    
    /**
     * Validates the command before execution.
     * 
     * @param command The command to validate
     * @throws ActionExecutionException if validation fails
     */
    void validate(@NotNull T command) throws ActionExecutionException;
    
    /**
     * Gets the action type this implementation handles.
     * 
     * @return The action type identifier
     */
    @NotNull
    String getActionType();
    
    /**
     * Checks if this action can be applied to the given mindmap model.
     * 
     * @param command The command to check
     * @param mapModel The mindmap model to check against
     * @return true if the action can be applied, false otherwise
     */
    boolean canApply(@NotNull T command, @NotNull MapModel mapModel);
    
    /**
     * Result of an action execution.
     */
    class ActionResult {
        private final boolean success;
        private final String message;
        private final Object result;
        private final String actionType;
        
        private ActionResult(boolean success, String message, Object result, String actionType) {
            this.success = success;
            this.message = message;
            this.result = result;
            this.actionType = actionType;
        }
        
        @NotNull
        public static ActionResult success(@NotNull String actionType, @NotNull String message) {
            return new ActionResult(true, message, null, actionType);
        }
        
        @NotNull
        public static ActionResult success(@NotNull String actionType, @NotNull String message, @Nullable Object result) {
            return new ActionResult(true, message, result, actionType);
        }
        
        @NotNull
        public static ActionResult failure(@NotNull String actionType, @NotNull String message) {
            return new ActionResult(false, message, null, actionType);
        }
        
        public boolean isSuccess() {
            return success;
        }
        
        @NotNull
        public String getMessage() {
            return message;
        }
        
        @Nullable
        public Object getResult() {
            return result;
        }
        
        @NotNull
        public String getActionType() {
            return actionType;
        }
    }
    
    /**
     * Exception thrown when an action cannot be executed.
     */
    class ActionExecutionException extends Exception {
        public ActionExecutionException(String message) {
            super(message);
        }
        
        public ActionExecutionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
