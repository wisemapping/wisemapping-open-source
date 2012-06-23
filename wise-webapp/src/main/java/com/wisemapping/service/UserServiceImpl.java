/*
*    Copyright [2011] [wisemapping]
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
import com.wisemapping.model.AccessAuditory;
import com.wisemapping.model.Collaborator;
import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;

import java.util.Calendar;
import java.util.UUID;

public class UserServiceImpl
        implements UserService {
    private UserManager userManager;
    private MindmapService mindmapService;
    private NotificationService notificationService;

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
            throws InvalidUserEmailException {
        final User user = userManager.getUserBy(email);
        if (user != null) {
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
        final AccessAuditory accessAuditory = new AccessAuditory();
        accessAuditory.setUser(user);
        accessAuditory.setLoginDate(Calendar.getInstance());
        userManager.auditLogin(accessAuditory);
    }

    public User createUser(@NotNull User user, boolean emailConfirmEnabled) throws WiseMappingException {
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
        mindmapService.addWelcomeMindmap(user);

        // Send registration email.
        if (emailConfirmEnabled) {
            notificationService.sendRegistrationEmail(user);
        } else {
            // Send a welcome email ..
            notificationService.newAccountCreated(user);
        }

        return user;
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
    public User getUserByUsername(String username) {
        return userManager.getUserByUsername(username);
    }

    @Override
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
}
