package com.wisemapping.security.ldap;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.AuthenticationSchema;
import com.wisemapping.model.User;
import com.wisemapping.security.UserDetails;
import com.wisemapping.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import java.util.Calendar;
import java.util.Collection;

public class LdapUserDetailsContextMapper implements UserDetailsContextMapper {

    private UserService userService;
    private String adminUser;
    private String ldapAttributeFirstName;
    private String ldapAttributeLastName;


    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }


    private boolean isAdmin(@Nullable String email) {
        return email != null && adminUser != null && email.trim().endsWith(adminUser);
    }

    public String getAdminUser() {
        return adminUser;
    }

    public void setAdminUser(String adminUser) {
        this.adminUser = adminUser;
    }

    @Override
    public UserDetails mapUserFromContext(@NotNull DirContextOperations userData,
                                          String email, Collection<? extends GrantedAuthority> arg2) {
        User user = userService.getUserBy(email);
        if (user == null) {
            // If the user was not found in the database, create a new one ...
            user = new User();
            user.setEmail(email);

            final String firstName = userData.getStringAttribute(ldapAttributeFirstName);
            user.setFirstname(firstName);

            final String lastName = userData.getStringAttribute(ldapAttributeLastName);
            user.setLastname(lastName);

            user.setPassword(email);
            final Calendar now = Calendar.getInstance();
            user.setActivationDate(now);

            try {
                user.setAuthenticationSchema(AuthenticationSchema.LDAP);
                user = userService.createUser(user, false, false);
            } catch (WiseMappingException e) {
                throw new IllegalStateException(e);
            }
        }
        return new UserDetails(user, isAdmin(email));
    }

    public String getLdapAttributeLastName() {
        return ldapAttributeLastName;
    }

    public void setLdapAttributeLastName(String ldapAttributLastName) {
        this.ldapAttributeLastName = ldapAttributLastName;
    }

    public String getLdapAttrbutFirstName() {
        return ldapAttributeFirstName;
    }

    public void setLdapAttributeFirstName(String ldapAttributeFirstName) {
        this.ldapAttributeFirstName = ldapAttributeFirstName;
    }

    @Override
    public void mapUserToContext(org.springframework.security.core.userdetails.UserDetails userDetails, DirContextAdapter dirContextAdapter) {
        // To be implemented ...
    }


}
