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

package com.wisemapping.model;

import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USER")
@PrimaryKeyJoinColumn(name = "colaborator_id")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class User
        extends Collaborator
        implements Serializable {

    private String firstname;
    private String lastname;
    private String password;
    private String locale;

    @Column(name = "activation_code")
    private long activationCode;

    @Column(name = "activation_date")
    private Calendar activationDate;

    @Column(name = "allow_send_email")
    private boolean allowSendEmail = false;

    @Column(name = "authentication_type")
    private Character authenticationTypeCode = AuthenticationType.DATABASE.getCode();

    @Column(name = "authenticator_uri")
    private String authenticatorUri;

    public User() {
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

    @Nullable
    public String getLocale() {
        return locale;
    }

    public void setLocale(@Nullable String locale) {
        this.locale = locale;
    }

    public char getAuthenticationTypeCode() {
        return this.authenticationTypeCode;
    }

    public void setAuthenticationTypeCode(char code) {
        this.authenticationTypeCode = code;
    }

    public AuthenticationType getAuthenticationType() {
        return authenticationTypeCode != null ? AuthenticationType.valueOf(authenticationTypeCode) : AuthenticationType.DATABASE;
    }

    public void setAuthenticationType(@NotNull AuthenticationType authenticationType) {
        this.authenticationTypeCode = authenticationType.getCode();
    }

    public boolean isDatabaseSchema() {
        return this.getAuthenticationType() == AuthenticationType.DATABASE;
    }

    public String getAuthenticatorUri() {
        return authenticatorUri;
    }

    public void setAuthenticatorUri(String authenticatorUri) {
        this.authenticatorUri = authenticatorUri;
    }

    @Override
    public String toString() {
        return "User{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                "', email = '" + this.getEmail() + "}";
    }
}
