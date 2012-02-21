package com.wisemapping.rest;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
public class AdminController {
    private static final String RESPONSE_VIEW = "responseView";
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

    @RequestMapping(method = RequestMethod.POST, value = "admin/users", consumes = {"application/xml", "application/json"}, produces = {"application/json", "text/html", "application/xml"})
    public ModelAndView getUserByEmail(@RequestBody RestUser user) throws IOException, WiseMappingException {
        if (user == null) {
            throw new IllegalArgumentException("User could not be found");
        }

        // User already exists ?
        final String email = user.getEmail();
        if(userService.getUserBy(email)!=null){
            throw new IllegalArgumentException("User already exists with this email.");
        }

        userService.createUser(user.getDelegated(), false);
        return new ModelAndView(RESPONSE_VIEW, "message", "User '" + user.getId() + "' created successfully");
    }

    @RequestMapping(method = RequestMethod.PUT, value = "admin/users/{id}/password", consumes = {"text/plain"}, produces = {"application/json", "text/html", "application/xml"})
    public ModelAndView changePassword(@RequestBody String password, @PathVariable long id) throws IOException, WiseMappingException {
        if (password == null) {
            throw new IllegalArgumentException("Password can not be null");
        }

        final User user = userService.getUserBy(id);
        user.setPassword(password);

        userService.changePassword(user);
        return new ModelAndView(RESPONSE_VIEW, "message", "User '" + user.getId() + "' password has been updated.");
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "admin/users/{id}", produces = {"application/json", "text/html", "application/xml"})
    public ModelAndView getUserByEmail(@PathVariable long id) throws IOException, WiseMappingException {
        final User user = userService.getUserBy(id);
        if (user == null) {
            throw new IllegalArgumentException("User '" + id + "' could not be found");
        }
        userService.deleteUser(user);
        return new ModelAndView(RESPONSE_VIEW, "message", "User deleted successfully");
    }

}
