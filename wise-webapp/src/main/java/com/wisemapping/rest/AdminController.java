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

import com.mangofactory.swagger.annotations.ApiModel;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.AuthenticationType;
import com.wisemapping.model.Collaboration;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.service.MindmapService;
import com.wisemapping.service.UserService;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

@Api(value = "AdminApi", description = "Administrative Related Objects.")
@Controller
public class AdminController extends BaseController {
    @Qualifier("userService")
    @Autowired
    private UserService userService;
    @Qualifier("mindmapService")
    @Autowired
    private MindmapService mindmapService;

    @ApiOperation("Note: Administration permissions required.")
    @RequestMapping(method = RequestMethod.GET, value = "admin/users/{id}", produces = {"application/json", "application/xml"})
    @ResponseBody
    public RestUser getUserById(@PathVariable @ApiParam(required = true, value = "User Id", allowableValues = "range[1," + Long.MAX_VALUE + "]") long id) throws IOException {
        final User userBy = userService.getUserBy(id);
        if (userBy == null) {
            throw new IllegalArgumentException("User could not be found");
        }
        return new RestUser(userBy);
    }

    @ApiOperation("Note: Administration permissions required.")
    @RequestMapping(method = RequestMethod.GET, value = "admin/users/email/{email}", produces = {"application/json", "application/xml"})
    @ResponseBody
    public RestUser getUserByEmail(@PathVariable String email) throws IOException {
        final User user = userService.getUserBy(email);
        if (user == null) {
            throw new IllegalArgumentException("User '" + email + "' could not be found");
        }
        return new RestUser(user);
    }

    @ApiOperation("Note: Administration permissions required.")
    @RequestMapping(method = RequestMethod.POST, value = "admin/users", consumes = {"application/xml", "application/json"}, produces = {"application/json", "application/xml"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createUser(@RequestBody @ApiParam(required = true) RestUser user, HttpServletResponse response) throws WiseMappingException {
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

    @ApiOperation("Note: Administration permissions required.")
    @RequestMapping(method = RequestMethod.PUT, value = "admin/users/{id}/password", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody @ApiParam(required = true) String password, @PathVariable @ApiParam(required = true, value = "User Id", allowableValues = "range[1," + Long.MAX_VALUE + "]") long id) throws WiseMappingException {
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

    @ApiOperation("Note: Administration permissions required.")
    @RequestMapping(method = RequestMethod.DELETE, value = "admin/users/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void getUserByEmail(@PathVariable @ApiParam(required = true, allowableValues = "range[1," + Long.MAX_VALUE + "]") long id) throws WiseMappingException {
        final User user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }
        userService.deleteUser(user);
    }

    @ApiOperation("Note: Administration permissions required.")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.GET, value = "admin/database/purge")
    public void purgeDB(@RequestParam(required = true) Integer minUid, @RequestParam(required = true) Integer maxUid, @RequestParam(required = true) Boolean apply) throws WiseMappingException, UnsupportedEncodingException {

        for (int i = minUid; i < maxUid; i++) {

            try {
                System.out.println("Looking for user:" + i);
                final User user = userService.getUserBy(i);
                if (user != null) {
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
                            yearAgo.add(Calendar.MONTH, -4);

                            // The use has only two maps... When they have been modified ..
                            System.out.println("Checking map id:" + mindmap.getId());
                            if (mindmap.getLastModificationTime().before(yearAgo) && !mindmap.isPublic()) {
                                System.out.println("Old map months map:" + mindmap.getId());

                                if (isWelcomeMap(mindmap) || isSimpleMap(mindmap)) {
                                    System.out.println("Purged map id:" + mindmap.getId() + ", userId:" + user.getId());
                                    if (apply) {
                                        mindmapService.removeMindmap(mindmap, user);
                                    }
                                }
                            }

                            // Purge history ...
                            mindmapService.purgeHistory(mindmap.getId());
                        }
                    }
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (WiseMappingException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (RuntimeException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

    @ApiOperation("Note: Administration permissions required.")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @RequestMapping(method = RequestMethod.GET, value = "admin/database/purge/history")
    public void purgeHistory(@RequestParam(required = true) Integer mapId) throws WiseMappingException, IOException {

        mindmapService.purgeHistory(mapId);
    }

    private boolean isWelcomeMap(@NotNull Mindmap mindmap) throws UnsupportedEncodingException {
        // Is welcome map ?
        final String xmlStr = mindmap.getXmlStr();
        boolean oldWelcomeMap = xmlStr.contains("Welcome to WiseMapping") && xmlStr.contains("My Wisemaps");
        return oldWelcomeMap;
    }

    public boolean isSimpleMap(@NotNull Mindmap mindmap) throws UnsupportedEncodingException {
        String xmlStr = mindmap.getXmlStr();
        String[] topics = xmlStr.split(Pattern.quote("<topic"));
        return topics.length <= 3;
    }
}
