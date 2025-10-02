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

/**
 * Exception thrown when a user tries to change their password but their authentication method
 * doesn't allow password changes (e.g., Google OAuth2, LDAP, Facebook OAuth2).
 */
public class PasswordChangeNotAllowedException extends WiseMappingException {
    
    public PasswordChangeNotAllowedException() {
        super("Password changes are not allowed for external authentication providers (Google, LDAP, Facebook). Please change your password through your authentication provider.");
    }
    
    public PasswordChangeNotAllowedException(String message) {
        super(message);
    }
    
    public PasswordChangeNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}
