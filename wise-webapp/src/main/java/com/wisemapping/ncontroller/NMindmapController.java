package com.wisemapping.ncontroller;


import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestUser;
import com.wisemapping.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

@Controller
public class NMindmapController {
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
}
