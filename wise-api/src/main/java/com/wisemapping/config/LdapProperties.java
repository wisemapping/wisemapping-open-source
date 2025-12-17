package com.wisemapping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * LDAP configuration properties combining Spring Boot standard properties
 * with WiseMapping-specific extensions.
 * 
 * Standard Spring Boot properties (spring.ldap.*):
 * - urls: LDAP server URL(s)
 * - base: Base DN for LDAP operations
 * - username: Manager/bind DN
 * - password: Manager/bind password
 * 
 * Custom extensions (app.ldap.*):
 * - enabled: Enable/disable LDAP authentication
 * - User search configuration
 * - Group search configuration
 * - Attribute mappings
 * - Connection settings
 */
@Configuration
@ConfigurationProperties(prefix = "spring.ldap")
public class LdapProperties {

    // ===== Spring Boot Standard Properties (spring.ldap.*) =====

    /**
     * LDAP server URL(s) including protocol and port.
     * Examples: ldap://localhost:389, ldaps://ldap.example.com:636
     * Spring Boot standard property: spring.ldap.urls
     */
    private String[] urls = new String[] { "ldap://localhost:389" };

    /**
     * Base Distinguished Name (DN) for LDAP searches.
     * This is the root of your LDAP directory tree.
     * Spring Boot standard property: spring.ldap.base
     */
    private String base = "dc=example,dc=com";

    /**
     * DN of the manager/admin account for binding to LDAP.
     * Required if anonymous binds are not allowed.
     * Leave empty for anonymous binding.
     * Spring Boot standard property: spring.ldap.username
     */
    private String username = "";

    /**
     * Password for the manager/admin account.
     * Spring Boot standard property: spring.ldap.password
     */
    private String password = "";

    // ===== Custom Extensions (app.ldap.*) =====

    /**
     * Enable or disable LDAP authentication.
     * When disabled, only DATABASE authentication is available.
     */
    private boolean enabled = false;

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

    // --- Getters and Setters for Spring Boot Standard Properties ---

    public String[] getUrls() {
        return urls;
    }

    public void setUrls(String[] urls) {
        this.urls = urls;
    }

    /**
     * Get the primary LDAP URL.
     * For backward compatibility with code expecting a single URL.
     */
    public String getUrl() {
        return urls != null && urls.length > 0 ? urls[0] : "ldap://localhost:389";
    }

    /**
     * Set a single LDAP URL.
     * For backward compatibility with code setting a single URL.
     */
    public void setUrl(String url) {
        this.urls = new String[] { url };
    }

    public String getBase() {
        return base;
    }

    public void setBase(String base) {
        this.base = base;
    }

    /**
     * Get base DN.
     * Alias for getBase() for backward compatibility.
     */
    public String getBaseDn() {
        return base;
    }

    /**
     * Set base DN.
     * Alias for setBase() for backward compatibility.
     */
    public void setBaseDn(String baseDn) {
        this.base = baseDn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Get manager DN.
     * Alias for getUsername() for backward compatibility.
     */
    public String getManagerDn() {
        return username;
    }

    /**
     * Set manager DN.
     * Alias for setUsername() for backward compatibility.
     */
    public void setManagerDn(String managerDn) {
        this.username = managerDn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get manager password.
     * Alias for getPassword() for backward compatibility.
     */
    public String getManagerPassword() {
        return password;
    }

    /**
     * Set manager password.
     * Alias for setPassword() for backward compatibility.
     */
    public void setManagerPassword(String managerPassword) {
        this.password = managerPassword;
    }

    // --- Getters and Setters for Custom Extensions ---

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
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
        return getUrl() + "/" + base;
    }

    /**
     * Check if manager credentials are configured.
     */
    public boolean hasManagerCredentials() {
        return username != null && !username.isEmpty()
                && password != null && !password.isEmpty();
    }

    @Override
    public String toString() {
        return "LdapProperties{" +
                "enabled=" + enabled +
                ", urls=" + java.util.Arrays.toString(urls) +
                ", base='" + base + '\'' +
                ", userDnPatterns='" + userDnPatterns + '\'' +
                ", userSearchBase='" + userSearchBase + '\'' +
                ", userSearchFilter='" + userSearchFilter + '\'' +
                ", groupSearchBase='" + groupSearchBase + '\'' +
                ", username='" + (username != null && !username.isEmpty() ? "[SET]" : "[NOT SET]") + '\'' +
                ", emailAttribute='" + emailAttribute + '\'' +
                '}';
    }
}