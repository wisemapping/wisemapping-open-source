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

import com.wisemapping.filter.SupportedUserAgent;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestLogItem;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.ResourceBundleMessageSource;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
final public class NotificationService {
    final private static Logger logger = LogManager.getLogger();
    @Autowired
    private ResourceBundleMessageSource messageSource;

    @Autowired
    private MailerService mailerService;

    @Value("${site.baseurl:http://localhost:8080/}")
    private String baseUrl;

    public void newCollaboration(@NotNull Collaboration collaboration, @NotNull Mindmap mindmap, @NotNull User user, @Nullable String message) {
        final Locale locale = LocaleContextHolder.getLocale();

        try {
            // Sent collaboration email ...
            final String formMail = mailerService.getServerSenderEmail();

            // Is the user already registered user ?.
            final String collabEmail = collaboration.getCollaborator().getEmail();

            // Build the subject ...
            final String subject = messageSource.getMessage("SHARE_MAP.EMAIL_SUBJECT", new Object[]{user.getFullName()}, locale);

            // Fill template properties ...
            final Map<String, Object> model = new HashMap<>();
            model.put("mindmap", mindmap);
            model.put("ownerName", user.getFirstname());
            model.put("mapEditUrl", getBaseUrl() + "/c/maps/" + mindmap.getId() + "/edit");
            model.put("baseUrl", getBaseUrl());
            model.put("senderMail", user.getEmail());
            model.put("message", message);
            model.put("doNotReplay", messageSource.getMessage("EMAIL.DO_NOT_REPLAY", new Object[]{mailerService.getSupportEmail()}, locale));

            // To resolve resources on templates ...
            model.put("noArg", new Object[]{});
            model.put("messages", messageSource);
            model.put("locale", locale);

            mailerService.sendEmail(formMail, collabEmail, subject, model, "newCollaboration.vm");
        } catch (Exception e) {
            handleException(e);
        }

    }

    public void resetPassword(@NotNull User user, @NotNull String temporalPassword) {
        final Locale locale = LocaleContextHolder.getLocale();

        final String mailSubject = messageSource.getMessage("CHANGE_PASSWORD.EMAIL_SUBJECT", null, locale);
        final String messageTitle = messageSource.getMessage("CHANGE_PASSWORD.EMAIL_TITLE", null, locale);
        final String messageBody = messageSource.getMessage("CHANGE_PASSWORD.EMAIL_BODY", new Object[]{temporalPassword, getBaseUrl()}, locale);

        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }


    public void passwordChanged(@NotNull User user) {
        final Locale locale = LocaleContextHolder.getLocale();

        final String mailSubject = messageSource.getMessage("PASSWORD_CHANGED.EMAIL_SUBJECT", null, locale);
        final String messageTitle = messageSource.getMessage("PASSWORD_CHANGED.EMAIL_TITLE", null, locale);
        final String messageBody = messageSource.getMessage("PASSWORD_CHANGED.EMAIL_BODY", null, locale);

        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }

    public void newAccountCreated(@NotNull User user) {
        final Locale locale = LocaleContextHolder.getLocale();

        final String mailSubject = messageSource.getMessage("REGISTRATION.EMAIL_SUBJECT", null, locale);
        final String messageTitle = messageSource.getMessage("REGISTRATION.EMAIL_TITLE", null, locale);
        final String messageBody = messageSource.getMessage("REGISTRATION.EMAIL_BODY", null, locale);
        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }

    private void sendTemplateMail(@NotNull User user, @NotNull String mailSubject, @NotNull String messageTitle, @NotNull String messageBody) {
        final Locale locale = LocaleContextHolder.getLocale();

        try {
            final Map<String, Object> model = new HashMap<>();
            model.put("firstName", user.getFirstname());
            model.put("messageTitle", messageTitle);
            model.put("messageBody", messageBody);
            model.put("baseUrl", getBaseUrl());
            model.put("supportEmail", mailerService.getSupportEmail());
            model.put("doNotReplay", messageSource.getMessage("EMAIL.DO_NOT_REPLAY", new Object[]{mailerService.getSupportEmail()}, locale));

            // To resolve resources on templates ...
            model.put("noArg", new Object[]{});
            model.put("messages", messageSource);
            model.put("locale", locale);

            logger.debug("Email properties->" + model);
            mailerService.sendEmail(mailerService.getServerSenderEmail(), user.getEmail(), mailSubject, model, "baseLayout.vm");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        System.err.println("An expected error has occurred trying to send an email notification. Usually, the main reason for this is that the SMTP server properties has not been configured properly. Edit the WEB-INF/app.properties file and verify the SMTP server configuration properties.");
        System.err.println("Cause:" + e.getMessage());

    }

    public void setMailer(MailerService mailerService) {
        this.mailerService = mailerService;
    }


    public void activateAccount(@NotNull User user) {
        final Map<String, Object> model = new HashMap<>();
        model.put("user", user);
        mailerService.sendEmail(mailerService.getServerSenderEmail(), user.getEmail(), "[WiseMapping] Active account", model, "activationAccountMail.vm");
    }

    public void sendRegistrationEmail(@NotNull User user) {
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

    public void reportJavascriptException(@Nullable Mindmap mindmap, @Nullable User user, @NotNull RestLogItem errorItem, @NotNull HttpServletRequest request) {

        final Map<String, String> summary = new HashMap<>();
        summary.put("JS-MSG", errorItem.getJsErrorMsg());
        summary.put("JS-STACK", errorItem.getJsStack());

        String mindmapXML = "";
        try {
            mindmapXML = StringEscapeUtils.escapeXml(mindmap == null ? "map not found" : mindmap.getXmlStr());
        } catch (UnsupportedEncodingException e) {
            // Ignore ...
        }
        summary.put("mapId", Integer.toString(mindmap.getId()));
        summary.put("mapTitle", mindmap.getTitle());

        logError(summary, user, request);
        logger.error("Unexpected editor mindmap => " + mindmapXML);
        logger.error("Unexpected editor JS Stack => " + errorItem.getJsErrorMsg() + "-" + errorItem.getJsStack());
    }

    private void logError(@NotNull Map<String, String> model, @Nullable User user, @NotNull HttpServletRequest request) {
        model.put("fullName", (user != null ? user.getFullName() : "'anonymous'"));
        final String userEmail = user != null ? user.getEmail() : "'anonymous'";

        model.put("email", userEmail);
        model.put("userAgent", request.getHeader(SupportedUserAgent.USER_AGENT_HEADER));
        model.put("server", request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
        model.put("requestURI", request.getRequestURI());
        model.put("method", request.getMethod());
        model.put("remoteAddress", request.getRemoteAddr());

        String errorAsString = model.keySet().stream()
                .map(key -> key + "=" + model.get(key))
                .collect(Collectors.joining(", ", "{", "}"));

        logger.error("Unexpected editor info => " + errorAsString);
    }

    public void reportJavaException(@NotNull Throwable exception, @Nullable User user, @NotNull HttpServletRequest request) {
        final Map<String, String> model = new HashMap<>();
        model.put("errorMsg", stackTraceToString(exception));

        logError(model, user, request);
    }

    public String stackTraceToString(@NotNull Throwable e) {
        String retValue = "";
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        try (sw; pw) {
            e.printStackTrace(pw);
            retValue = sw.toString();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return retValue;
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


