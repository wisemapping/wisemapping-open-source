/*
*    Copyright [2015] [wisemapping]
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

import com.wisemapping.model.Constants;
import com.wisemapping.rest.model.RestUserRegistration;
import com.wisemapping.service.UserService;
import com.wisemapping.view.UserBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class UserValidator
        implements Validator {

    private UserService userService;
    public boolean supports(final Class clazz) {
        return clazz.equals(UserBean.class);
    }

    public void validate(@Nullable Object obj, @NotNull Errors errors) {
        RestUserRegistration user = (RestUserRegistration) obj;
        if (user == null) {
            errors.rejectValue("user", "error.not-specified");
        } else {

            // Validate email address ...
            final String email = user.getEmail();
            boolean isValid = Utils.isValidateEmailAddress(email);
            if (isValid) {
                if (userService.getUserBy(email) != null) {
                    errors.rejectValue("email", Messages.EMAIL_ALREADY_EXIST);
                }
            } else {
                Utils.validateEmailAddress(email, errors);
            }

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstname", Messages.FIELD_REQUIRED);
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastname", Messages.FIELD_REQUIRED);
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", Messages.FIELD_REQUIRED);
            ValidatorUtils.rejectIfExceeded(errors,
                    "firstname",
                    "The firstname must have less than " + Constants.MAX_USER_FIRSTNAME_LENGTH + " characters.",
                    user.getFirstname(),
                    Constants.MAX_USER_FIRSTNAME_LENGTH);
            ValidatorUtils.rejectIfExceeded(errors,
                    "lastname",
                    "The lastname must have less than " + Constants.MAX_USER_LASTNAME_LENGTH + " characters.",
                    user.getLastname(),
                    Constants.MAX_USER_LASTNAME_LENGTH);
            ValidatorUtils.rejectIfExceeded(errors,
                    "password",
                    "The password must have less than " + Constants.MAX_USER_PASSWORD_LENGTH + " characters.",
                    user.getPassword(),
                    Constants.MAX_USER_PASSWORD_LENGTH);
        }
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}