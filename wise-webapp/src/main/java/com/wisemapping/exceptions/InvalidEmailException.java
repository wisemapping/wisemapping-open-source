/*
 *    Copyright [2022] [wisemapping]
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

package com.wisemapping.exceptions;


import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;

public class InvalidEmailException
        extends ClientException {
    private static final String EMAIL_IS_INVALID = "INVALID_EMAIL_ADDRESS";

    public InvalidEmailException(@NotNull String emails) {
        super("Invalid email exception:" + emails, Severity.WARNING);
    }

    @NotNull
    @Override
    protected String getMsgBundleKey() {
        return EMAIL_IS_INVALID;
    }
}
