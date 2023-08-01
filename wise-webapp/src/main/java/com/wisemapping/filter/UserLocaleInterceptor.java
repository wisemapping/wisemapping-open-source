/*
*    Copyright [2022] [wisemapping]
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

import com.wisemapping.model.User;
import com.wisemapping.security.Utils;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;

public class UserLocaleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, Object object) throws Exception {

        final HttpSession session = request.getSession(false);
        User user = Utils.getUser(false);

        if (user != null && session != null) {
            String userLocale = user.getLocale();
            final Locale sessionLocale = (Locale) session.getAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME);
            if ((userLocale != null) && ((sessionLocale == null) || (!userLocale.equals(sessionLocale.toString())))) {
                Locale locale;
                if (userLocale.contains("_")) {
                    final String[] spit = userLocale.split("_");
                    locale = new Locale(spit[0], spit[1]);
                } else {
                    locale = new Locale(userLocale);
                }
                session.setAttribute(SessionLocaleResolver.LOCALE_SESSION_ATTRIBUTE_NAME, locale);
            }
        }
        return true;
    }
}
