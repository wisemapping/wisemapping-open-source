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

package com.wisemapping.service;

import com.wisemapping.dao.UserManager;
import com.wisemapping.exceptions.InvalidMindmapException;
import com.wisemapping.exceptions.OAuthAuthenticationException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.*;
import com.wisemapping.rest.model.RestResetPasswordAction;
import com.wisemapping.rest.model.RestResetPasswordResponse;
import com.wisemapping.service.google.GoogleAccountBasicData;
import com.wisemapping.service.google.GoogleService;
import com.wisemapping.service.google.http.HttpInvokerException;
import com.wisemapping.service.facebook.FacebookAccountBasicData;
import com.wisemapping.service.facebook.FacebookService;
import com.wisemapping.util.VelocityEngineUtils;
import com.wisemapping.util.VelocityEngineWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service("userService")
@Transactional(propagation = Propagation.REQUIRED)
public class UserServiceImpl
        implements UserService {


    @Autowired
    private UserManager userManager;
    @Autowired
    private MindmapService mindmapService;
    @Autowired
    private NotificationService notificationService;
    @Autowired
    private MessageSource messageSource;
    @Autowired
    private VelocityEngineWrapper velocityEngineWrapper;
    @Autowired
    private GoogleService googleService;
    @Autowired
    private FacebookService facebookService;
    @Autowired
    private MetricsService metricsService;

    final private static Logger logger = LogManager.getLogger();

    @Override
    public void activateAccount(long code)
            throws InvalidActivationCodeException {
        final Account user = userManager.getUserByActivationCode(code);
        if (user == null || user.isActive()) {
            throw new InvalidActivationCodeException("Invalid Activation Code");
        } else {
            final Calendar now = Calendar.getInstance();
            user.setActivationDate(now);
            userManager.updateUser(user);
            notificationService.activateAccount(user);
        }
    }

    @Override
    public RestResetPasswordResponse resetPassword(@NotNull String email)
            throws InvalidUserEmailException, InvalidAuthSchemaException {
        final Account user = userManager.getUserBy(email);
        if (user != null) {
            RestResetPasswordResponse response = new RestResetPasswordResponse();
            if (user.getAuthenticationType().equals(AuthenticationType.GOOGLE_OAUTH2)) {
                response.setAction(RestResetPasswordAction.OAUTH2_USER);
                return response;
            }

            if (user.getAuthenticationType() != AuthenticationType.DATABASE) {
                throw new InvalidAuthSchemaException("Could not change password for " + user.getAuthenticationType().getCode());
            }

            // Generate a random password ...
            final String password = randomstring(8, 10);
            user.setPassword(password);
            updateUser(user);

            // Send an email with the new temporal password ...
            notificationService.resetPassword(user, password);

            response.setAction(RestResetPasswordAction.EMAIL_SENT);
            return response;
        } else {
            throw new InvalidUserEmailException("The email '" + email + "' does not exists.");
        }
    }

    private String randomstring(int lo, int hi) {
        int n = rand(lo, hi);
        byte[] b = new byte[n];
        for (int i = 0; i < n; i++)
            b[i] = (byte) rand('@', 'Z');
        return new String(b);
    }

    private int rand(int lo, int hi) {
        java.util.Random rn = new java.util.Random();
        int n = hi - lo + 1;
        int i = rn.nextInt() % n;
        if (i < 0)
            i = -i;
        return lo + i;
    }

    @Override
    public void removeUser(@NotNull Account user) {
        // Force object reload before removing....
        final Account userBy = userManager.getUserBy(user.getEmail());
        userManager.removeUser(userBy);
    }

    @Override
    public void auditLogin(@NotNull Account user) {
        if (user == null) {
            throw new IllegalArgumentException("User can not be null");
        }
        final AccessAuditory accessAuditory = new AccessAuditory();
        accessAuditory.setUser(user);
        accessAuditory.setLoginDate(Calendar.getInstance());
        userManager.auditLogin(accessAuditory);

        // Track user login with enhanced metrics
        metricsService.trackUserLogin(user, "database");
    }

    @NotNull
    public Account createUser(@NotNull Account user, boolean emailConfirmEnabled, boolean welcomeEmail) throws WiseMappingException {
        final UUID uuid = UUID.randomUUID();
        user.setCreationDate(Calendar.getInstance());
        user.setActivationCode(uuid.getLeastSignificantBits());

        if (emailConfirmEnabled) {
            user.setActivationDate(null);
        } else {
            user.setActivationDate(Calendar.getInstance());
        }

        final Collaborator col = userManager.getCollaboratorBy(user.getEmail());
        if (col != null) {
            userManager.createUser(user, col);
        } else {
            userManager.createUser(user);
        }

        //create welcome map
        final Mindmap mindMap = buildTutorialMindmap(user.getFirstname());
        mindmapService.addMindmap(mindMap, user);


        // Send registration email.
        if (emailConfirmEnabled) {
            notificationService.sendRegistrationEmail(user);
        } else if (welcomeEmail) {
            // Send a welcome email ..
            notificationService.newAccountCreated(user);
        }

        return user;
    }

    @NotNull
    public Account createAndAuthUserFromGoogle(@NotNull String callbackCode) throws WiseMappingException {
        GoogleAccountBasicData data;
        try {
            data = googleService.processCallback(callbackCode);
        } catch (HttpInvokerException e) {
            logger.debug(e.getMessage(), e);
            throw new OAuthAuthenticationException(e);
        }

        // Callback is successful, the email of the user exits. Is an existing account ?
        Account result = userManager.getUserBy(data.getEmail());
        if (result == null) {
            // Check if there's an existing collaborator with this email
            Collaborator existingCollaborator = userManager.getCollaboratorBy(data.getEmail());

            Account newUser = new Account();
            // new registrations from google starts sync
            newUser.setGoogleSync(true);
            newUser.setEmail(data.getEmail());
            newUser.setFirstname(data.getName());
            newUser.setLastname(data.getLastName());
            newUser.setAuthenticationType(AuthenticationType.GOOGLE_OAUTH2);
            newUser.setGoogleToken(data.getAccessToken());

            if (existingCollaborator != null) {
                // Migrate existing collaborator to account
                logger.debug("Migrating existing collaborator to Google OAuth account for email: " + data.getEmail());
                result = userManager.createUser(newUser, existingCollaborator);
            } else {
                // Create new account
                result = this.createUser(newUser, false, true);
            }
            logger.debug("Google account successfully created");
        }

        // Is the user a non-oauth user ?
        if (result.getGoogleSync() == null || !result.getGoogleSync()) {
            result.setGoogleSync(false);
            result.setSyncCode(callbackCode);
            result.setGoogleToken(data.getAccessToken());
            userManager.updateUser(result);
        }
        return result;
    }

    public Account createAndAuthUserFromFacebook(@NotNull String callbackCode) throws WiseMappingException {
        FacebookAccountBasicData data;
        try {
            data = facebookService.processCallback(callbackCode);
        } catch (HttpInvokerException e) {
            logger.debug(e.getMessage(), e);
            throw new OAuthAuthenticationException(e);
        }
        // Callback is successful, the email of the user exits. Is an existing account ?
        Account result = userManager.getUserBy(data.getEmail());
        if (result == null) {
            // Check if there's an existing collaborator with this email
            Collaborator existingCollaborator = userManager.getCollaboratorBy(data.getEmail());

            Account newUser = new Account();
            // new registrations from facebook starts sync
            newUser.setGoogleSync(true);
            newUser.setEmail(data.getEmail());
            newUser.setFirstname(data.getName());
            newUser.setLastname(data.getLastName());
            newUser.setAuthenticationType(AuthenticationType.FACEBOOK_OAUTH2);
            newUser.setGoogleToken(data.getAccessToken());

            if (existingCollaborator != null) {
                // Migrate existing collaborator to account
                logger.debug("Migrating existing collaborator to Facebook OAuth account for email: " + data.getEmail());
                result = userManager.createUser(newUser, existingCollaborator);
            } else {
                // Create new account
                result = this.createUser(newUser, false, true);
            }
            logger.debug("Facebook account successfully created");
        }

        // Is the user a non-oauth user ?
        if (result.getGoogleSync() == null || !result.getGoogleSync()) {
            result.setGoogleSync(false);
            result.setSyncCode(callbackCode);
            result.setGoogleToken(data.getAccessToken());
            userManager.updateUser(result);
        }
        return result;
    }

    public Account confirmGoogleAccountSync(@NotNull String email, @NotNull String code) throws WiseMappingException {
        final Account existingUser = userManager.getUserBy(email);
        // additional security check
        if (existingUser == null || !existingUser.getSyncCode().equals(code)) {
            throw new WiseMappingException("User not found / incorrect code");
        }
        existingUser.setGoogleSync(true);
        existingUser.setSyncCode(null);
        // user will not be able to login again with usr/pwd schema
        existingUser.setAuthenticationType(AuthenticationType.GOOGLE_OAUTH2);
        existingUser.setPassword("");
        userManager.updateUser(existingUser);

        return existingUser;
    }


    public Mindmap buildTutorialMindmap(@NotNull String firstName) throws InvalidMindmapException {
        //To change body of created methods use File | Settings | File Templates.
        final Locale locale = LocaleContextHolder.getLocale();
        Mindmap result = new Mindmap();
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("messages", messageSource);
        model.put("noArgs", new Object[]{});
        model.put("locale", locale);

        final String mapXml = VelocityEngineUtils
                .mergeTemplateIntoString(velocityEngineWrapper
                        .getVelocityEngine(), "/samples/tutorial.vm", model);

        result.setXmlStr(mapXml);
        result.setTitle(messageSource.getMessage("WELCOME", null, locale) + " " + firstName);
        result.setDescription("");

        return result;
    }

    @Override
    public void changePassword(@NotNull Account user) {
        notificationService.passwordChanged(user);
        userManager.updateUser(user);
    }

    @Override
    public Account getUserBy(String email) {
        return userManager.getUserBy(email);
    }

    @Override
    @Nullable
    public Account getUserBy(int id) {
        return userManager.getUserBy(id);
    }

    @Override
    public java.util.List<Account> getAllUsers() {
        return userManager.getAllUsers();
    }

    @Override
    public void updateUser(@NotNull Account user) {
        userManager.updateUser(user);
    }

    public void setUserManager(@NotNull UserManager userManager) {
        this.userManager = userManager;
    }

    public void setMindmapService(@NotNull MindmapService mindmapService) {
        this.mindmapService = mindmapService;
    }

    public void setNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void setMessageSource(@NotNull MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
        this.velocityEngineWrapper = velocityEngineWrapper;
    }

    public void setGoogleService(GoogleService googleService) {
        this.googleService = googleService;
    }

    public void setFacebookService(FacebookService facebookService) {
        this.facebookService = facebookService;
    }

    @Override
    public Account getCasUserBy(String uid) {
        // TODO Auto-generated method stub
        return null;
    }
}
