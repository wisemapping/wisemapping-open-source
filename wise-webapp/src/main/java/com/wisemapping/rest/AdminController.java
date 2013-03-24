/*
*    Copyright [2012] [wisemapping]
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
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestMindmapList;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import sun.util.resources.CalendarData_th;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

@Controller
public class AdminController extends BaseController {
    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;


    @RequestMapping(method = RequestMethod.GET, value = "admin/users/{id}", produces = {"application/json", "text/html", "application/xml"})
    @ResponseBody
    public ModelAndView getUserById(@PathVariable long id) throws IOException {
        final User userBy = userService.getUserBy(id);
        if (userBy == null) {
            throw new IllegalArgumentException("User could not be found");
        }
        return new ModelAndView("userView", "user", new RestUser(userBy));
    }

    @RequestMapping(method = RequestMethod.GET, value = "admin/users/email/{email}", produces = {"application/json", "text/html", "application/xml"})
    @ResponseBody
    public ModelAndView getUserByEmail(@PathVariable String email) throws IOException {
        final User user = userService.getUserBy(email);
        if (user == null) {
            throw new IllegalArgumentException("User '" + email + "' could not be found");
        }
        return new ModelAndView("userView", "user", new RestUser(user));
    }

    @RequestMapping(method = RequestMethod.POST, value = "admin/users", consumes = {"application/xml", "application/json"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createUser(@RequestBody RestUser user, HttpServletResponse response) throws WiseMappingException {
        if (user == null) {
            throw new IllegalArgumentException("User could not be found");
        }

        // User already exists ?
        final String email = user.getEmail();
        if (userService.getUserBy(email) != null) {
            throw new IllegalArgumentException("User already exists with this email.");
        }

        // Run some other validations ...
        final User delegated = user.getDelegated();
        final String lastname = delegated.getLastname();
        if (lastname == null || lastname.isEmpty()) {
            throw new IllegalArgumentException("lastname can not be null");
        }

        final String firstName = delegated.getFirstname();
        if (firstName == null || firstName.isEmpty()) {
            throw new IllegalArgumentException("firstname can not be null");
        }

        // Finally create the user ...
        delegated.setAuthenticationType(AuthenticationType.DATABASE);
        userService.createUser(delegated, false, true);
        response.setHeader("Location", "/service/admin/users/" + user.getId());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "admin/users/{id}/password", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody String password, @PathVariable long id) throws WiseMappingException {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null");
        }

        final User user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }
        user.setPassword(password);
        userService.changePassword(user);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "admin/users/{id}", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void getUserByEmail(@PathVariable long id) throws WiseMappingException {
        final User user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }
        userService.deleteUser(user);
    }

    @RequestMapping(method = RequestMethod.GET, value = "admin/database/purge")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void purgeDB(@RequestParam(required = true) int max) throws UnsupportedEncodingException, WiseMappingException {

        for (int i = 0; i < max; i++) {
            User user;
            try {
                user = userService.getUserBy(i);
            } catch (Exception e) {
                // User does not exit's continue ...
                continue;
            }

            // Do not process admin accounts ...
            if (user.getEmail().contains("wisemapping")) {
                continue;
            }

            // Iterate over the list of maps ...
            final List<Collaboration> collaborations = mindmapService.findCollaborations(user);
            for (Collaboration collaboration : collaborations) {
                final Mindmap mindmap = collaboration.getMindMap();
                if (MindmapFilter.MY_MAPS.accept(mindmap, user)) {

                    final Calendar yearAgo = Calendar.getInstance();
                    yearAgo.add(Calendar.MONTH, -18);
                    // The use has only two maps... When they have been modified ..
                    if (mindmap.getLastModificationTime().before(yearAgo) && !mindmap.isPublic()) {
                        if (isWelcomeMap(mindmap) || isSimpleMap(mindmap)) {
                            mindmapService.removeMindmap(mindmap, user);
                        }
                    }
                }
            }
        }


    }

    private boolean isWelcomeMap(@NotNull Mindmap mindmap) throws UnsupportedEncodingException {
        // Is welcome map ?
        final String xmlStr = mindmap.getXmlStr();
        return xmlStr.contains("Welcome To") && xmlStr.contains("My Wisemaps");
    }

    public boolean isSimpleMap(@NotNull Mindmap mindmap) throws UnsupportedEncodingException {
        String xmlStr = mindmap.getXmlStr();
        String[] topics = xmlStr.split(Pattern.quote("<topic"));
        return topics.length <= 3;
    }
}
