package com.wisemapping.rest.model;


import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import org.codehaus.jackson.annotate.*;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestMindmap {

    @JsonIgnore
    private MindMap mindmap;
    @JsonIgnore
    static private SimpleDateFormat sdf;

    static {
        sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public RestMindmap() {
        this(new MindMap());

    }

    public RestMindmap(@NotNull MindMap mindmap) {
        this.mindmap = mindmap;
    }

    public String getCreationTime() {
        final Calendar creationTime = mindmap.getCreationTime();
        String result = null;
        if (creationTime != null) {
            result = this.toISO8601(creationTime.getTime());
        }
        return result;
    }


    public String getDescription() {
        return mindmap.getDescription();
    }

    public String getTags() {
        return mindmap.getTags();
    }

    public String getTitle() {
        return mindmap.getTitle();
    }

    public int getId() {
        return mindmap.getId();
    }

    public String getCreator() {
        return mindmap.getCreator();
    }

    public String getLastModifierUser() {
        return mindmap.getLastModifierUser();
    }

    public String getLastModificationTime() {
        final Calendar date = mindmap.getLastModificationTime();
        String result = null;
        if (date != null) {
            result = toISO8601(date.getTime());
        }
        return result;
    }

    public boolean isPublic() {
        return mindmap.isPublic();
    }


    public void setPublic(boolean value) {
        // return mindmap.isPublic();
    }

    public String getXml() throws IOException {
        return mindmap.getXmlStr();
    }

    public void setXml(@Nullable String xml) throws IOException {

        if (xml != null)
            mindmap.setXmlStr(xml);
    }

    public void setId(int id) {
        mindmap.setId(id);
    }

    public void setTitle(String title) {
        mindmap.setTitle(title);
    }

    public void setTags(String tags) {
        mindmap.setTags(tags);
    }

    public void setDescription(String description) {
        mindmap.setDescription(description);
    }

    public void setOwner(User owner) {

    }

    public void setCreator(String creatorUser) {
    }


    public void setProperties(String properties) {
        mindmap.setProperties(properties);
    }

    public void setLastModificationTime(final String value) {
    }

    public void setLastModifierUser(String lastModifierUser) {
    }

    public String getProperties() {
        return mindmap.getProperties();
    }

    @JsonIgnore
    public MindMap getDelegated() {
        return this.mindmap;
    }

    private String toISO8601(@Nullable Date date) {
        String result = "";
        if (date != null) {
            result = sdf.format(date) + "Z";
        }
        return result;
    }
}
