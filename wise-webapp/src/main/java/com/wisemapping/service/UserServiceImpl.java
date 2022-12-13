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

package com.wisemapping.service;

import com.wisemapping.dao.UserManager;
import com.wisemapping.exceptions.InvalidMindmapException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.mail.NotificationService;
import com.wisemapping.model.*;
import com.wisemapping.rest.model.RestResetPasswordAction;
import com.wisemapping.rest.model.RestResetPasswordResponse;
import com.wisemapping.service.google.GoogleAccountBasicData;
import com.wisemapping.service.google.GoogleService;
import com.wisemapping.util.VelocityEngineUtils;
import com.wisemapping.util.VelocityEngineWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import java.util.*;

public class UserServiceImpl
        implements UserService {
    private UserManager userManager;
    private MindmapService mindmapService;
    private NotificationService notificationService;
    private MessageSource messageSource;
    private VelocityEngineWrapper velocityEngineWrapper;
	private GoogleService googleService;

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
    public RestResetPasswordResponse resetPassword(@NotNull String email)
            throws InvalidUserEmailException, InvalidAuthSchemaException {
        final User user = userManager.getUserBy(email);
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
    public void removeUser(@NotNull User user) {
        // Force object reload before removing....
        final User userBy = userManager.getUserBy(user.getEmail());
        userManager.removeUser(userBy);
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
    public User createUserFromGoogle(@NotNull String callbackCode) throws WiseMappingException {
		try {
			GoogleAccountBasicData data = googleService.processCallback(callbackCode);
			User existingUser = userManager.getUserBy(data.getEmail());
			if (existingUser == null) {
				User newUser = new User();
				// new registrations from google starts synched
				newUser.setGoogleSync(true);
				newUser.setEmail(data.getEmail());
				newUser.setFirstname(data.getName());
				newUser.setLastname(data.getLastName());
				newUser.setAuthenticationType(AuthenticationType.GOOGLE_OAUTH2);
				newUser.setGoogleToken(data.getAccessToken());
				existingUser = this.createUser(newUser, false, true);
			} else {
				// user exists and doesnt have confirmed account linking, I must wait for confirmation
				if (existingUser.getGoogleSync() == null) {
					existingUser.setGoogleSync(false);
					existingUser.setSyncCode(callbackCode);
					existingUser.setGoogleToken(data.getAccessToken());
					userManager.updateUser(existingUser);
				}

			}
			return existingUser;
		} catch (Exception e) {
			throw new WiseMappingException("Cant create user", e);
		}
    }

	public User confirmAccountSync(@NotNull String email, @NotNull String code) throws WiseMappingException {
		User existingUser = userManager.getUserBy(email);
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
    public User getUserBy(int id) {
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

    public void setVelocityEngineWrapper(VelocityEngineWrapper velocityEngineWrapper) {
        this.velocityEngineWrapper = velocityEngineWrapper;
    }

	public void setGoogleService(GoogleService googleService) {
		this.googleService = googleService;
	}

	@Override
    public User getCasUserBy(String uid) {
        // TODO Auto-generated method stub
        return null;
    }
}
