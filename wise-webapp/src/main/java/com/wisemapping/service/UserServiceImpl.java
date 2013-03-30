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

package com.wisemapping.service;

import com.wisemapping.dao.UserManager;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.mail.NotificationService;
import com.wisemapping.model.*;
import org.apache.velocity.app.VelocityEngine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.velocity.VelocityEngineUtils;

import java.io.IOException;
import java.util.*;

public class UserServiceImpl
        implements UserService {
    private UserManager userManager;
    private MindmapService mindmapService;
    private NotificationService notificationService;
    private MessageSource messageSource;
    private VelocityEngine velocityEngine;


    @Override
    public void activateAccount(long code)
            throws InvalidActivationCodeException {
        final User user = userManager.getUserByActivationCode(code);
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
    public void resetPassword(@NotNull String email)
            throws InvalidUserEmailException, InvalidAuthSchemaException {
        final User user = userManager.getUserBy(email);
        if (user != null) {

            if (user.getAuthenticationType() != AuthenticationType.DATABASE) {
                throw new InvalidAuthSchemaException("Could not change password for " + user.getAuthenticationType().getCode());
            }

            // Generate a random password ...
            final String password = randomstring(8, 10);
            user.setPassword(password);
            updateUser(user);

            // Send an email with the new temporal password ...
            notificationService.resetPassword(user, password);
        } else {
            throw new InvalidUserEmailException("The email '" + email + "' does not exists.");
        }
    }

    private String randomstring(int lo, int hi) {
        int n = rand(lo, hi);
        byte b[] = new byte[n];
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
    public void deleteUser(@NotNull User user) {
        userManager.deleteUser(user);
    }

    @Override
    public void auditLogin(@NotNull User user) {
        if (user == null) {
            throw new IllegalArgumentException("User can not be null");
        }
        final AccessAuditory accessAuditory = new AccessAuditory();
        accessAuditory.setUser(user);
        accessAuditory.setLoginDate(Calendar.getInstance());
        userManager.auditLogin(accessAuditory);
    }

    @NotNull
    public User createUser(@NotNull User user, boolean emailConfirmEnabled, boolean welcomeEmail) throws WiseMappingException {
        final UUID uuid = UUID.randomUUID();
        user.setCreationDate(Calendar.getInstance());
        user.setActivationCode(uuid.getLeastSignificantBits());

        if (emailConfirmEnabled) {
            user.setActivationDate(null);

        } else {
            user.setActivationDate(Calendar.getInstance());
        }

        Collaborator col = userManager.getCollaboratorBy(user.getEmail());

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

    public Mindmap buildTutorialMindmap(@NotNull String firstName) {
        //To change body of created methods use File | Settings | File Templates.
        final Locale locale = LocaleContextHolder.getLocale();
        Mindmap result = new Mindmap();
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("messages", messageSource);
        model.put("noArgs", new Object[]{});
        model.put("locale", locale);

        final String mapXml = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "/samples/tutorial.vm", model);

        result.setXmlStr(mapXml);
        result.setTitle(messageSource.getMessage("WELCOME", null, locale) + " " + firstName);
        result.setDescription("");

        return result;
    }

    @Override
    public void changePassword(@NotNull User user) {
        notificationService.passwordChanged(user);
        userManager.updateUser(user);
    }

    @Override
    public User getUserBy(String email) {
        return userManager.getUserBy(email);
    }

    @Override
    @Nullable
    public User getUserBy(long id) {
        return userManager.getUserBy(id);
    }

    @Override
    public void updateUser(@NotNull User user) {
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

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    @Override
    public User getCasUserBy(String uid) {
        // TODO Auto-generated method stub
        return null;
    }
}
