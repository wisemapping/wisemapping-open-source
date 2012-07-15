package com.wisemapping.rest.model;


import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.MindMapHistory;
import com.wisemapping.security.Utils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@XmlRootElement(name = "history")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestMindmapHistory {

    static private SimpleDateFormat sdf;
    private int id;
    private Calendar creation;
    private String creator;

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public RestMindmapHistory(@NotNull MindMapHistory history) {
        this.id = history.getId();
        this.creation = history.getCreationTime();
        this.creator = history.getEditor().getFullName();
    }

    public String getCreationTime() {
        return this.toISO8601(creation.getTime());
    }

    public void setCreationTime() {

    }

    public String getCreator() {
        return creator;
    }

    public void setCreator() {
        // Do nothing ...
    }

    public void setId(int id) {
    }

    private String toISO8601(@NotNull Date date) {
        return sdf.format(date) + "Z";
    }

    public int getId() {
        return id;
    }
}
