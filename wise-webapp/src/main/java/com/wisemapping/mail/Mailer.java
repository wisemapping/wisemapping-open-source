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

import org.apache.velocity.app.VelocityEngine;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.ui.velocity.VelocityEngineUtils;

import javax.mail.internet.MimeMessage;
import java.util.Map;

public final class Mailer {

    //~ Instance fields ......................................................................................

    private JavaMailSender mailSender;
    private VelocityEngine velocityEngine;
    private String registrationEmail;
    private String siteEmail;

    //~ Methods ..............................................................................................

    public Mailer(String registrationEmail, String siteEmail)
    {
        this.registrationEmail = registrationEmail;
        this.siteEmail = siteEmail;
    }

    public String getRegistrationEmail()
    {
        return registrationEmail;
    }

    public String getSiteEmail()
    {
        return siteEmail;
    }

    public void sendEmail(final String from, final String to, final String subject, final Map model,
                          final String templateMail) {
        final MimeMessagePreparator preparator =
                new MimeMessagePreparator() {
                    public void prepare(MimeMessage mimeMessage)
                            throws Exception {
                        final MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                        message.setTo(to);
                        message.setFrom(from);
                        message.setSubject(subject);

                        final String text =
                                VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "/mail/" + templateMail,
                                        model);

                        message.setText(text, true);
                    }
                };

        this.mailSender.send(preparator);
    }

    public void setMailSender(JavaMailSender mailer) {
        this.mailSender = mailer;
    }

    public void setVelocityEngine(VelocityEngine engine) {
        this.velocityEngine = engine;
    }
}
