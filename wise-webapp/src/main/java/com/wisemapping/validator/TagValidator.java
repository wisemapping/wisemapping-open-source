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

import com.wisemapping.model.Constants;
import com.wisemapping.view.TagBean;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class TagValidator implements Validator {

    public boolean supports(final Class clazz) {
        return clazz.equals(TagBean.class);
    }

    public void validate(Object obj, Errors errors) {
        TagBean tag = (TagBean) obj;
        if (tag == null) {
            errors.rejectValue("user", "error.not-specified");
        } else {

            // Validate email address ...
            final String tags = tag.getMindmapTags();
            ValidatorUtils.rejectIfExceeded(errors,
                                "mindmapTags",
                                "The tags must have less than "+ Constants.MAX_TAGS_LENGTH + " characters.",
                                tags,
                                Constants.MAX_TAGS_LENGTH);
        }
    }
}
