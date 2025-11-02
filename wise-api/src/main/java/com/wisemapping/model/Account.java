/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package com.wisemapping.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Calendar;

@Entity
@Table(name = "ACCOUNT")
@PrimaryKeyJoinColumn(name = "collaborator_id")
@NamedQueries({
    @NamedQuery(
        name = "Account.getUserByActivationCode",
        query = "SELECT a FROM Account a WHERE a.activationCode = :activationCode"
    ),
    @NamedQuery(
        name = "Account.getAllUsers",
        query = "SELECT a FROM Account a ORDER BY a.id DESC"
    ),
    @NamedQuery(
        name = "Account.countAllUsers",
        query = "SELECT COUNT(a) FROM Account a"
    ),
    @NamedQuery(
        name = "Account.findSuspendedUsers",
        query = "SELECT a FROM Account a WHERE a.suspended = true ORDER BY a.id"
    ),
    @NamedQuery(
        name = "Account.findUsersSuspendedForInactivity",
        query = "SELECT a FROM Account a WHERE a.suspended = true AND a.suspensionReasonCode = :inactivityCode ORDER BY a.id"
    ),
    @NamedQuery(
        name = "Account.searchUsers",
        query = "SELECT a FROM Account a WHERE LOWER(a.email) LIKE LOWER(:search) OR LOWER(a.firstname) LIKE LOWER(:search) OR LOWER(a.lastname) LIKE LOWER(:search) ORDER BY a.id DESC"
    ),
    @NamedQuery(
        name = "Account.countUsersBySearch",
        query = "SELECT COUNT(a) FROM Account a WHERE LOWER(a.email) LIKE LOWER(:search) OR LOWER(a.firstname) LIKE LOWER(:search) OR LOWER(a.lastname) LIKE LOWER(:search)"
    )
})
public class Account
        extends Collaborator
        implements Serializable {

    public static final int MIN_PASSWORD_LENGTH_SIZE = 8;
    public static final int MAX_PASSWORD_LENGTH_SIZE = 40;

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

    @Column(name = "oauth_sync")
    private Boolean oauthSync;

    @Column(name = "sync_code")
    private String syncCode;

    @Column(name = "oauth_token", columnDefinition = "TEXT")
    private String oauthToken;

    @Column(name = "suspended")
    private boolean suspended = false;

    @Column(name = "suspended_date")
    private Calendar suspendedDate;

    @Column(name = "suspension_reason")
    private Character suspensionReasonCode;


    public Account() {
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

    public void setPassword(@jakarta.validation.constraints.NotNull String password) {
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

    /**
     * Check if password changes are allowed for this user.
     * Password changes are only allowed for DATABASE authentication type users.
     * External providers (Google, LDAP, Facebook) manage their own passwords.
     */
    public boolean isPasswordChangeAllowed() {
        return this.getAuthenticationType() == AuthenticationType.DATABASE;
    }

    public String getAuthenticatorUri() {
        return authenticatorUri;
    }

    public void setAuthenticatorUri(String authenticatorUri) {
        this.authenticatorUri = authenticatorUri;
    }

    public void setAuthenticationTypeCode(Character authenticationTypeCode) {
        this.authenticationTypeCode = authenticationTypeCode;
    }

    public Boolean getOauthSync() {
        return oauthSync != null && oauthSync;
    }

    public void setOauthSync(Boolean oauthSync) {
        this.oauthSync = oauthSync;
    }

    @Nullable
    public String getSyncCode() {
        return syncCode;
    }

    public void setSyncCode(@Nullable String syncCode) {
        this.syncCode = syncCode;
    }

    public String getOauthToken() {
        return oauthToken;
    }

    public void setOauthToken(String oauthToken) {
        this.oauthToken = oauthToken;
    }

    public boolean isSuspended() {
        return suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public void suspend() {
        this.suspended = true;
    }

    public void suspend(SuspensionReason reason) {
        this.suspended = true;
        this.suspensionReasonCode = reason != null ? reason.getCode().charAt(0) : null;
    }

    @PrePersist
    @PreUpdate
    private void updateSuspendedDate() {
        if (suspended && suspendedDate == null) {
            suspendedDate = Calendar.getInstance();
        } else if (!suspended) {
            suspendedDate = null;
        }
    }

    public void unsuspend() {
        this.suspended = false;
        this.suspensionReasonCode = null;
    }

    public Calendar getSuspendedDate() {
        return suspendedDate;
    }

    public void setSuspendedDate(Calendar suspendedDate) {
        this.suspendedDate = suspendedDate;
    }

    public SuspensionReason getSuspensionReason() {
        return suspensionReasonCode != null ? SuspensionReason.fromCode(suspensionReasonCode.toString()) : null;
    }

    public void setSuspensionReason(SuspensionReason reason) {
        this.suspensionReasonCode = reason != null ? reason.getCode().charAt(0) : null;
    }


    @Override
    public String toString() {
        return "User{" +
                "firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                "', email = '" + this.getEmail() + "}";
    }
}
