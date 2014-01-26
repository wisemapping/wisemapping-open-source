package com.wisemapping.validator;

import com.wisemapping.model.Label;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class LabelValidator implements Validator {
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
        String title = label.getTitle();
        //todo hacer otras validaciones como si supera el maximo o el label existe
    }
}
