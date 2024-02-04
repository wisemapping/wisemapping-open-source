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
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/restfull/admin/")
@PreAuthorize("isAuthenticated() and hasRole('ROLE_ADMIN')")
public class AdminController extends BaseController {
    @Qualifier("userService")
    @Autowired
    private UserService userService;

    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @RequestMapping(method = RequestMethod.GET, value = "users/{id}", produces = {"application/json"})
    @ResponseBody
    public RestUser getUserById(@PathVariable int id) throws IOException {
        final User userBy = userService.getUserBy(id);
        if (userBy == null) {
            throw new IllegalArgumentException("User could not be found");
        }
        return new RestUser(userBy);
    }

    @RequestMapping(method = RequestMethod.GET, value = "users/email/{email:.+}", produces = {"application/json"})
    @ResponseBody
    public RestUser getUserByEmail(@PathVariable String email) throws IOException {
        final User user = userService.getUserBy(email);
        if (user == null) {
            throw new IllegalArgumentException("User '" + email + "' could not be found");
        }
        return new RestUser(user);
    }

    @RequestMapping(method = RequestMethod.POST, value = "users", consumes = {"application/json"}, produces = {"application/json"})
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


        final String password = delegated.getPassword();
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("password can not be null");
        }

        // Finally create the user ...
        delegated.setAuthenticationType(AuthenticationType.DATABASE);
        userService.createUser(delegated, false, true);
        response.setHeader("Location", "/api/restfull/admin/users/" + user.getId());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "users/{id}/password", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody String password, @PathVariable int id) throws WiseMappingException {
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

    @RequestMapping(method = RequestMethod.DELETE, value = "users/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUserByEmail(@PathVariable int id) throws WiseMappingException {
        final User user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }

        final List<Collaboration> collaborations = mindmapService.findCollaborations(user);
        for (Collaboration collaboration : collaborations) {
            final Mindmap mindmap = collaboration.getMindMap();
            mindmapService.removeMindmap(mindmap, user);
        }

        userService.removeUser(user);
    }
}
