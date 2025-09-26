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

import java.util.List;

/**
 * Command for deleting topics from a mind map.
 * Maps to the frontend DeleteCommand.
 */
public class DeleteCommand extends StompCommand {

    @NotNull
    private List<Integer> topicIds;

    public DeleteCommand() {
        super();
    }

    public DeleteCommand(@NotNull String mindmapId, @NotNull String userId) {
        super(mindmapId, userId);
    }

    @Override
    @NotNull
    public String getActionType() {
        return "DELETE";
    }

    @Override
    public void validate() {
        if (topicIds == null || topicIds.isEmpty()) {
            throw new IllegalArgumentException("Topic IDs cannot be null or empty");
        }
        if (topicIds.contains(null)) {
            throw new IllegalArgumentException("Topic IDs cannot contain null values");
        }
    }

    @NotNull
    @JsonProperty("topicIds")
    public List<Integer> getTopicIds() {
        return topicIds;
    }

    public void setTopicIds(@NotNull List<Integer> topicIds) {
        this.topicIds = topicIds;
    }
}