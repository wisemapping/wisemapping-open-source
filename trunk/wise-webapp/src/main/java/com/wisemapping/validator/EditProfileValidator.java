/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  See the NOTICE file distributed with
* this work for additional information regarding copyright ownership.
* The ASF licenses this file to You under the Apache License, Version 2.0
* (the "License"); you may not use this file except in compliance with
* the License.  You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* $Id: file 64488 2006-03-10 17:32:09Z paulo $
*/

package com.wisemapping.validator;

import com.wisemapping.view.UserBean;
import com.wisemapping.controller.Messages;
import com.wisemapping.service.UserService;
import com.wisemapping.model.User;
import com.wisemapping.model.Constants;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class EditProfileValidator implements Validator {

    private UserService userService;

    public boolean supports(final Class clazz) {
        return clazz.equals(UserBean.class);
    }

    public void validate(Object obj, Errors errors) {
        UserBean user = (UserBean) obj;
        if (user == null) {
            errors.rejectValue("user", "error.not-specified", null, "Value required.");
        } else {

            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "firstname", "required", "Field is required.");
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "lastname", "required", "Field is required.");
            ValidatorUtils.rejectIfExceeded(errors,
                                "firstname",
                                "The firstname must have less than "+ Constants.MAX_USER_FIRSTNAME_LENGTH + " characters.",
                                user.getFirstname(),
                                Constants.MAX_USER_FIRSTNAME_LENGTH);
            ValidatorUtils.rejectIfExceeded(errors,
                                "lastname",
                                "The lastname must have less than "+ Constants.MAX_USER_LASTNAME_LENGTH + " characters.",
                                user.getLastname(),
                                Constants.MAX_USER_LASTNAME_LENGTH);
            final String email = user.getEmail();
            boolean isValid = Utils.isValidateEmailAddress(email);
            if (isValid) {
                final User oldUser = userService.getUserBy(email);
                if (oldUser != null && user.getId() != oldUser.getId()) {
                    errors.rejectValue("email", Messages.EMAIL_ALREADY_EXIST);
                }
            } else {
                Utils.validateEmailAddress(email, errors);
            }
        }
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}