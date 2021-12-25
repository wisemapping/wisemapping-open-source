/*
*    Copyright [2015] [wisemapping]
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public class RequestPropertiesInterceptor extends HandlerInterceptorAdapter {
    @Value("${google.analytics.enabled}")
    private Boolean analyticsEnabled;

    @Value("${google.analytics.account}")
    private String analyticsAccount;

    @Value("${google.recaptcha2.enabled}")
    private Boolean recaptcha2Enabled;

    @Value("${google.recaptcha2.siteKey}")
    private String recaptcha2SiteKey;

    @Value("${google.ads.enabled}")
    private Boolean adsEnabled;

    @Value("${site.homepage}")
    private String siteHomepage;

    @Value("${site.baseurl}")
    private String siteUrl;

    @Value("${security.type}")
    private String securityType;

    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, Object object) throws Exception {

        request.setAttribute("google.analytics.enabled", analyticsEnabled);
        request.setAttribute("google.analytics.account", analyticsAccount);
        request.setAttribute("google.ads.enabled", adsEnabled);

        request.setAttribute("google.recaptcha2.enabled", recaptcha2Enabled);
        request.setAttribute("google.recaptcha2.siteKey", recaptcha2SiteKey);

        request.setAttribute("site.homepage", siteHomepage);
        request.setAttribute("security.type", securityType);

        // If the property could not be resolved, try to infer one from the request...
        if ("${site.baseurl}".equals(siteUrl)) {
            siteUrl = request.getRequestURL().toString().replace(request.getRequestURI(), request.getContextPath());
        }
        request.setAttribute("site.baseurl", siteUrl);
        return true;
    }
}
