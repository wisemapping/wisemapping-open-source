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

import com.wisemapping.model.Account;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Mindmap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
final public class NotificationService {
    final private static Logger logger = LogManager.getLogger();
    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    private MailerService mailerService;

    @Value("${app.site.ui-base-url:http://localhost:8080/}")
    private String baseUrl;

    public void newCollaboration(@NotNull Collaboration collaboration, @NotNull Mindmap mindmap, @NotNull Account user, @Nullable String message) {
        final Locale locale = LocaleContextHolder.getLocale();

        try {
            // Sent collaboration email ...
            final String formMail = mailerService.getServerSenderEmail();

            // Is the user already registered user ?.
            final String collabEmail = collaboration.getCollaborator().getEmail();

            // Build the URL
            final String mapEditUrl = getBaseUrl() + "/c/maps/" + mindmap.getId() + "/edit";

            // Build the subject ...
            final String subject = messageSource.getMessage("SHARE_MAP.EMAIL_SUBJECT", new Object[]{user.getFullName()}, locale);

            // Pre-render translated messages with parameters
            final String emailTitle = messageSource.getMessage("SHARE_MAP.EMAIL_TITLE", new Object[]{mapEditUrl, mindmap.getTitle()}, locale);
            final String emailClickToOpen = messageSource.getMessage("SHARE_MAP.EMAIL_CLICK_TO_OPEN", new Object[]{mapEditUrl, mindmap.getTitle()}, locale);
            final String emailMessageFrom = messageSource.getMessage("SHARE_MAP.EMAIL_MESSAGE_FROM", new Object[]{user.getEmail()}, locale);
            final String emailAccountInfo = messageSource.getMessage("SHARE_MAP.EMAIL_ACCOUNT_INFO", null, locale);
            final String emailTeam = messageSource.getMessage("SHARE_MAP.EMAIL_TEAM", null, locale);

            // Fill template properties ...
            final Map<String, Object> model = new HashMap<>();
            model.put("mindmap", mindmap);
            model.put("ownerName", user.getFirstname());
            model.put("mapEditUrl", mapEditUrl);
            model.put("baseUrl", getBaseUrl());
            model.put("senderMail", user.getEmail());
            model.put("message", message);
            model.put("doNotReplay", messageSource.getMessage("EMAIL.DO_NOT_REPLAY", new Object[]{mailerService.getSupportEmail()}, locale));
            
            // Add pre-rendered messages
            model.put("emailTitle", emailTitle);
            model.put("emailClickToOpen", emailClickToOpen);
            model.put("emailMessageFrom", emailMessageFrom);
            model.put("emailAccountInfo", emailAccountInfo);
            model.put("emailTeam", emailTeam);

            mailerService.sendEmail(formMail, collabEmail, subject, model, "newCollaboration.vm");
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }

    }

    public void resetPassword(@NotNull Account user, @NotNull String temporalPassword) {
        final Locale locale = LocaleContextHolder.getLocale();

        final String mailSubject = messageSource.getMessage("CHANGE_PASSWORD.EMAIL_SUBJECT", null, locale);
        final String messageTitle = messageSource.getMessage("CHANGE_PASSWORD.EMAIL_TITLE", null, locale);
        final String messageBody = messageSource.getMessage("CHANGE_PASSWORD.EMAIL_BODY", new Object[]{temporalPassword, getBaseUrl()}, locale);

        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }


    public void passwordChanged(@NotNull Account user) {
        final Locale locale = LocaleContextHolder.getLocale();

        final String mailSubject = messageSource.getMessage("PASSWORD_CHANGED.EMAIL_SUBJECT", null, locale);
        final String messageTitle = messageSource.getMessage("PASSWORD_CHANGED.EMAIL_TITLE", null, locale);
        final String messageBody = messageSource.getMessage("PASSWORD_CHANGED.EMAIL_BODY", null, locale);

        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }

    public void newAccountCreated(@NotNull Account user) {
        final Locale locale = LocaleContextHolder.getLocale();

        final String mailSubject = messageSource.getMessage("REGISTRATION.EMAIL_SUBJECT", null, locale);
        final String messageTitle = messageSource.getMessage("REGISTRATION.EMAIL_TITLE", null, locale);
        final String messageBody = messageSource.getMessage("REGISTRATION.EMAIL_BODY", new Object[]{getBaseUrl()}, locale);
        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }

    private void sendTemplateMail(@NotNull Account user, @NotNull String mailSubject, @NotNull String messageTitle, @NotNull String messageBody) {
        final Locale locale = LocaleContextHolder.getLocale();

        try {
            final Map<String, Object> model = new HashMap<>();
            model.put("firstName", user.getFirstname());
            model.put("messageTitle", messageTitle);
            model.put("messageBody", messageBody);
            model.put("baseUrl", getBaseUrl());
            model.put("support-email", mailerService.getSupportEmail());
            model.put("doNotReplay", messageSource.getMessage("EMAIL.DO_NOT_REPLAY", new Object[]{mailerService.getSupportEmail()}, locale));

            // To resolve resources on templates ...
            model.put("noArg", new Object[]{});
            model.put("messages", messageSource);
            model.put("locale", locale);

            logger.debug("Email properties->" + model);
            mailerService.sendEmail(mailerService.getServerSenderEmail(), user.getEmail(), mailSubject, model, "baseLayout.vm");
        } catch (Exception e) {
            logger.info(e.getMessage(), e);
        }
    }


    public void activateAccount(@NotNull Account user) {
        final Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        mailerService.sendEmail(mailerService.getServerSenderEmail(), user.getEmail(), "[WiseMapping] Active account", model, "activationAccountMail.vm");
    }

    public void sendRegistrationEmail(@NotNull Account user) {
//        throw new UnsupportedOperationException("Not implemented yet");
//        try {
//            final Map<String, String> model = new HashMap<String, String>();
//            model.put("email", user.getEmail());
////            final String activationUrl = "http://wisemapping.com/c/activation?code=" + user.getActivationCode();
////            model.put("emailcheck", activationUrl);
////            mailer.sendEmail(mailer.getServerSenderEmail(), user.getEmail(), "Welcome to Wisemapping!", model,
////                    "confirmationMail.vm");
//        } catch (Exception e) {
//            handleException(e);
//        }
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setMessageSource(ResourceBundleMessageSource messageSource) {
        this.messageSource = messageSource;
    }

}


