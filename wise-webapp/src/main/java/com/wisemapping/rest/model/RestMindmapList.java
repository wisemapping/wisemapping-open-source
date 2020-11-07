/*
 *    Copyright [2015] [wisemapping]
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


import com.wisemapping.model.Collaborator;
import com.wisemapping.model.Mindmap;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

@XmlRootElement(name = "maps")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestMindmapList {

    private List<RestMindmapInfo> mindmapsInfo;

    public RestMindmapList() {
        this(Collections.<Mindmap>emptyList(), null);
    }

    public RestMindmapList(@NotNull List<Mindmap> mindmaps, @NotNull Collaborator collaborator) {
        this.mindmapsInfo = new ArrayList<>(mindmaps.size());
        for (Mindmap mindMap : mindmaps) {
            this.mindmapsInfo.add(new RestMindmapInfo(mindMap, collaborator));
        }
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
