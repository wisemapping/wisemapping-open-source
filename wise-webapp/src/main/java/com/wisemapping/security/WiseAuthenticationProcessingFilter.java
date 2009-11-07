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

package com.wisemapping.security;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class WiseAuthenticationProcessingFilter
    extends AuthenticationProcessingFilter
{
    public static final String ACEGI_SECURITY_FORM_SSO_ID_KEY = "j_sso_id";   

    private AuthenticationHandler authenticationHandler;

    public void setAuthenticationHandler(AuthenticationHandler ssoAuthenticationHandler)
    {
        this.authenticationHandler = ssoAuthenticationHandler;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request)
           throws AuthenticationException
    {

        final AuthenticationToken ssoToken = authenticationHandler.getAuthenticationToken(request);

        final UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(ssoToken.getUsername(), ssoToken.getPassword());

        // Place the last username attempted into HttpSession for views
        request.getSession().setAttribute(ACEGI_SECURITY_LAST_USERNAME_KEY, ssoToken.getUsername());

        // Allow subclasses to set the "details" property
        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void onPreAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException, IOException
    {
        assert request != null;
    }
}
