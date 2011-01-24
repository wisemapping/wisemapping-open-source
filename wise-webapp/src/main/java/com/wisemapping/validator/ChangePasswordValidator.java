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

import org.springframework.validation.Validator;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import com.wisemapping.view.ChangePasswordBean;
import com.wisemapping.model.Constants;

public class ChangePasswordValidator
        implements Validator {

    public boolean supports(final Class clazz) {
        return clazz.equals(ChangePasswordBean.class);
    }

    public void validate(Object obj, Errors errors) {
        ChangePasswordBean bean = (ChangePasswordBean) obj;
	
        if (bean == null) {
            errors.rejectValue("changePassword", "error.not-specified", null, "Value required.");
        } else {
	
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "required", "Field is required.");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "retryPassword", "required", "Field is required.");
            ValidatorUtils.rejectIfExceeded(errors,
                                "password",
                                "The password must have less than "+ Constants.MAX_USER_PASSWORD_LENGTH + " characters.",
                                bean.getPassword(),
                                Constants.MAX_USER_PASSWORD_LENGTH);
            ValidatorUtils.rejectIfExceeded(errors,
                                "retryPassword",
                                "The retryPassword must have less than "+ Constants.MAX_USER_PASSWORD_LENGTH + " characters.",
                                bean.getRetryPassword(),
                                Constants.MAX_USER_PASSWORD_LENGTH);
            final String password = bean.getPassword();
            if (password != null && !password.equals(bean.getRetryPassword())) {
                errors.rejectValue("password", "Password mismatch", "Your password entries did not match");
            }
        }
    }
}
