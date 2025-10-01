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

public interface Messages {
    String EMAIL_ALREADY_EXIST = "EMAIL_ALREADY_EXIST";
    String NO_VALID_EMAIL_ADDRESS = "NO_VALID_EMAIL_ADDRESS";
    String FIELD_REQUIRED = "FIELD_REQUIRED";
    String MAP_TITLE_ALREADY_EXISTS = "MAP_TITLE_ALREADY_EXISTS";
    String LABEL_TITLE_ALREADY_EXISTS = "LABEL_TITLE_ALREADY_EXISTS";

    String CAPTCHA_LOADING_ERROR = "CAPTCHA_LOADING_ERROR";

    String CAPTCHA_TIMEOUT_OUT_DUPLICATE = "CAPTCHA_TIMEOUT_OUT_DUPLICATE";
    String CAPTCHA_INVALID_INPUT_RESPONSE = "CAPTCHA_INVALID_INPUT_RESPONSE";
    
    String DISPOSABLE_EMAIL_NOT_ALLOWED = "DISPOSABLE_EMAIL_NOT_ALLOWED";
}
