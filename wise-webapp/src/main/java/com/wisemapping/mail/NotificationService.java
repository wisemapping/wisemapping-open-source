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

package com.wisemapping.mail;

import com.wisemapping.model.Collaboration;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

final public class NotificationService {


    @Autowired
    private Mailer mailer;
    private String baseUrl;

    public NotificationService() {

    }

    public void newCollaboration(@NotNull Collaboration collaboration, @NotNull MindMap mindmap, @NotNull User user, @Nullable String message) {

        try {
            // Sent collaboration email ...
            final String formMail = mailer.getServerSenderEmail();

            // Is the user already registered user ?.
            final String collabEmail = collaboration.getCollaborator().getEmail();

            // Build the subject ...
            final String subject = "[WiseMapping] " + user.getFullName() + " has shared a mindmap with you";

            // Fill template properties ...
            final Map<String, Object> model = new HashMap<String, Object>();
            model.put("mindmap", mindmap);
            model.put("message", "message");
            model.put("ownerName", user.getFirstname());
            model.put("mapEditUrl", baseUrl + "/c/maps/" + mindmap.getId() + "/edit");
            model.put("baseUrl", formMail);
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
                        "<p><strong>Here is your new password: : " + temporalPassword + "</strong></p>\n" +
                        "<p>You can login clicking <a href=\"" + this.baseUrl + "/c/login\">here</a>. We strongly encourage you to change the password as soon as possible.</p>";

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
                "<p> Thank you for your interest in WiseMapping.  If have any feedback or idea, send us an email to <a href=\"mailto:feedback@wisemapping.com\">feedback@wisemapping.com</a> .We'd love to hear from  you.</p>";
        sendTemplateMail(user, mailSubject, messageTitle, messageBody);
    }

    private void sendTemplateMail(@NotNull User user, @NotNull String mailSubject, @NotNull String messageTitle, @NotNull String messageBody) {

        try {
            final Map<String, Object> model = new HashMap<String, Object>();
            model.put("firstName", user.getFirstname());
            model.put("messageTitle", messageTitle);
            model.put("messageBody", messageBody);
            model.put("baseUrl", this.baseUrl);
            model.put("supportEmail", mailer.getSupportEmail());

            mailer.sendEmail(mailer.getServerSenderEmail(), user.getEmail(), mailSubject, model, "baseLayout.vm");
        } catch (Exception e) {
            handleException(e);
        }
    }

    private void handleException(Exception e) {
        System.err.println("An expected error has occurred trying to send an email notification. Usually, the main reason for this is that the SMTP server properties has not been configured properly. Edit the WEB-INF/app.properties file and verify the SMTP server configuration properties.");
        e.printStackTrace();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }


    public void activateAccount(@NotNull User user) {
        final Map<String, User> model = new HashMap<String, User>();
        model.put("user", user);
        mailer.sendEmail(mailer.getServerSenderEmail(), user.getEmail(), "[WiseMapping] Active account", model, "activationAccountMail.vm");
    }

    public void sendRegistrationEmail(@NotNull User user) {
        throw new UnsupportedOperationException("Not implemented yet");
//        try {
//            final Map<String, Object> model = new HashMap<String, Object>();
//            model.put("user", user);
//            final String activationUrl = "http://wisemapping.com/c/activation?code=" + user.getActivationCode();
//            model.put("emailcheck", activationUrl);
//            mailer.sendEmail(mailer.getServerSenderEmail(), user.getEmail(), "Welcome to Wisemapping!", model,
//                    "confirmationMail.vm");
//        } catch (Exception e) {
//            handleException(e);
//        }
    }

    public void reportMindmapEditorError(@NotNull MindMap mindmap, @NotNull User user, @NotNull String userAgent, @Nullable String jsErrorMsg) {

        try {
            final Map<String, Object> model = new HashMap<String, Object>();
            model.put("user", user);
            model.put("errorMsg", jsErrorMsg);
            model.put("mapXML", mindmap.getXmlStr().replaceAll("<", "&lt;"));
            model.put("mapId", mindmap.getId());
            model.put("mapTitle", mindmap.getTitle());
            model.put("userAgent", userAgent);

            final String errorReporterEmail = mailer.getErrorReporterEmail();
            if (errorReporterEmail != null && !errorReporterEmail.isEmpty()) {
                mailer.sendEmail(mailer.getServerSenderEmail(), errorReporterEmail, "[WiseMapping] Editor error from " + user.getEmail(), model,
                        "errorNotification.vm");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void reportMindmapExportError(@NotNull String exportContent, @NotNull User user, @NotNull String userAgent, @NotNull Throwable exception) {
        try {
            final Map<String, Object> model = new HashMap<String, Object>();
            model.put("user", user);
            model.put("errorMsg", stackTraceToString(exception));
            model.put("mapXML", exportContent.replaceAll("<", "&lt;"));
            model.put("userAgent", userAgent);

            final String errorReporterEmail = mailer.getErrorReporterEmail();
            if (errorReporterEmail != null && !errorReporterEmail.isEmpty()) {
                mailer.sendEmail(mailer.getServerSenderEmail(), errorReporterEmail, "[WiseMapping] Export error from " + user.getEmail(), model,
                        "errorNotification.vm");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public void reportUnexpectedError(@NotNull Throwable exception, @Nullable User user, @NotNull String userAgent) {
        try {
            final Map<String, Object> model = new HashMap<String, Object>();
            model.put("user", user);
            model.put("errorMsg", stackTraceToString(exception));
            model.put("userAgent", userAgent);

            final String errorReporterEmail = mailer.getErrorReporterEmail();
            if (errorReporterEmail != null && !errorReporterEmail.isEmpty()) {
                mailer.sendEmail(mailer.getServerSenderEmail(), errorReporterEmail, "[WiseMapping] Unexpected error from " + (user != null ? user.getEmail() : "anonymous"), model,
                        "errorNotification.vm");
            }
        } catch (Exception e) {
            handleException(e);
        }
    }

    public String stackTraceToString(@NotNull Throwable e) {
        String retValue = null;
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

}


