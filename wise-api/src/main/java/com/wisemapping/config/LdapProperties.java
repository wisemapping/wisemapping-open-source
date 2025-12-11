package com.wisemapping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
@Configuration
@ConfigurationProperties(prefix = "app.ldap")
public class LdapProperties {

    /**
     * Enable or disable LDAP authentication.
     * When disabled, only DATABASE authentication is available.
     */
    private boolean enabled = false;

    /**
     * LDAP server URL including protocol and port.
     * Examples: ldap://localhost:389, ldaps://ldap.example.com:636
     */
    private String url = "ldap://localhost:389";

    /**
     * Base Distinguished Name (DN) for LDAP searches.
     * This is the root of your LDAP directory tree.
     */
    private String baseDn = "dc=example,dc=com";

    /**
     * Pattern for constructing user DN from username.
     * {0} is replaced with the username during authentication.
     * Used for direct bind authentication.
     */
    private String userDnPatterns = "uid={0},ou=users";

    /**
     * Base DN for user searches (relative to base-dn).
     * Used when user-search-filter is specified.
     */
    private String userSearchBase = "ou=users";

    /**
     * LDAP filter for searching users.
     * {0} is replaced with the username.
     * Common filters: (uid={0}), (sAMAccountName={0}), (mail={0})
     */
    private String userSearchFilter = "(uid={0})";

    /**
     * Base DN for group searches (relative to base-dn).
     */
    private String groupSearchBase = "ou=groups";

    /**
     * LDAP filter for searching groups.
     * {0} is replaced with the user's DN.
     */
    private String groupSearchFilter = "(member={0})";

    /**
     * DN of the manager/admin account for binding to LDAP.
     * Required if anonymous binds are not allowed.
     * Leave empty for anonymous binding.
     */
    private String managerDn = "";

    /**
     * Password for the manager/admin account.
     */
    private String managerPassword = "";

    /**
     * LDAP attribute containing the user's password.
     * Used for password comparison authentication.
     */
    private String passwordAttribute = "userPassword";

    /**
     * LDAP attribute containing the user's email address.
     * This is used to identify the user in WiseMapping.
     */
    private String emailAttribute = "mail";

    /**
     * LDAP attribute containing the user's first name.
     */
    private String firstnameAttribute = "givenName";

    /**
     * LDAP attribute containing the user's last name.
     */
    private String lastnameAttribute = "sn";

    /**
     * Use password comparison instead of bind authentication.
     * Set to true if you want to compare passwords locally.
     * Set to false (default) for bind authentication.
     */
    private boolean passwordCompare = false;

    /**
     * Pooled connection - reuse LDAP connections.
     * Improves performance for high-traffic scenarios.
     */
    private boolean pooled = true;

    /**
     * Connection timeout in milliseconds.
     */
    private int connectTimeout = 5000;

    /**
     * Read timeout in milliseconds.
     */
    private int readTimeout = 10000;

    // --- Getters and Setters ---

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getBaseDn() {
        return baseDn;
    }

    public void setBaseDn(String baseDn) {
        this.baseDn = baseDn;
    }

    public String getUserDnPatterns() {
        return userDnPatterns;
    }

    public void setUserDnPatterns(String userDnPatterns) {
        this.userDnPatterns = userDnPatterns;
    }

    public String getUserSearchBase() {
        return userSearchBase;
    }

    public void setUserSearchBase(String userSearchBase) {
        this.userSearchBase = userSearchBase;
    }

    public String getUserSearchFilter() {
        return userSearchFilter;
    }

    public void setUserSearchFilter(String userSearchFilter) {
        this.userSearchFilter = userSearchFilter;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public void setGroupSearchBase(String groupSearchBase) {
        this.groupSearchBase = groupSearchBase;
    }

    public String getGroupSearchFilter() {
        return groupSearchFilter;
    }

    public void setGroupSearchFilter(String groupSearchFilter) {
        this.groupSearchFilter = groupSearchFilter;
    }

    public String getManagerDn() {
        return managerDn;
    }

    public void setManagerDn(String managerDn) {
        this.managerDn = managerDn;
    }

    public String getManagerPassword() {
        return managerPassword;
    }

    public void setManagerPassword(String managerPassword) {
        this.managerPassword = managerPassword;
    }

    public String getPasswordAttribute() {
        return passwordAttribute;
    }

    public void setPasswordAttribute(String passwordAttribute) {
        this.passwordAttribute = passwordAttribute;
    }

    public String getEmailAttribute() {
        return emailAttribute;
    }

    public void setEmailAttribute(String emailAttribute) {
        this.emailAttribute = emailAttribute;
    }

    public String getFirstnameAttribute() {
        return firstnameAttribute;
    }

    public void setFirstnameAttribute(String firstnameAttribute) {
        this.firstnameAttribute = firstnameAttribute;
    }

    public String getLastnameAttribute() {
        return lastnameAttribute;
    }

    public void setLastnameAttribute(String lastnameAttribute) {
        this.lastnameAttribute = lastnameAttribute;
    }

    public boolean isPasswordCompare() {
        return passwordCompare;
    }

    public void setPasswordCompare(boolean passwordCompare) {
        this.passwordCompare = passwordCompare;
    }

    public boolean isPooled() {
        return pooled;
    }

    public void setPooled(boolean pooled) {
        this.pooled = pooled;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    /**
     * Constructs the full LDAP URL with base DN.
     * Used by Spring LDAP context source.
     */
    public String getFullUrl() {
        return url + "/" + baseDn;
    }

    /**
     * Check if manager credentials are configured.
     */
    public boolean hasManagerCredentials() {
        return managerDn != null && !managerDn.isEmpty()
                && managerPassword != null && !managerPassword.isEmpty();
    }

    @Override
    public String toString() {
        return "LdapProperties{" +
                "enabled=" + enabled +
                ", url='" + url + '\'' +
                ", baseDn='" + baseDn + '\'' +
                ", userDnPatterns='" + userDnPatterns + '\'' +
                ", userSearchBase='" + userSearchBase + '\'' +
                ", userSearchFilter='" + userSearchFilter + '\'' +
                ", groupSearchBase='" + groupSearchBase + '\'' +
                ", managerDn='" + (managerDn != null && !managerDn.isEmpty() ? "[SET]" : "[NOT SET]") + '\'' +
                ", emailAttribute='" + emailAttribute + '\'' +
                '}';
    }
}