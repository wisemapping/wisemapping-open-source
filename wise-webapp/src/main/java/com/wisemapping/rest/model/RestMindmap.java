package com.wisemapping.rest.model;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.*;
import com.wisemapping.util.TimeUtils;
import org.codehaus.jackson.annotate.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.*;
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
    private Collaborator collaborator;
    @JsonIgnore
    private Mindmap mindmap;
    @Nullable
    private String properties;

    public RestMindmap() throws WiseMappingException {
        this(new Mindmap(), null);

    }

    public RestMindmap(@NotNull Mindmap mindmap, @Nullable Collaborator collaborator) throws WiseMappingException {
        this.mindmap = mindmap;
        this.collaborator = collaborator;
        if (collaborator != null) {
            final CollaborationProperties collaborationProperties = mindmap.findCollaborationProperties(collaborator, false);
            if (collaborationProperties != null) {
                this.properties = collaborationProperties.getMindmapProperties();
            }
        }
    }


    public String getCreationTime() {
        final Calendar creationTime = mindmap.getCreationTime();
        String result = null;
        if (creationTime != null) {
            result = TimeUtils.toISO8601(creationTime.getTime());
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
        return mindmap.getCreator().getEmail();
    }

    public RestCollaborator getLastModifierUser() {
        final User lastEditor = mindmap.getLastEditor();

        RestCollaborator result = null;
        if (lastEditor != null && mindmap.hasPermissions(collaborator, CollaborationRole.EDITOR)) {
            result = new RestCollaborator(lastEditor);
        }
        return result;
    }

    public String getLastModificationTime() {
        final Calendar date = mindmap.getLastModificationTime();
        String result = null;
        if (date != null) {
            result = TimeUtils.toISO8601(date.getTime());
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

    public void setOwner(String owner) {

    }

    public String getOwner() {
        final User owner = mindmap.getCreator();
        return owner != null ? owner.getEmail() : null;
    }

    public void setCreator(String creatorUser) {
    }


    public void setProperties(@Nullable String properties) {
        this.properties = properties;
    }

    public void setLastModificationTime(final String value) {
    }

    public void setLastModifierUser(String lastModifierUser) {
    }

    @Nullable
    public String getProperties() {
        return properties;
    }

    public boolean getStarred() {
        boolean result = false;
        if (collaborator != null) {
            result = mindmap.isStarred(collaborator);
        }
        return result;
    }

    public void setStarred(boolean value) throws WiseMappingException {
        if (collaborator != null) {
            mindmap.setStarred(collaborator, value);
        }
    }

    @JsonIgnore
    public Mindmap getDelegated() {
        return this.mindmap;
    }
}
