package com.wisemapping.rest;

import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Label;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestLabel;
import com.wisemapping.security.Utils;
import com.wisemapping.service.LabelService;
import com.wisemapping.validator.LabelValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;

@Controller
public class LabelController extends BaseController {

    @Qualifier("labelService")
    @Autowired
    private LabelService labelService;


    @RequestMapping(method = RequestMethod.POST, value = "/labels", consumes = {"application/json"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createLabel(@RequestBody RestLabel restLabel, @NotNull HttpServletResponse response, @RequestParam(required = false) String title) throws WiseMappingException {
        // Overwrite title if it was specified by parameter.
        if (title != null && !title.isEmpty()) {
            restLabel.setTitle(title);
        }

        final Label label = restLabel.getDelegated();

        // Validate ...
        final BindingResult result = new BeanPropertyBindingResult(restLabel, "");
        new LabelValidator().validate(label, result);
        if (result.hasErrors()) {

            throw new ValidationException(result);
        }

        // Add new label ...
        final User user = Utils.getUser();
        assert user != null;
        labelService.addLabel(label, user);

        // Return the new created map ...
        response.setHeader("ResourceId", Integer.toString(label.getId()));
    }

}
