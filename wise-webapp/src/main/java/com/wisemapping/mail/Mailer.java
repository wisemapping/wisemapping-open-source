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


package com.wisemapping.mail;

import com.wisemapping.util.VelocityEngineUtils;
import com.wisemapping.util.VelocityEngineWrapper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.validation.constraints.NotNull;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class Mailer {

    //~ Instance fields ......................................................................................

    private JavaMailSender mailSender;
    private VelocityEngineWrapper velocityEngineWrapper;
    private final String serverFromEmail;
    private final String supportEmail;
    private final String errorReporterEmail;

    //~ Methods ..............................................................................................

    public Mailer(@NotNull String siteEmail, @NotNull String supportEmail, @NotNull String errorReporterEmail) {
        this.serverFromEmail = siteEmail;
        this.supportEmail = supportEmail;
        this.errorReporterEmail = errorReporterEmail;
    }

    public String getServerSenderEmail() {
        return serverFromEmail;
    }

    public void sendEmail(final String from, final String to, final String subject, final Map<String, Object> model,
                          @NotNull final String templateMail) {
        final MimeMessagePreparator preparator =
                mimeMessage -> {
                    final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, StandardCharsets.UTF_8.name());
                    message.setTo(to);
                    message.setFrom(from);
                    message.setSubject(subject);

                    final String messageBody = VelocityEngineUtils.mergeTemplateIntoString(velocityEngineWrapper.getVelocityEngine(), "/mail/" + templateMail, model);
                    message.setText(messageBody, true);
                };

        this.mailSender.send(preparator);
    }

    public void setMailSender(JavaMailSender mailer) {
        this.mailSender = mailer;
    }

    public void setVelocityEngineWrapper(VelocityEngineWrapper engine) {
        this.velocityEngineWrapper = engine;
    }

    public String getSupportEmail() {
        return supportEmail;
    }

    public String getErrorReporterEmail() {
        return errorReporterEmail;
    }
}
