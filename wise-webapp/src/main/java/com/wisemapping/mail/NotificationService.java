/*
 *    Copyright [2015] [wisemapping]
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

package com.wisemapping.mail;

import com.wisemapping.filter.SupportedUserAgent;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestLogItem;
import com.wisemapping.util.VelocityEngineUtils;
import com.wisemapping.util.VelocityEngineWrapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

final public class NotificationService {
    final private static Logger logger = Logger.getLogger(Mailer.class);
    final private static String DEFAULT_WISE_URL = "http://localhost:8080/wisemapping";
    private VelocityEngineWrapper velocityEngineWrapper;

    @Autowired
    private Mailer mailer;

    private String baseUrl;

    public NotificationService() {
        NotifierFilter notificationFilter = new NotifierFilter();
    }

    public void newCollaboration(@NotNull Collaboration collaboration, @NotNull Mindmap mindmap, @NotNull User user, @Nullable String message) {

        try {
            // Sent collaboration email ...
            final String formMail = mailer.getServerSenderEmail();

            // Is the user already registered user ?.
            final String collabEmail = collaboration.getCollaborator().getEmail();

            // Build the subject ...
            final String subject = "[WiseMapping] " + user.getFullName() + " has shared a mindmap with you";

            // Fill template properties ...
            final Map<String, Object> model = new HashMap<>();
            model.put("mindmap", mindmap);
            model.put("message", "message");
            model.put("ownerName", user.getFirstname());
            model.put("mapEditUrl", getBaseUrl() + "/c/maps/" + mindmap.getId() + "/edit");
            model.put("baseUrl", getBaseUrl());
            model.put("senderMail", user.getEmail());
            model.put("message", message);
            model.put("supportEmail", mailer.getSupportEmail());

            mailer.sendEmail(formMail, collabEmail, subject, model, "newCollaboration.vm");
        } catch (Exception e) {
            handleException(e);
        }

    }

    public void resetPassword(@NotNull User user, @NotNull String temporalPassword) {
        final String mailSubject = "[WiseMapping] Your new password";
        final String messageTitle = "Your new password has been generated";
        final String messageBody =
                "<p>Someone, most likely you, requested a new password for your WiseMapping account. </p>\n" +
                        "<p><strong>Here is your new password: " + temporalPassword + "</strong></p>\n" +
                        "<p>You can login clicking <a href=\"" + getBaseUrl() + "/c/login\">here</a>. We strongly encourage you to change the password as soon as possible.</p>";

        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }


    public void passwordChanged(@NotNull User user) {
        final String mailSubject = "[WiseMapping] Your password has been changed";
        final String messageTitle = "Your password has been changed successfully";
        final String messageBody =
                "<p>This is only an notification that your password has been changed. No further action is required.</p>";

        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }

    public void newAccountCreated(@NotNull User user) {
        final String mailSubject = "Welcome to WiseMapping !";
        final String messageTitle = "Your account has been created successfully";
        final String messageBody =
                "<p> Thank you for your interest in WiseMapping.  Click <a href='https://app.wisemapping.com/c/login'>here</a> to start creating and sharing new mind maps. If have any feedback or idea, send us an email to <a href=\"mailto:feedback@wisemapping.com\">feedback@wisemapping.com</a> .We'd love to hear from  you.</p>";
        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }

    private void sendTemplateMail(@NotNull User user, @NotNull String mailSubject, @NotNull String messageTitle, @NotNull String messageBody) {

        try {
            final Map<String, Object> model = new HashMap<>();
            model.put("firstName", user.getFirstname());
            model.put("messageTitle", messageTitle);
            model.put("messageBody", messageBody);
            model.put("baseUrl", getBaseUrl());
            model.put("supportEmail", mailer.getSupportEmail());

            mailer.sendEmail(mailer.getServerSenderEmail(), user.getEmail(), mailSubject, model, "baseLayout.vm");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        System.err.println("An expected error has occurred trying to send an email notification. Usually, the main reason for this is that the SMTP server properties has not been configured properly. Edit the WEB-INF/app.properties file and verify the SMTP server configuration properties.");
        System.err.println("Cause:" + e.getMessage());

    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }


    public void activateAccount(@NotNull User user) {
        final Map<String, User> model = new HashMap<>();
        model.put("user", user);
        mailer.sendEmail(mailer.getServerSenderEmail(), user.getEmail(), "[WiseMapping] Active account", model, "activationAccountMail.vm");
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

    public void setVelocityEngineWrapper(VelocityEngineWrapper engine) {
        this.velocityEngineWrapper = engine;
    }

    public void reportJavascriptException(@Nullable Mindmap mindmap, @Nullable User user, @NotNull RestLogItem errorItem, @NotNull HttpServletRequest request) {

        final Map<String, String> model = new HashMap<>();
        model.put("JS-MSG", errorItem.getJsErrorMsg());
        model.put("JS-STACK", errorItem.getJsStack());

        String mindmapXML = "";
        try {
            mindmapXML = StringEscapeUtils.escapeXml(mindmap == null ? "map not found" : mindmap.getXmlStr());
        } catch (UnsupportedEncodingException e) {
            // Ignore ...
        }
        model.put("mapId", Integer.toString(mindmap.getId()));
        model.put("mapTitle", mindmap.getTitle());

        logError(model, user, request);
        logger.error("Unexpected editor mindmap => " + mindmapXML);
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
        String retValue;
        StringWriter sw = null;
        PrintWriter pw = null;
        try {
            sw = new StringWriter();
            pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            retValue = sw.toString();
        } finally {
            IOUtils.closeQuietly(pw);
            IOUtils.closeQuietly(sw);
        }
        return retValue;
    }

    public String getBaseUrl() {
        if ("${site.baseurl}".equals(baseUrl)) {
            baseUrl = DEFAULT_WISE_URL;
            System.err.println("Warning: site.baseurl has not being configured. Mail site references could be not properly sent. Using :" + baseUrl);
        }
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}


