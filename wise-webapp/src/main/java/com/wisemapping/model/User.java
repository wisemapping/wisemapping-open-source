/*
*    Copyright [2012] [wisemapping]
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

package com.wisemapping.model;

import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class User
        extends Collaborator
        implements Serializable {

    private String firstname;
    private String lastname;
    private String password;
    private long activationCode;
    private Calendar activationDate;
    private Set<String> tags = new HashSet<String>();
    private boolean allowSendEmail = false;
    private String locale;


    public User() {
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<String> getTags() {
        return tags;
    }

    public String getFullName() {
        return this.getFirstname() + " " + this.getLastname();
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isActive() {
        return activationDate != null;
    }

    public void setActivationCode(long code) {
        this.activationCode = code;
    }

    public long getActivationCode() {
        return activationCode;
    }

    public void setActivationDate(Calendar date) {
        this.activationDate = date;
    }

    public Calendar getActivationDate() {
        return activationDate;
    }

    public boolean isAllowSendEmail() {
        return allowSendEmail;
    }

    public void setAllowSendEmail(boolean allowSendEmail) {
        this.allowSendEmail = allowSendEmail;
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || !User.class.getClass().isInstance(o))
            return false;

        final User user = (User) o;

        final String email = getEmail();
        if (email != null ? !email.equals(user.getEmail()) : user.getEmail() != null) return false;
        if (firstname != null ? !firstname.equals(user.firstname) : user.firstname != null) return false;
        return !(lastname != null ? !lastname.equals(user.lastname) : user.lastname != null);
    }


    public int hashCode() {
        int result;
        result = (firstname != null ? firstname.hashCode() : 0);
        result = 29 * result + (lastname != null ? lastname.hashCode() : 0);
        result = 29 * result + (password != null ? password.hashCode() : 0);
        result = 29 * result + (getEmail() != null ? getEmail().hashCode() : 0);
        return result;
    }

    @Nullable
    public String getLocale() {
        return locale;
    }

    public void setLocale(@Nullable String locale) {
        this.locale = locale;
    }
}
