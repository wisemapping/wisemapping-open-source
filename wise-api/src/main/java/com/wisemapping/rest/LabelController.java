/*
 *    Copyright [2007-2025] [wisemapping]
 *
 *   Licensed under WiseMapping Public License, Version 1.0 (the "License").
 *   It is basically the Apache License, Version 2.0 (the "License") plus the
 *   "powered by wisemapping" text requirement on every single page;
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the license at
 *
 *       https://github.com/wisemapping/wisemapping-open-source/blob/main/LICENSE.md
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package com.wisemapping.rest;

import com.wisemapping.exceptions.LabelCouldNotFoundException;
import com.wisemapping.exceptions.ValidationException;
import com.wisemapping.exceptions.WiseMappingException;
import com.wisemapping.model.MindmapLabel;
import com.wisemapping.model.Account;
import com.wisemapping.rest.model.RestLabel;
import com.wisemapping.rest.model.RestLabelList;
import com.wisemapping.security.Utils;
import com.wisemapping.service.LabelService;
import com.wisemapping.validator.LabelValidator;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/restful/labels")
@PreAuthorize("isAuthenticated() and hasRole('ROLE_USER')")
public class LabelController extends BaseController {

    @Qualifier("labelService")
    @Autowired
    private LabelService labelService;

    @RequestMapping(method = RequestMethod.POST, value = "", consumes = {"application/json"})
    @ResponseStatus(value = HttpStatus.CREATED)
    public void createLabel(@RequestBody RestLabel restLabel, @NotNull HttpServletResponse response, @RequestParam(required = false) String title) throws WiseMappingException {
        // Overwrite title if it was specified by parameter.
        if (title != null && !title.isEmpty()) {
            restLabel.setTitle(title);
        }

        // Validate ...
        validate(restLabel);

        final MindmapLabel label = createLabel(restLabel);

        // Return the new created label ...
        response.setHeader("Location", "/api/restful/labels/" + label.getId());
        response.setHeader("ResourceId", Long.toString(label.getId()));
    }

    @RequestMapping(method = RequestMethod.GET, value = "/", produces = {"application/json"})
    public RestLabelList retrieveList() {
        final Account user = Utils.getUser();
        assert user != null;
        final List<MindmapLabel> all = labelService.getAll(user);
        return new RestLabelList(all);
    }

    @RequestMapping(method = RequestMethod.DELETE, value = "/{id}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteLabelById(@PathVariable int id) throws WiseMappingException {
        final Account user = Utils.getUser();
        final MindmapLabel label = labelService.findLabelById(id, user);
        if (label == null) {
            throw new LabelCouldNotFoundException("Label could not be found. Id: " + id);
        }
        assert user != null;
        labelService.removeLabel(label, user);
    }

    @NotNull private MindmapLabel createLabel(@NotNull final RestLabel restLabel) throws WiseMappingException {
        final MindmapLabel label = restLabel.getDelegated();
        // Add new label ...
        final Account user = Utils.getUser();
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
