/*
*    Copyright [2011] [wisemapping]
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

package com.wisemapping.validator;

import com.wisemapping.model.MindMap;
import com.wisemapping.model.User;
import com.wisemapping.model.Constants;
import com.wisemapping.service.MindmapService;
import com.wisemapping.view.MindMapInfoBean;
import org.jetbrains.annotations.NotNull;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class MapInfoValidator implements Validator {


    private MindmapService mindmapService;

    public boolean supports(final Class clazz) {
        return clazz.equals(MindMapInfoBean.class);
    }

    public MapInfoValidator() {

    }

    public MapInfoValidator(@NotNull MindmapService service) {
        this.mindmapService = service;
    }

    public void validate(Object obj, @NotNull Errors errors) {
        final MindMap map = (MindMap) obj;


        if (map == null) {
            errors.rejectValue("map", "error.not-specified", null, "Value required.");
        } else {

            final String title = map.getTitle();
            final String desc = map.getDescription();
            validateMapInfo(errors, title, desc);
        }

    }

    protected void validateMapInfo(Errors errors, String title, String desc) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", Messages.FIELD_REQUIRED);


        if (title != null && title.length() > 0) {
            if (title.length() > Constants.MAX_MAP_NAME_LENGTH) {
                errors.rejectValue("title", "field.max.length",
                        new Object[]{Constants.MAX_MAP_NAME_LENGTH},
                        "The title must have less than " + Constants.MAX_MAP_NAME_LENGTH + " characters.");
            } else {
                // Map already exists ?
                final MindmapService service = this.getMindmapService();
                final User user = com.wisemapping.security.Utils.getUser();
                final MindMap mindMap = service.getMindmapByTitle(title, user);
                if (mindMap != null) {
                    errors.rejectValue("title", Messages.MAP_TITLE_ALREADY_EXISTS);
                }
            }
        }
        ValidatorUtils.rejectIfExceeded(errors,
                "description",
                "The description must have less than " + Constants.MAX_MAP_DESCRIPTION_LENGTH + " characters.",
                desc,
                Constants.MAX_MAP_DESCRIPTION_LENGTH);
    }

    public MindmapService getMindmapService() {
        return mindmapService;
    }

    public void setMindmapService(MindmapService mindmapService) {
        this.mindmapService = mindmapService;
    }
}