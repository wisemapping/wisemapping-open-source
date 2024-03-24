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

package com.wisemapping.rest.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.wisemapping.model.MindmapLabel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.PUBLIC_ONLY;

@JsonAutoDetect(
        fieldVisibility = NONE,
        setterVisibility = PUBLIC_ONLY,
        isGetterVisibility = NONE,
        getterVisibility = PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestLabel {

    @JsonIgnore
    private final MindmapLabel label;

    public RestLabel() {
        this(new MindmapLabel());
    }

    public RestLabel(@NotNull final MindmapLabel label) {
        this.label = label;
    }

    public void setParent(final MindmapLabel parent) {
        this.label.setParent(parent);
    }

    @Nullable
    public MindmapLabel getParent() {
        return this.label.getParent();
    }

    @Nullable
    public String getTitle() {
        return this.label.getTitle();
    }

    public int getId() {
        return label.getId();
    }

    public void setId(int id) {
        label.setId(id);
    }

    public void setTitle(String title) {
        label.setTitle(title);
    }

    public void setColor(final String color) {
        label.setColor(color);
    }

    @Nullable public String getColor() {
        return label.getColor();
    }

    @JsonIgnore
    public MindmapLabel getDelegated() {
        return label;
    }
}
