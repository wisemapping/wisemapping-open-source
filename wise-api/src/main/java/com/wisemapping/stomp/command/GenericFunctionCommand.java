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

package com.wisemapping.stomp.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * Command for generic function calls that don't fit into specific command types.
 * Maps to the frontend GenericFunctionCommand.
 */
public class GenericFunctionCommand extends StompCommand {

    @NotNull
    private String functionName;

    @Nullable
    private List<Object> parameters;

    @Nullable
    private Map<String, Object> namedParameters;

    @Nullable
    private Object result;

    public GenericFunctionCommand() {
        super();
    }

    public GenericFunctionCommand(@NotNull String mindmapId, @NotNull String userId, @NotNull String functionName) {
        super(mindmapId, userId);
        this.functionName = functionName;
    }

    @Override
    @NotNull
    public String getActionType() {
        return "GENERIC_FUNCTION";
    }

    @Override
    public void validate() {
        if (functionName == null || functionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
    }

    @NotNull
    @JsonProperty("functionName")
    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(@NotNull String functionName) {
        this.functionName = functionName;
    }

    @Nullable
    @JsonProperty("parameters")
    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(@Nullable List<Object> parameters) {
        this.parameters = parameters;
    }

    @Nullable
    @JsonProperty("namedParameters")
    public Map<String, Object> getNamedParameters() {
        return namedParameters;
    }

    public void setNamedParameters(@Nullable Map<String, Object> namedParameters) {
        this.namedParameters = namedParameters;
    }

    @Nullable
    @JsonProperty("result")
    public Object getResult() {
        return result;
    }

    public void setResult(@Nullable Object result) {
        this.result = result;
    }
}
