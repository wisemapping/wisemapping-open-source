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

import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import com.wisemapping.controller.Messages;

public class Utils {
    //Set the email emailPattern string

    static private Pattern emailPattern = Pattern.compile(".+@.+\\.[a-z]+");

    private Utils() {

    }

    static void validateEmailAddress(final String email, final Errors errors) {
        if (email == null || email.trim().length() == 0) {
            ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", Messages.FIELD_REQUIRED);
        } else {
            boolean isValid = Utils.isValidateEmailAddress(email);
            if (!isValid) {
                errors.rejectValue("email", Messages.NO_VALID_EMAIL_ADDRESS);
            }
        }
    }

    static boolean isValidateEmailAddress(final String email) {

        //Match the given string with the emailPattern
        final Matcher m = emailPattern.matcher(email);

        //check whether match is found
        return m.matches();
    }
}
