package com.wisemapping.rest;


import com.wisemapping.model.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class UserController {
    private Jaxb2Marshaller jaxb2Mashaller;

    public void setJaxb2Mashaller(@NotNull final Jaxb2Marshaller jaxb2Mashaller) {
        this.jaxb2Mashaller = jaxb2Mashaller;
    }

    private static final String XML_VIEW_NAME = "users";

    @RequestMapping(method = RequestMethod.GET, value = "/employee/{id}")
    public ModelAndView getEmployee(@PathVariable String id) {
        User user = new User();
        return new ModelAndView(XML_VIEW_NAME, "object", user);
    }


}
