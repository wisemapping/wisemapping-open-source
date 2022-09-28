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

package com.wisemapping.rest;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.mail.NotificationService;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Label;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestLogItem;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.security.Utils;
import com.wisemapping.service.LabelService;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Controller
public class AccountController extends BaseController {
    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @Qualifier("labelService")
    @Autowired
    private LabelService labelService;

    @Autowired
    private NotificationService notificationService;


    @RequestMapping(method = RequestMethod.PUT, value = "account/password", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null");
        }

        final User user = Utils.getUser(true);
        user.setPassword(password);
        userService.changePassword(user);
    }

    @RequestMapping(method = RequestMethod.GET, value = "/account", produces = {"application/json"})
    public RestUser fetchAccount() {
        final User user = Utils.getUser(true);
        return new RestUser(user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "account/firstname", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changeFirstname(@RequestBody String firstname) {
        if (firstname == null) {
            throw new IllegalArgumentException("Firstname can not be null");
        }

        final User user = Utils.getUser(true);
        user.setFirstname(firstname);
        userService.updateUser(user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "account/lastname", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changeLastName(@RequestBody String lastname) {
        if (lastname == null) {
            throw new IllegalArgumentException("lastname can not be null");

        }
        final User user = Utils.getUser(true);
        user.setLastname(lastname);
        userService.updateUser(user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "account/locale", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changeLanguage(@RequestBody String language) {
        if (language == null) {
            throw new IllegalArgumentException("language can not be null");

        }

        final User user = Utils.getUser(true);
        user.setLocale(language);
        userService.updateUser(user);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "account")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser() throws WiseMappingException {
        // Delete collaborations ...
        final User user = Utils.getUser(true);
        final List<Collaboration> collaborations = mindmapService.findCollaborations(user);
        for (Collaboration collaboration : collaborations) {
            final Mindmap mindmap = collaboration.getMindMap();
            mindmapService.removeMindmap(mindmap, user);
        }

        // Delete labels ....
        final List<Label> labels = labelService.getAll(user);
        labels.forEach(l -> {
            try {
                labelService.removeLabel(l, user);
            } catch (WiseMappingException e) {
                throw new IllegalStateException(e);
            }
        });

        // Finally, delete user ...
        userService.removeUser(user);
    }
}
