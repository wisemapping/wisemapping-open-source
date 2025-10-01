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

package com.wisemapping.rest.model;


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Mindmap;
import org.jetbrains.annotations.NotNull;

import jakarta.xml.bind.annotation.XmlElement;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestMindmapList {

    private List<RestMindmapInfo> mindmapsInfo;

    public RestMindmapList() {
        this(Collections.emptyList(), null);
    }

    public RestMindmapList(@NotNull List<Mindmap> mindmaps, Collaborator collaborator) {
        this.mindmapsInfo = mindmaps.stream()
                .map(m->new RestMindmapInfo(m, collaborator))
                .collect(Collectors.toList());
    }

    public int getCount() {
        return this.mindmapsInfo.size();
    }

    public void setCount(int count) {

    }

    @XmlElement(name = "map")
    public List<RestMindmapInfo> getMindmapsInfo() {
        return mindmapsInfo;
    }

    public void setMindmapsInfo(List<RestMindmapInfo> mindmapsInfo) {
        this.mindmapsInfo = mindmapsInfo;
    }
}
