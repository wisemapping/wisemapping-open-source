package com.wisemapping.validator;

import com.wisemapping.model.Constants;
import com.wisemapping.model.Label;
import com.wisemapping.model.User;
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
        return clazz.equals(Label.class);
    }

    @Override
    public void validate(@Nullable final Object target, @NotNull final Errors errors) {
        final Label label = (Label) target;
        if (label == null) {
            errors.rejectValue("map", "error.not-specified", null, "Value required.");
        } else {
            validateLabel(label, errors);

        }
    }

    private void validateLabel(@NotNull final Label label, @NotNull final Errors errors) {
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "title", Messages.FIELD_REQUIRED);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "color", Messages.FIELD_REQUIRED);
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "icon", Messages.FIELD_REQUIRED);
        final String title = label.getTitle();
        ValidatorUtils.rejectIfExceeded(
                errors,
                "title",
                "The description must have less than " + Constants.MAX_LABEL_NAME_LENGTH + " characters.",
                title,
                Constants.MAX_LABEL_NAME_LENGTH);
        final User user = com.wisemapping.security.Utils.getUser();
        assert user != null;
        final Label foundLabel = service.getLabelByTitle(title, user);
        if (foundLabel != null) {
            errors.rejectValue("title", Messages.LABEL_TITLE_ALREADY_EXISTS);
        }
    }
}
