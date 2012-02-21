package com.wisemapping.rest.model;


import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Set;

@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class RestUser {

    private User user;

    public RestUser() {
        this(new User());
    }

    public RestUser(@NotNull User user) {
        this.user = user;
    }

    public Calendar getCreationDate() {
        return user.getCreationDate();
    }

    public void setTags(Set<String> tags) {
        user.setTags(tags);
    }

    public Set<String> getTags() {
        return user.getTags();
    }

    public String getFirstname() {
        return user.getFirstname();
    }

    public void setFirstname(String firstname) {
        user.setFirstname(firstname);
    }

    public String getLastname() {
        return user.getLastname();
    }

    public void setLastname(String lastname) {
        user.setLastname(lastname);
    }

    public boolean isActive() {
        return user.isActive();
    }

    public String getUsername() {
        return user.getUsername();
    }

    public void setUsername(String username) {
        user.setUsername(username);
    }

    public long getId() {
        return user.getId();
    }

    public void setId(long id) {
        user.setId(id);
    }

    public String getEmail() {
        return user.getEmail();
    }

    public void setEmail(String email) {
        user.setEmail(email);
    }

    public void setCreationDate(Calendar creationDate) {
//        user.setCreationDate(creationDate);
    }

    public void setPassword(@NotNull final String password){
        this.user.setPassword(password);
    }

    @JsonIgnore
    public User getDelegated(){
        return this.user;
    }
}
