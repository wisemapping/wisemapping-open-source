/*
*    Copyright [2015] [wisemapping]
*
*   Licensed under WiseMapping Public License, Version 1.0 (the "License").
*   It is basically the Apache License, Version 2.0 (the "License") plus the
*   "powered by wisemapping" text requirement on every single page;
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the license at
*
*       http://www.wisemapping.org/license
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*/

package com.wisemapping.rest.model;


import com.wisemapping.model.User;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Calendar;
import java.util.Set;



@XmlRootElement(name = "user")
@XmlAccessorType(XmlAccessType.PROPERTY)
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RestUser {

    private User user;
    private String password;

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

//    public boolean isActive() {
//        return user.isActive();
//    }

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

    public void setPassword(final String password) {
        this.user.setPassword(password);
        this.password = password;
    }

    public String getPassword() {
        return this.password;
    }

    @JsonIgnore
    public User getDelegated() {
        return this.user;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RestUser)) {
            return false;
        }

        RestUser restUser = (RestUser) o;
        return this.getDelegated().identityEquality(restUser.getDelegated());
    }

    @Override
    public int hashCode() {
        return this.getDelegated().hashCode();
    }


}
