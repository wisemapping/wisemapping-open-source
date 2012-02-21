package com.wisemapping.rest.model;


import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import org.codehaus.jackson.annotate.*;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY
)
public class RestMindmap {

    @JsonIgnore
    private MindMap mindmap;

    public RestMindmap() {
        this(new MindMap());

    }

    public RestMindmap(@NotNull MindMap mindmap) {
        this.mindmap = mindmap;
    }

    public String getOwner() {
        return mindmap.getOwner().getUsername();
    }

    public Calendar getCreationTime() {
        return mindmap.getCreationTime();
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

    public Date getLastModificationDate() {
        return mindmap.getLastModificationDate();
    }

    public boolean isPublic() {
        return mindmap.isPublic();
    }

    public String getXml() throws IOException {
        return mindmap.getXmlStr();
    }

    public void setXml(@NotNull String xml) throws IOException {

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
        mindmap.setOwner(owner);
    }

    public void setCreator(String creatorUser) {
        mindmap.setCreator(creatorUser);
    }


    public void setProperties(String properties) {
        mindmap.setProperties(properties);
    }

    public void setLastModificationTime(Calendar lastModificationTime) {
        mindmap.setLastModificationTime(lastModificationTime);
    }

    public void setLastModifierUser(String lastModifierUser) {
        mindmap.setLastModifierUser(lastModifierUser);
    }

    public String getProperties() {
        return mindmap.getProperties();
    }

    @JsonIgnore
    public MindMap getDelegated() {
        return this.mindmap;
    }
}
