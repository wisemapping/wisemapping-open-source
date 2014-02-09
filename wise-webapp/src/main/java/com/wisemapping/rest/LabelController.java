package com.wisemapping.rest;

import com.wisemapping.exceptions.LabelCouldNotFoundException;
import com.wisemapping.exceptions.MapCouldNotFoundException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.Label;
import com.wisemapping.model.Mindmap;
import com.wisemapping.model.User;
import com.wisemapping.rest.model.RestLabel;
import com.wisemapping.rest.model.RestLabelList;
import com.wisemapping.rest.model.RestMindmapInfo;
import com.wisemapping.rest.model.RestMindmapList;
import com.wisemapping.security.Utils;
import com.wisemapping.service.LabelService;
import com.wisemapping.service.MindmapService;
import com.wisemapping.validator.LabelValidator;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class LabelController extends BaseController {

    @Qualifier("labelService")
    @Autowired
    private LabelService labelService;


    @RequestMapping(method = RequestMethod.POST, value = "/labels", consumes = {"application/json", "application/xml"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createLabel(@RequestBody RestLabel restLabel, @NotNull HttpServletResponse response, @RequestParam(required = false) String title) throws WiseMappingException {
        // Overwrite title if it was specified by parameter.
        if (title != null && !title.isEmpty()) {
            restLabel.setTitle(title);
        }

        // Validate ...
        validate(restLabel);

        final Label label = createLabel(restLabel);

        // Return the new created label ...
        response.setHeader("ResourceId", Integer.toString(label.getId()));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/labels", produces = {"application/json", "application/xml"})
    public RestLabelList retrieveList() {
        final User user = Utils.getUser();
        assert user != null;
        final List<Label> all = labelService.getAll(user);
        return new RestLabelList(all);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/labels/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteLabelById(@PathVariable int id) throws WiseMappingException {
        final User user = Utils.getUser();
        final Label label = labelService.getLabelById(id);
        if (label == null) {
            throw new LabelCouldNotFoundException("Label could not be found. Id: " + id);
        }
        assert user != null;
        labelService.removeLabel(label, user);
    }

    @NotNull private Label createLabel(@NotNull final RestLabel restLabel) throws WiseMappingException {
        final Label label = restLabel.getDelegated();
        // Add new label ...
        final User user = Utils.getUser();
        assert user != null;
        labelService.addLabel(label, user);
        return label;
    }

    private void validate(@NotNull final RestLabel restLabel) throws ValidationException {
        final BindingResult result = new BeanPropertyBindingResult(restLabel, "");
        new LabelValidator(labelService).validate(restLabel.getDelegated(), result);
        if (result.hasErrors()) {
            throw new ValidationException(result);
        }
    }
}
