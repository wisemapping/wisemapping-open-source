package com.wisemapping.rest.model;


import com.wisemapping.model.CollaborationRole;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "collaborators")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestCollaboration {

    @JsonIgnore
    private String email;

    @JsonIgnore
    private CollaborationRole role;

    public RestCollaboration() {

    }

    public void setRole(@NotNull final String value) {
        if (value == null) {
            throw new IllegalStateException("role can not be null");
        }

        role = CollaborationRole.valueOf(value.toUpperCase());
    }

    public String getRole() {
        return role.toString().toLowerCase();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
