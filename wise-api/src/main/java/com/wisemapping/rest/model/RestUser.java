/*
 *    Copyright [2022] [wisemapping]
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


import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Account;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;


@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestUser {

    private final Account user;
    private String password;

    public RestUser() {
        this(new Account());
    }

    public RestUser(@NotNull Account user) {
        this.user = user;
    }

    public Calendar getCreationDate() {
        return user.getCreationDate();
    }

    public String getLocale() {
        return user.getLocale();
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

    public int getId() {
        return user.getId();
    }

    public void setId(int id) {
        user.setId(id);
    }

    public String getEmail() {
        return user.getEmail();
    }

    public void setEmail(String email) {
        user.setEmail(email);
    }

    public void setPassword(final String password) {
        this.user.setPassword(password);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public Account getDelegated() {
        return this.user;
    }

	public AuthenticationType getAuthenticationType() {
		return user.getAuthenticationType();
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
