/*
*    Copyright [2012] [wisemapping]
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
import com.wisemapping.exceptions.UnsupportedBrowserException;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RequestPropertiesInterceptor extends HandlerInterceptorAdapter {
    private Map<String, String> attributes;

    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, Object object) throws Exception {
        for (String key : attributes.keySet()) {
            request.setAttribute(key, attributes.get(key));
        }
        return true;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }
}
