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
package com.wisemapping.validator;

import com.wisemapping.model.Constants;
import com.wisemapping.model.MindmapLabel;
import com.wisemapping.model.Account;
import com.wisemapping.service.LabelService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class LabelValidator implements Validator {

    private final LabelService service;

    public LabelValidator(@NotNull final LabelService service) {
        this.service = service;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.equals(MindmapLabel.class);
    }

    @Override
    public void validate(@Nullable final Object target, @NotNull final Errors errors) {
        final MindmapLabel label = (MindmapLabel) target;
        if (label == null) {
            errors.rejectValue("map", "error.not-specified", null, "Value required.");
        } else {
            validateLabel(label, errors);

        }
    }

    private void validateLabel(@NotNull final MindmapLabel label, @NotNull final Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", Messages.FIELD_REQUIRED);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "color", Messages.FIELD_REQUIRED);
        final String title = label.getTitle();

        ValidatorUtils.rejectIfExceeded(
                errors,
                "title",
                "The description must have less than " + Constants.MAX_LABEL_NAME_LENGTH + " characters.",
                title,
                Constants.MAX_LABEL_NAME_LENGTH);

        final Account user = com.wisemapping.security.Utils.getUser();
        if (user != null && title != null) {
            final MindmapLabel foundLabel = service.getLabelByTitle(title, user);
            if (foundLabel != null) {
                errors.rejectValue("title", Messages.LABEL_TITLE_ALREADY_EXISTS);
            }
        }

    }
}
