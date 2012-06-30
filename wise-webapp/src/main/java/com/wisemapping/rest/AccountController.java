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

package com.wisemapping.rest;

import com.wisemapping.mail.NotificationService;
import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestLogItem;
import com.wisemapping.security.Utils;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class AccountController extends BaseController {
    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @Autowired
    private NotificationService notificationService;

    @RequestMapping(method = RequestMethod.PUT, value = "account/password", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null");
        }

        final User user = Utils.getUser();
        user.setPassword(password);
        userService.changePassword(user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "account/firstname", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changeFirstname(@RequestBody String firstname) {
        if (firstname == null) {
            throw new IllegalArgumentException("Firstname can not be null");
        }

        final User user = Utils.getUser();
        user.setFirstname(firstname);
        userService.updateUser(user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "account/lastname", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changeLastName(@RequestBody String lastname) {
        if (lastname == null) {
            throw new IllegalArgumentException("lastname can not be null");

        }
        final User user = Utils.getUser();
        user.setLastname(lastname);
        userService.updateUser(user);
    }

    @RequestMapping(method = RequestMethod.PUT, value = "account/locale", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changeLanguage(@RequestBody String language) {
        if (language == null) {
            throw new IllegalArgumentException("language can not be null");

        } if (!language.equals("en") && !language.equals("es") ){
            throw new IllegalArgumentException("language not supported yet");
        }

        final User user = Utils.getUser();
        user.setLocale(language);
        userService.updateUser(user);
    }

    @RequestMapping(method = RequestMethod.POST, value = "logger/editor", consumes = {"application/xml", "application/json"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody RestLogItem item) {
        final MindMap mindmap = mindmapService.findMindmapById(item.getMapId());
        final User user = Utils.getUser();
        notificationService.reportMindmapEditorError(mindmap, user, item.getUserAgent(), item.getJsErrorMsg() + "\n" + item.getJsStack());
    }

}
