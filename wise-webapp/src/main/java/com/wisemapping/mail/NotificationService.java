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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

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
            final String formMail = mailer.getSiteEmail();

            // Is the user already registered user ?.
            final String collabEmail = collaboration.getCollaborator().getEmail();

            // Build the subject ...
            final String subject = user.getFullName() + " has shared a mindmap with you";

            // Fill template properties ...
            final Map<String, Object> model = new HashMap<String, Object>();
            model.put("mindmap", mindmap);
            model.put("message", "message");
            model.put("ownerName", user.getFirstname());
            model.put("mapEditUrl", baseUrl + "/c/maps/" + mindmap.getId() + "/edit");
            model.put("baseUrl", formMail);
            model.put("senderMail", user.getEmail());
            model.put("message", message);


            mailer.sendEmail(formMail, collabEmail, subject, model, "newCollaboration.vm");
        } catch (Exception e) {
            handleException(e);
        }

    }

    private void handleException(Exception e) {
        e.printStackTrace();
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public void setMailer(Mailer mailer) {
        this.mailer = mailer;
    }


}

