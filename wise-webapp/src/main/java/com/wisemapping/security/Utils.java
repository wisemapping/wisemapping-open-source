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

import com.wisemapping.model.User;

import javax.servlet.http.HttpServletRequest;

import org.acegisecurity.providers.AbstractAuthenticationToken;
import org.acegisecurity.context.SecurityContextHolder;
import org.acegisecurity.Authentication;


public class Utils {
    private Utils() {
    }

    public static User getUser(final HttpServletRequest request) {

        final AbstractAuthenticationToken token = (AbstractAuthenticationToken) request.getUserPrincipal();
        User result = null;
        if (token != null) {
            final com.wisemapping.security.User user = (com.wisemapping.security.User) token.getPrincipal();
            result = user.getModel();
        }
        return result;
    }

    public static User getUser()
    {
        User user = null;
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getDetails() != null)
        {
            final Object principal = auth.getPrincipal();
            if (principal != null && principal instanceof com.wisemapping.security.User) {
                user = ((com.wisemapping.security.User)principal).getModel();
            }
        }
        return user;
    }
}
