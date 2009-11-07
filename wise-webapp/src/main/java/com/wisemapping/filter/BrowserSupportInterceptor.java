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

package com.wisemapping.filter;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.wisemapping.exceptions.UnsupportedBrowserException;

import java.util.List;

public class BrowserSupportInterceptor extends HandlerInterceptorAdapter {
    private List<String> exclude;
    public static final String USER_AGENT = "wisemapping.userAgent";

    public boolean preHandle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object object) throws Exception {

        final String servletPath = httpServletRequest.getServletPath();
        if (exclude != null && !exclude.contains(servletPath)) {
            final HttpSession session = httpServletRequest.getSession(false);

            // Try to loaded from the request ...
            UserAgent userAgent = null;
            if (session != null) {
                userAgent = (UserAgent) session.getAttribute(USER_AGENT);
            }

            // I could not loaded. I will create a new one...
            if (userAgent == null) {
                userAgent = UserAgent.create(httpServletRequest);
                if (session != null) {
                    session.setAttribute(USER_AGENT, userAgent);
                }
            }

            // It's a supported browser ?.
            if (!userAgent.isBrowserSupported()) {
                throw new UnsupportedBrowserException();
            }
        }
        return true;
    }


    public List<String> getExclude() {
        return exclude;
    }

    public void setExclude(List<String> exclude) {
        this.exclude = exclude;
    }
}
