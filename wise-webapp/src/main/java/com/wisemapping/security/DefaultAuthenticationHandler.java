/*
*    Copyright [2011] [wisemapping]
*
*   Licensed under the Apache License, Version 2.0 (the "License") plus the
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

package com.wisemapping.security;

import org.acegisecurity.AuthenticationException;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;

public class DefaultAuthenticationHandler
    implements AuthenticationHandler
{
    public AuthenticationToken getAuthenticationToken(HttpServletRequest request)
        throws AuthenticationException
    {
        String username = request.getParameter(AuthenticationProcessingFilter.ACEGI_SECURITY_FORM_USERNAME_KEY);
        String password = request.getParameter(AuthenticationProcessingFilter.ACEGI_SECURITY_FORM_PASSWORD_KEY);
        if (username == null) {
            username = "";
        }

        if (password == null) {
            password = "";
        }
        return new AuthenticationToken(username,password);
    }
}
