package com.wisemapping.rest;


import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.service.UserService;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
public class AdminController {
    @Autowired
    private UserService userService;

    @RequestMapping(method = RequestMethod.GET, value = "admin/users/{id}", produces = {"application/xml", "application/json"})
    @ResponseBody
    public ModelAndView getUserById(@PathVariable int id) throws IOException {
        final User userBy = userService.getUserBy(id);
        if (userBy == null) {
            throw new IllegalArgumentException("User could not be found");
        }
        return new ModelAndView("userView", "user", new RestUser(userBy));
    }

    @RequestMapping(method = RequestMethod.GET, value = "admin/users/email/{email}", produces = {"application/xml", "application/json"})
    @ResponseBody
    public ModelAndView getUserByEmail(@PathVariable String email) throws IOException {
        final User userBy = userService.getUserBy(email);
        if (userBy == null) {
            throw new IllegalArgumentException("User could not be found");
        }
        return new ModelAndView("userView", "user", new RestUser(userBy));
    }

    @RequestMapping(method = RequestMethod.POST, value = "admin/users", consumes = {"application/xml", "application/json"})
    public void getUserByEmail(@RequestBody RestUser user) throws IOException, WiseMappingException {
        if (user == null) {
            throw new IllegalArgumentException("User could not be found");
        }
        userService.createUser(user.getDelegated(), false);
    }

}
