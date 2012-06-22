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

package com.wisemapping.filter;

import com.wisemapping.exceptions.GoogleChromeFrameRequiredException;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.wisemapping.exceptions.UnsupportedBrowserException;

import java.util.Set;

public class BrowserSupportInterceptor extends HandlerInterceptorAdapter {
    private Set<String> exclude;
    public static final String USER_AGENT = "wisemapping.userAgent";

    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, Object object) throws Exception {

        final String requestUri = request.getRequestURI();
        if (exclude != null && !exclude.contains(requestUri)) {
            final HttpSession session = request.getSession(false);

            // Try to loaded from the request ...
            UserAgent userAgent = null;
            if (session != null) {
                userAgent = (UserAgent) session.getAttribute(USER_AGENT);
            }

            // I could not loaded. I will create a new one...
            if (userAgent == null) {
                userAgent = UserAgent.create(request);
                if (session != null) {
                    session.setAttribute(USER_AGENT, userAgent);
                }
            }

            // It's a supported browser ?.
            if (!userAgent.isBrowserSupported()) {
                throw new UnsupportedBrowserException();
            }

            // Is a Explorer 9 or less without Google Chrome Frame ?.
            if (!userAgent.needsGCF()) {
                throw new GoogleChromeFrameRequiredException();
            }


        }
        return true;
    }


    public void setExclude(Set<String> exclude) {
        this.exclude = exclude;
    }
}
