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

import com.wisemapping.util.VelocityEngineUtils;
import com.wisemapping.util.VelocityEngineWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public final class MailerService {

    //~ Instance fields ......................................................................................

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private VelocityEngineWrapper velocityEngineWrapper;

    @Value("${app.mail.sender-email}")
    private String serverFromEmail;

    @Value("${app.mail.enabled:true}")
    private boolean isEnabled;

    @Value("${app.mail.support-email}")
    private String supportEmail;

    //~ Methods ..............................................................................................

    public String getServerSenderEmail() {
        return serverFromEmail;
    }

    public void sendEmail(final String from, final String to, final String subject, final Map<String, Object> model,
                          @NotNull final String templateMail) {
        if (isEnabled) {
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
}
