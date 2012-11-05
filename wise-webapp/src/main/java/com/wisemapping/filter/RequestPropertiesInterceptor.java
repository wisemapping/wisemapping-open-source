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

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.regex.Pattern;


public class RequestPropertiesInterceptor extends HandlerInterceptorAdapter {
    @Value("${google.analytics.enabled}")
    private Boolean analyticsEnabled;

    @Value("${google.analytics.account}")
    private String analyticsAccount;

    @Value("${google.ads.enabled}")
    private Boolean adsEnabled;

    @Value("${site.homepage}")
    private String siteHomepage;

    @Autowired
    Environment env;

    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, Object object) throws Exception {

        request.setAttribute("google.analytics.enabled", analyticsEnabled);
        request.setAttribute("google.analytics.account", analyticsAccount);
        request.setAttribute("google.ads.enabled", adsEnabled);
        request.setAttribute("site.homepage", siteHomepage);

        final String baseUrl;
        if (env.containsProperty("site.baseurl")) {
            baseUrl = env.getProperty("site.baseurl");
        } else {
            baseUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        }
        request.setAttribute("site.baseurl", baseUrl);
        return true;
    }
}
