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

package com.wisemapping.rest.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.wisemapping.model.Account;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Lightweight DTO for admin console user listing.
 * Optimized to avoid N+1 queries and expensive operations.
 */
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY,
        isGetterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminRestUser {
    
    private int id;
    private String email;
    private String firstname;
    private String lastname;
    private String fullName;
    private String locale;
    private boolean isActive;
    private boolean suspended;
    private String suspensionReason;
    private String suspendedDate;
    private String authenticationType;
    private String creationDate;
    private boolean allowSendEmail;
    private boolean isAdmin;

    // Default constructor for Jackson deserialization
    public AdminRestUser() {
    }

    /**
     * Constructor that safely accesses eagerly loaded entities to avoid N+1 queries.
     * Assumes the Account has been loaded with proper JOIN FETCH clauses.
     */
    public AdminRestUser(Account account, boolean isAdmin) {
        this.id = account.getId();
        this.email = account.getEmail();
        this.firstname = account.getFirstname();
        this.lastname = account.getLastname();
        this.fullName = account.getFullName();
        this.locale = account.getLocale();
        this.isActive = account.isActive();
        this.suspended = account.isSuspended();
        this.allowSendEmail = account.isAllowSendEmail();
        this.isAdmin = isAdmin;
        
        // Authentication type
        if (account.getAuthenticationType() != null) {
            this.authenticationType = account.getAuthenticationType().name();
        }
        
        // Suspension information
        if (account.getSuspensionReason() != null) {
            this.suspensionReason = account.getSuspensionReason().name();
        }
        
        if (account.getSuspendedDate() != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                account.getSuspendedDate().toInstant(), 
                ZoneId.systemDefault());
            this.suspendedDate = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
        
        // Creation date
        if (account.getCreationDate() != null) {
            LocalDateTime dateTime = LocalDateTime.ofInstant(
                account.getCreationDate().toInstant(), 
                ZoneId.systemDefault());
            this.creationDate = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getFullName() {
        return fullName;
    }

    public String getLocale() {
        return locale;
    }

    @JsonProperty("isActive")
    public boolean isActive() {
        return isActive;
    }

    @JsonProperty("isSuspended")
    public boolean isSuspended() {
        return suspended;
    }

    public String getSuspensionReason() {
        return suspensionReason;
    }

    public String getSuspendedDate() {
        return suspendedDate;
    }

    public String getAuthenticationType() {
        return authenticationType;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public boolean isAllowSendEmail() {
        return allowSendEmail;
    }

    @JsonProperty("isAdmin")
    public boolean isAdmin() {
        return isAdmin;
    }

    // Setters for testing and updates
    public void setId(int id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public void setSuspensionReason(String suspensionReason) {
        this.suspensionReason = suspensionReason;
    }

    public void setSuspendedDate(String suspendedDate) {
        this.suspendedDate = suspendedDate;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public void setAllowSendEmail(boolean allowSendEmail) {
        this.allowSendEmail = allowSendEmail;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
