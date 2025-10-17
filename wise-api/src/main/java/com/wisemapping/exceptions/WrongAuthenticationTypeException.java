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

package com.wisemapping.exceptions;

import com.wisemapping.model.AuthenticationType;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when a user tries to authenticate with the wrong authentication method.
 * For example, when a user registered with Google OAuth tries to login with email/password.
 */
public class WrongAuthenticationTypeException extends AuthenticationException {
    
    private final AuthenticationType authenticationType;
    
    public WrongAuthenticationTypeException(@NotNull AuthenticationType authenticationType, @NotNull String message) {
        super(message);
        this.authenticationType = authenticationType;
    }
    
    @NotNull
    public AuthenticationType getAuthenticationType() {
        return authenticationType;
    }
}

