package com.wisemapping.rest.model;


import com.wisemapping.model.MindMap;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.CollectionUtils;

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

    private List<RestMindmap> mindmaps;

    public RestMindmapList() {
        this(Collections.<MindMap>emptyList());
    }

    public RestMindmapList(@NotNull List<MindMap> mindmaps) {
        this.mindmaps = new ArrayList<RestMindmap>();
        for (MindMap mindMap : mindmaps) {
            this.mindmaps.add(new RestMindmap(mindMap));
        }
    }

    public int getCount() {
        return this.mindmaps.size();
    }

    public void setCount(int count) {

    }

    @XmlElement(name = "map")
    public List<RestMindmap> getMindmaps() {
        return mindmaps;
    }

    public void setMindmaps(List<RestMindmap> mindmaps) {
        this.mindmaps = mindmaps;
    }
}
