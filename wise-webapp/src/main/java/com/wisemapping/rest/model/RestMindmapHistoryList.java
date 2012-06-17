package com.wisemapping.rest.model;


import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "history")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestMindmapHistoryList {

    private List<RestMindmapHistory> changes;

    public RestMindmapHistoryList() {
        changes = new ArrayList<RestMindmapHistory>();
    }

    public int getCount() {
        return this.changes.size();
    }

    public void setCount(int count) {

    }

    @XmlElement(name = "changes")
    public List<RestMindmapHistory> getChanges() {
        return changes;
    }

    public void addHistory(@NotNull RestMindmapHistory history) {
        changes.add(history);
    }
}
