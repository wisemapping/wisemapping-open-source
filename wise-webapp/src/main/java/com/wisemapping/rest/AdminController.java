package com.wisemapping.rest;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
public class AdminController extends BaseController {
    @Autowired
    private UserService userService;

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

    @RequestMapping(method = RequestMethod.GET, value = "admin/users/username/{username}", produces = {"application/json", "text/html", "application/xml"})
    @ResponseBody
    public ModelAndView getUserByUsername(@PathVariable String username) throws IOException {
        final User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new IllegalArgumentException("User '" + username + "' could not be found");
        }
        return new ModelAndView("userView", "user", new RestUser(user));
    }

    @RequestMapping(method = RequestMethod.POST, value = "admin/users", consumes = {"application/xml", "application/json"}, produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createUser(@RequestBody RestUser user, HttpServletResponse response) throws IOException, WiseMappingException {
        if (user == null) {
            throw new IllegalArgumentException("User could not be found");
        }

        // User already exists ?
        final String email = user.getEmail();
        if (userService.getUserBy(email) != null) {
            throw new IllegalArgumentException("User already exists with this email.");
        }

        final String username = user.getUsername();
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("username can not be null");
        }

        if (userService.getUserByUsername(username) != null) {
            throw new IllegalArgumentException("User already exists with this username.");
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
        userService.createUser(delegated, false);
        response.setHeader("Location", "/service/admin/users/" + user.getId());
    }

    @RequestMapping(method = RequestMethod.PUT, value = "admin/users/{id}/password", consumes = {"text/plain"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void changePassword(@RequestBody String password, @PathVariable long id) throws IOException, WiseMappingException {
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

    @RequestMapping(method = RequestMethod.DELETE, value = "admin/users/{id}", produces = {"application/json", "text/html", "application/xml"})
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void getUserByEmail(@PathVariable long id) throws IOException, WiseMappingException {
        final User user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }
        userService.deleteUser(user);
    }

}
