package com.wisemapping.rest.model;


import com.wisemapping.model.Collaboration;
import com.wisemapping.model.CollaborationRole;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "collaboration")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestCollaboration {

    private String email;
    private String role;

    public RestCollaboration(@NotNull Collaboration collaboration) {
        this.email = collaboration.getCollaborator().getEmail();
        this.role = collaboration.getRole().getLabel();
    }

    public RestCollaboration() {

    }

    public void setRole(@NotNull final String value) {
        if (value == null) {
            throw new IllegalStateException("role can not be null");
        }
        // Only check ...
        CollaborationRole.valueOf(value.toUpperCase());
        role = value;

    }

    public String getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(@NotNull String email) {
        this.email = email;
    }
}
