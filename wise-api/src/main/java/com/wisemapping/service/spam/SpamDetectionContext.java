/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.service.spam;

import com.wisemapping.mindmap.model.MapModel;
import com.wisemapping.model.Mindmap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context object that holds both the Mindmap entity and its parsed model.
 * This allows spam detection strategies to work with the parsed model without
 * re-parsing the XML multiple times.
 */
public class SpamDetectionContext {
    
    @NotNull
    private final Mindmap mindmap;
    
    @NotNull
    private final MapModel mapModel;
    
    public SpamDetectionContext(@NotNull Mindmap mindmap, @NotNull MapModel mapModel) {
        this.mindmap = mindmap;
        this.mapModel = mapModel;
    }
    
    /**
     * Gets the Mindmap entity (for metadata like creator, title, description).
     * 
     * @return The Mindmap entity
     */
    @NotNull
    public Mindmap getMindmap() {
        return mindmap;
    }
    
    /**
     * Gets the parsed MapModel (for content analysis like topics, notes, links).
     * 
     * @return The parsed MapModel
     */
    @NotNull
    public MapModel getMapModel() {
        return mapModel;
    }
    
    /**
     * Gets the mindmap title (from entity, fallback to model).
     * 
     * @return The title
     */
    @Nullable
    public String getTitle() {
        if (mindmap.getTitle() != null && !mindmap.getTitle().trim().isEmpty()) {
            return mindmap.getTitle();
        }
        return mapModel.getTitle();
    }
    
    /**
     * Gets the mindmap description (from entity, fallback to model).
     * 
     * @return The description
     */
    @Nullable
    public String getDescription() {
        if (mindmap.getDescription() != null && !mindmap.getDescription().trim().isEmpty()) {
            return mindmap.getDescription();
        }
        return mapModel.getDescription();
    }
}

