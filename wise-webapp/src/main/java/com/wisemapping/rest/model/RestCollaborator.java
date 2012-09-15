package com.wisemapping.rest.model;

import com.wisemapping.model.Collaborator;
import com.wisemapping.model.User;
import com.wisemapping.util.TimeUtils;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;
import java.util.Date;

@XmlRootElement(name = "collaborator")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)

public class RestCollaborator {

    @JsonIgnore
    private Collaborator collaborator;

    public RestCollaborator(@NotNull Collaborator collaborator) {

        this.collaborator = collaborator;
    }

    public String getCreationDate() {

        return TimeUtils.toISO8601(collaborator.getCreationDate().getTime());
    }

    public void setCreationDate(Calendar creationDate) {

    }

    public String getEmail() {
        return collaborator.getEmail();
    }

    public void setEmail(String email) {

    }

}
